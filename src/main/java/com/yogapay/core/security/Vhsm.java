package com.yogapay.core.security;

import java.util.Random;
import java.util.UUID;

import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yogapay.core.exception.HsmException;

/**
 * 虚拟加密机，即软加密，需要知道zmk的明文，然后使用lmk生成加密的密文作为LMK_ZMK
 * 
 * @author dj
 * 
 */
public class Vhsm implements IHsm {

	private static final Logger log = LoggerFactory.getLogger(Vhsm.class);
	/**
	 * LMK
	 */
	private String LMK_KEY = "0000000000000000";

	public Vhsm() {
	}

	public Vhsm(String mk) {
		LMK_KEY = mk;
	}

	public void setMk(String mk) {
		LMK_KEY = mk;
	}

	/**
	 * 明文KEY和指定keyType生产lmk下加密的key
	 * 
	 * @param keyType
	 *            密钥类型,软加密不区分加密类型，不进行判断
	 * @param clearKey
	 *            明文KEY
	 * @return lmk下加密的key
	 * @throws HsmException
	 */
	public String[] genKey(KEY_TYPE keyType, String clearKey)
			throws HsmException {
		String desKey = JCEHandler.encryptData(clearKey, getLmk(clearKey));
		String checkVlue = genCheckValue(KEY_TYPE.ZAK, desKey);
		return new String[] { desKey, checkVlue };
	}

	/**
	 * 由LMK下加密的TPK导出为TMK下加密
	 * 
	 * @param lmkTpk
	 *            LMK下加密的TPK
	 * @param lmkTmk
	 *            LMK下加密的TMK
	 * @return TMK下加密的TPK
	 * 
	 */
	public String[] exportTmkTpk(String lmkTak, String lmkTmk) {
		String clearTmkKey = clearKey(lmkTmk);
		String clearTakKey = clearKey(lmkTak);

		byte[] result = JCEHandler.encryptData(ISOUtil.hex2byte(clearTakKey),
				ISOUtil.hex2byte(clearTmkKey));
		String desKey = ISOUtil.hexString(result);
		String checkVlue = "";
		checkVlue = genCheckValue(KEY_TYPE.ZAK, lmkTak);
		return new String[] { desKey, checkVlue };
	}

	/**
	 * 计算pinblock，可以使用tpk或者zpk和明文的密码进行计算pinblock
	 * 
	 * @param pin
	 *            明文的pin
	 * @param accountNo
	 *            卡号
	 * @param pk
	 *            zpk或者tpk
	 * @param keyType
	 *            密钥类型
	 * @return pinblock
	 * @throws HsmException
	 */
	public String genPinBlock(String pin, String accountNo, KEY_TYPE keyType,
			String pk) throws HsmException {
		String clearKey = JCEHandler.decryptData(pk, getLmk(pk));
		accountNo = accountNo.substring(accountNo.length() - 13,
				accountNo.length() - 1);
		// 将卡号与密码进行异或
		byte[] x = ISOUtil.xor(ISOUtil.hex2byte("06" + pin + "FFFFFFFF"),
				ISOUtil.hex2byte("0000" + accountNo));
		String pinblock = JCEHandler
				.encryptData(ISOUtil.hexString(x), clearKey);
		return pinblock;
	}

	/**
	 * ECB银联标准MAC加密算法 ，mac计算，从mti到63域之间的部分作为data传入
	 * 
	 * @param desAk
	 *            mk加密的密文
	 * @param data
	 *            先对传来的数据进行64域填8个字节的0，用于长度截取
	 * @return mac
	 */
	public byte[] genECBMAC(byte[] data, KEY_TYPE keyType, String key) {
		byte[] clearTak = ISOUtil.hex2byte(JCEHandler.decryptData(key,
				getLmk(key)));
		data = ISOUtil.concat(data, new byte[8]);
		int len = data.length - data.length % 8;
		if (data.length != len) {
			data = ISOUtil.trim(data, len);
		} else {
			data = ISOUtil.trim(data, len - 8);
		}
		byte[] mac = new byte[8];
		byte[] tmp = new byte[8];
		byte[] block = new byte[8];
		for (int i = 0; i < data.length / 8; i++) {
			System.arraycopy(data, i * 8, tmp, 0, 8);
			block = ISOUtil.xor(block, tmp);
		}
		block = ISOUtil.hexString(block).getBytes();
		System.arraycopy(block, 0, tmp, 0, 8);
		mac = JCEHandler.encryptData(tmp, clearTak);
		System.arraycopy(block, 8, tmp, 0, 8);
		tmp = ISOUtil.xor(mac, tmp);
		mac = JCEHandler.encryptData(tmp, clearTak);
		block = ISOUtil.hexString(mac).getBytes();
		System.arraycopy(block, 0, mac, 0, 8);

		return mac;
	}

