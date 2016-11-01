package com.yogapay.core.security;

import com.yogapay.core.exception.HsmException;

/**
 * 提供报文常用mac计算，pin转加密等操作，具体型号加密机需要实现接口
 * 
 * @author dj
 * 
 */
public interface IHsm {

	public static final String ZERO_16 = "0000000000000000";
	public static final String ZERO_32 = "00000000000000000000000000000000";
	public static final String ZERO_48 = "000000000000000000000000000000000000000000000000";

	enum KEY_TYPE {
		ZMK, ZPK, ZAK, TMK, TPK, TAK, BDK
	}

	enum DES_TYPE {
		DES, DES3_2KEY, DES3_3KEY
	}

	enum MAC_TYPE {
		ECB, DES3_2KEY, DES3_3KEY
	}

	public void setMk(String mk);

	/**
	 * 指定密钥类型和des算法生产密钥
	 * 
	 * @param keyType
	 *            密钥类型
	 * @param desType
	 *            des算法
	 * @return lmk下的密钥和校验值
	 */
	public String[] genKeyRandom(KEY_TYPE keyType, DES_TYPE desType)
			throws HsmException;

	public String[] exportZmkZmk(String lmkZmk, String lmkKek)
			throws HsmException;

	/**
	 * 根据一个明文生成指定类型的密钥
	 * 
	 * @param keyType
	 *            密钥类型
	 * @param clearKey
	 *            明文key
	 * @return lmk下的密钥和校验值
	 * @throws HsmException
	 */
	public String[] genKey(KEY_TYPE keyType, String clearKey)
			throws HsmException;

	/**
	 * 随机产生一个明文的KEY
	 * 
	 * @param desType
	 * @return
	 * @throws HsmException
	 */
	public String randomClearKey(DES_TYPE desType) throws HsmException;

	/**
	 * 由LMK下加密的TPK导出为TMK下加密
	 * 
	 * @param lmkTpk
	 *            LMK下加密的TPK
	 * @param lmkTmk
	 *            LMK下加密的TMK
	 * 
	 * @return TMK下加密的TPK
	 * 
	 */
	public String[] exportTmkTpk(String lmkTpk, String lmkTmk)
			throws HsmException;

	/**
	 * 由LMK下加密的TAK导出为TMK下加密
	 * 
	 * @param lmkTak
	 *            LMK下加密的TAK
	 * @param lmkTmk
	 *            LMK下加密的TMK
	 * 
	 * @return TMK下加密的TAK
	 * 
	 */
	public String[] exportTmkTak(String lmkTak, String lmkTmk)
			throws HsmException;

	/**
	 * 由LMK下加密的ZAK导出为ZMK下加密
	 * 
	 * @param lmkZak
	 *            LMK下加密的ZAK
	 * @param lmkZmk
	 *            LMK下加密的ZMK
	 * @return ZMK下加密的zak
	 */
	public String[] exportZmkZak(String lmkZak, String lmkZmk)
			throws HsmException;

	/**
	 * 由LMK下加密的ZPK导出为ZMK下加密
	 * 
	 * @param lmkZpk
	 *            LMK下加密的ZPK
	 * @param lmkZmk
	 *            LMK下加密的ZMK
	 * @return ZMK下加密的zpk
	 */
	public String[] exportZmkZpk(String lmkZpk, String lmkZmk)
			throws HsmException;

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
	public String translateLmkTpkLmkZpk(String pinBlock, String accountNo,
			String lmkTpk, String lmkZpk) throws HsmException;

	/**
	 * 把一个zpk转换成另外一个zpk下加密
	 * 
	 * @param pinBlock
	 *            原zpk下机密的pinblock
	 * @param accountNo
	 * @param srcLmkZpk
	 *            原加密使用的zpk
	 * @param destLmkZpk
	 *            要转换加密的zpk
	 * 
	 *            卡号
	 * @return 新的zpk下加密的pinblock
	 * @throws HsmException
	 */
	public String translateLmkZpkLmkZpk(String pinBlock, String accountNo,
			String srcLmkZpk, String destLmkZpk) throws HsmException;

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
	public String[] translateZmkZpkLmkZpk(String zmkZpk, String lmkZmk)
			throws HsmException;

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
	public String[] translateZmkZakLmkZak(String zmkZak, String lmkZmk)
			throws HsmException;

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
	public String genCheckValue(KEY_TYPE keyType, String lmkKey)
			throws HsmException;

	/**
	 * 计算pinblock，可以使用tpk或者zpk和明文的密码进行计算pinblock
	 * 
	 * @param pin
	 *            明文的pin
	 * @param accountNo
	 *            卡号
	 * @param keyType
	 *            密钥类型
	 * @param lmkPk
	 *            zpk或者tpk
	 * @return pinblock
	 * @throws HsmException
	 */
	public String genPinBlock(String pin, String accountNo, KEY_TYPE keyType,
			String lmkPk) throws HsmException;

	/**
	 * 计算银联标准mac
	 * 
	 * @param data
	 *            需要计算mac的数据
	 * @param keyType
	 *            zak或者tak
	 * @param lmkAk
	 *            计算mac的key
	 * @return mac
	 * @throws HsmException
	 */
	public byte[] genECBMAC(byte[] data, KEY_TYPE keyType, String lmkAk)
			throws HsmException;

}