	/**
	 * 把zmk下加密的zpk转换成lmk下加密，多用于收单机构签到时候的转换
	 * 
	 * @param zmkZpk
	 *            zmk下加密的zpk，即签到下发下来的
	 * @param lmkZmk
	 *            lmk下加密的zmk，一般都是收单机构录入加密机里的密文
	 * 
	 * @return lmk下加密的zpk
	 */
	@Override
	public String[] translateZmkZpkLmkZpk(String zmkZpk, String lmkZmk) {
		String clearLmkZmk = clearKey(lmkZmk);
		String clearZmkZpk = JCEHandler.decryptData(zmkZpk, clearLmkZmk);
		String desLmkZpk = JCEHandler.encryptData(clearZmkZpk, getLmk(lmkZmk));
		String checkValue = "";
		checkValue = genCheckValue(KEY_TYPE.ZAK, zmkZpk);
		return new String[] { desLmkZpk, checkValue };
	}

	/**
	 * 把zmk下加密的zak转换成lmk下加密，多用于收单机构签到时候的转换
	 * 
	 * @param zmkZak
	 *            zmk下加密的zak，即签到下发下来的
	 * @param lmkZmk
	 *            lmk下加密的zmk，一般都是收单机构录入加密机里的密文
	 * 
	 * @return lmk下加密的zak
	 */
	@Override
	public String[] translateZmkZakLmkZak(String zmkZak, String lmkZmk) {
		String clearLmkZmk = clearKey(lmkZmk);
		String clearZmkZak = JCEHandler.decryptData(zmkZak, clearLmkZmk);
		String desLmkZak = JCEHandler.encryptData(clearZmkZak, getLmk(lmkZmk));
		String checkValue = "";
		checkValue = genCheckValue(KEY_TYPE.ZAK, lmkZmk);
		return new String[] { desLmkZak, checkValue };
	}

	/**
	 * 把tpk转换成zpk
	 * 
	 * @param pinBlock
	 *            tpk加密的pinblock
	 * @param accountNo
	 *            卡号
	 * @param lmkTpk
	 *            lmk下加密的tpk
	 * @param lmkZpk
	 *            lmk下机密的zpk
	 * 
	 * @return zpk下机密的pinblock
	 * @throws HsmException
	 */
	@Override
	public String translateLmkTpkLmkZpk(String pinBlock, String accountNo,
			String lmkTpk, String lmkZpk) {

		return null;
	}

	/**
	 * 把一个zpk转换成另外一个zpk下加密
	 * 
	 * @param pinBlock
	 *            原zpk下机密的pinblock
	 * @param accountNo
	 *            卡号
	 * @param srcLmkZpk
	 *            原加密使用的zpk
	 * @param destLmkZpk
	 *            要转换加密的zpk
	 * 
	 * @return 新的zpk下加密的pinblock
	 * @throws HsmException
	 */
	@Override
	public String translateLmkZpkLmkZpk(String pinBlock, String accountNo,
			String srcLmkZpk, String destLmkZpk) throws HsmException {
		String clearSrcZpk = JCEHandler.decryptData(srcLmkZpk,
				getLmk(srcLmkZpk));
		String accountNoClone = accountNo;
		accountNo = accountNo.substring(accountNo.length() - 13,
				accountNo.length() - 1);
		// 先用加密的srcLmkZpk解开加密前的数据
		String clearData = JCEHandler.decryptData(pinBlock, clearSrcZpk);
		// 在做异或
		String srcData = ISOUtil.hexString((ISOUtil.xor(
				ISOUtil.hex2byte(clearData),
				ISOUtil.hex2byte("0000" + accountNo))));

		String clearPin = srcData.substring(2, 8);
		pinBlock = genPinBlock(clearPin, accountNoClone, KEY_TYPE.ZPK,
				destLmkZpk);
		return pinBlock;
	}

	/**
	 * 指定密钥类型和des算法生产密钥
	 * 
	 * @param keyType
	 *            密钥类型
	 * @param desType
	 *            des算法
	 * @return lmk下的密钥和校验值
	 */
	@Override
	public String[] genKeyRandom(KEY_TYPE keyType, DES_TYPE desType) {
		UUID uuid = UUID.randomUUID();
		String key = uuid.toString().replace("-", "");
		if (DES_TYPE.DES == desType) {
			key = key.substring(0, 16);
		}

		String lmkKey = JCEHandler.encryptData(key, getLmk(key));
		String checkValue;
		checkValue = genCheckValue(KEY_TYPE.ZAK, key);
		return new String[] { lmkKey, checkValue };
	}

	@Override
	public String[] exportTmkTak(String lmkTak, String lmkTmk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] exportZmkZak(String lmkZak, String lmkZmk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] exportZmkZpk(String lmkZpk, String lmkZmk) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 根据密钥类型和lmk下的密钥计算校验值
	 * 
	 * @param keyType
	 *            密钥类型
	 * @param lmkKey
	 *            lmk下加密的密钥
	 * @return 校验值
	 * @throws HsmException
	 */
	@Override
	public String genCheckValue(KEY_TYPE keyType, String lmkKey) {
		String clearKey = clearKey(lmkKey);
		DES_TYPE desType = getDesType(clearKey);
		String checkvalue = null;
		if (desType == DES_TYPE.DES) {
			checkvalue = JCEHandler.encryptData(clearKey, ZERO_16);
		}
		if (desType == DES_TYPE.DES3_2KEY) {
			checkvalue = JCEHandler.encryptData(clearKey, ZERO_32);
		}
		return checkvalue;
	}

	/**
	 * 根据密文的Key解出明文的Key
	 * 
	 * @param desKey
	 *            LMK下加密的key
	 * @return
	 */
	private String clearKey(String desKey) {
		byte[] result = JCEHandler.decryptData(ISOUtil.hex2byte(desKey),
				ISOUtil.hex2byte(LMK_KEY));
		return ISOUtil.hexString(result);

	}

	/**
	 * 根据要加密的数据长度来截取lmk
	 * 
	 * @param data
	 *            加密的数据
	 * @return 截取的lmk
	 */
	public String getLmk(String data) {
		String lmk = LMK_KEY;
		DES_TYPE desType = getDesType(data);
		if (desType == DES_TYPE.DES) {
			lmk = LMK_KEY.substring(0, 16);
		}
		return lmk;
	}

	/**
	 * 传入的Key长度来判断des算法
	 * 
	 * @param key
	 *            输入的key
	 * @return DES_TYPE
	 */
	public DES_TYPE getDesType(String key) {
		DES_TYPE desType = null;
		int len = key.length();
		if (len == 16) {
			desType = DES_TYPE.DES;
		}
		if (len == 32) {
			desType = DES_TYPE.DES3_2KEY;
		}
		return desType;
	}

	@Override
	public String[] exportZmkZmk(String lmkZmk, String lmkKek)
			throws HsmException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String randomClearKey(DES_TYPE desType) throws HsmException {
		Random random = new Random();
		String s = "";
		int len = 16;
		if (desType == DES_TYPE.DES3_2KEY) {
			len = 32;
		}
		if (desType == DES_TYPE.DES3_3KEY) {
			len = 48;
		}

		for (int i = 0; i < 8; i++) {
			s = s + Integer.toHexString(random.nextInt());
		}
		return s.toUpperCase().substring(0, len);
	}

}
