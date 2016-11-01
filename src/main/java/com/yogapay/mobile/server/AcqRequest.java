package com.yogapay.mobile.server;

import com.yogapay.core.exception.HsmException;
import com.yogapay.core.security.IHsm;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.security.Vhsm;
import com.yogapay.core.server.PackagerNavigator;
import com.yogapay.core.server.jpos.HEXChannel;
import com.yogapay.core.server.jpos.Packager;
import com.yogapay.core.server.jpos.Utils;
import com.yogapay.core.utils.Constants;
import com.yogapay.core.utils.StringUtil;
import com.yogapay.mobile.domain.AcqTerminal;
import org.apache.commons.lang.StringUtils;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AcqRequest {
	private static final Logger log = LoggerFactory.getLogger(AcqRequest.class);
	private IHsm cryp = new Vhsm();

	/**
	 * 余额查询
	 * 
	 * @return ISOMsg
	 * @throws ISOException
	 * @throws HsmException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws Exception
	 */
	public ISOMsg query(ISOMsg req, String track2Data, String track3Data, String pin,
			AcqTerminal at) throws ISOException, HsmException,
			NotFoundException, IOException {
		ISOMsg request = getMsg(at);
		request.setMTI("0200");
		request.set(2, req.getString(2));
		request.set(3, "310000");
		request.set(11, at.getVoucherNo());
		if(StringUtils.isNotEmpty(req.getString(14))){
			request.set(14, req.getString(14));
		}
		request.set(22, req.getString(22));
		if(StringUtils.isNotEmpty(req.getString(23))){
			request.set(23, req.getString(23));
		}
		request.set(25, "00");
		request.set(26, req.getString(26));
		request.set(35, track2Data);
		if (StringUtils.isNotEmpty(track3Data)) {
			request.set(36, track3Data);
		}
		request.set(41, at.getTerminalNo());
		request.set(42, at.getMerchantNo());
		request.set(49, "156");
		request.set(53, "2600000000000000");
		if(StringUtils.isNotEmpty(req.getString(55))){
			request.set(55, req.getString(55));
		}
		request.set(60, "01" + at.getBatchNo()+"060");
		
		
		request.set(52, ISOUtil.hex2byte(setPin(request, pin, at)));
		request.set(64, new byte[8]);
		// TODO，此方法传入的是加密的tak，现在传的是明文tak有问题，需要在 getnECBMAC方法中将解密代码去掉
		String tak = JCEHandler.decryptData(at.getTmkTak(), at.getTmk());
		byte[] mac = cryp.genECBMAC(request.pack(), IHsm.KEY_TYPE.TAK, tak);
		request.set(64, ISOUtil.hexString(mac));
        return send(request, at.getAcqName());
	}

	// 消费撤销
	public ISOMsg reversal(String track2Data, String track3Data, String pin,
			BigDecimal srcAmount, String srcReferenceNo, String srcAuthNo,
			String srcBatchNo, String srcSerialNo, AcqTerminal at)
			throws Exception {
		ISOMsg request = getMsg(at);
		request.setMTI("0200");
		// 交易金额同原始交易应答报文
		String str = srcAmount.multiply(new BigDecimal("100")).setScale(0)
				.toString();
		request.set(3, "200000");
		request.set(4, StringUtil.stringFillLeftZero(str, 12));
		request.set(11, at.getVoucherNo());
		request.set(22, "021");
		request.set(35, track2Data);
		if (StringUtils.isNotEmpty(track3Data)) {
			request.set(36, track3Data);
		}
		// 检索参考号 37域
		request.set(37, srcReferenceNo);
		// 授权码
		if (StringUtils.isNotEmpty(str)) {
			request.set(38, srcAuthNo);
		}
		request.set(41, at.getTerminalNo());
		request.set(42, at.getMerchantNo());
		request.set(52, ISOUtil.hex2byte(setPin(request, pin, at)));
		request.set(60, "23" + at.getBatchNo());
		// 原批次号60域后6位+原流水号11域
		request.set(61, srcBatchNo + srcSerialNo);
		return send(request, at.getAcqName());
	}

	/**
	 * 冲正
	 * 
	 * @return
	 * @throws IOException
	 */
	public ISOMsg rollback(ISOMsg req,String track2Data, String track3Data,
			String srcTransCode, BigDecimal srcAmount, String srcAuthNo,
			String srcSerialNo, AcqTerminal at) throws ISOException,
			NotFoundException, SQLException, IOException {
		ISOMsg request = getMsg(at);
		request.setMTI("0400");
		String str = srcAmount.multiply(new BigDecimal("100")).setScale(0)
				.toString();
		request.set(3, srcTransCode);
		// 原交易金额
		request.set(4, StringUtil.stringFillLeftZero(str, 12));
		// 原流水号
		request.set(11, srcSerialNo);
		request.set(22, "021");
		// 授权码
		if (StringUtils.isNotEmpty(str)) {
			request.set(38, srcAuthNo);
		}
        request.set(25,req.getString(25));
		// 应答码,冲中原因
		request.set(39, "98");
		request.set(41, at.getTerminalNo());
		request.set(42, at.getMerchantNo());
		// 交易类型编码原来的，
		request.set(60, "22" + at.getBatchNo());
        String f61 = req.getString(61);
        if(StringUtils.isNotBlank(f61)){
            f61 = at.getBatchNo()+at.getVoucherNo();
            request.set(61,f61);
        }
		return send(request, at.getAcqName());
	}

	/**
	 * 退货
	 * 
	 * @return
	 * @throws IOException
	 */
	public ISOMsg refund(String track2Data, String track3Data,
			BigDecimal amount, String srcAuthNo, String srcBatchNo,
			String srcSerialNo, String srcRefNo, String srcDate, AcqTerminal at)
			throws ISOException, NotFoundException, SQLException, IOException {
		ISOMsg request = getMsg(at);
		request.setMTI("0220");
		String str = amount.multiply(new BigDecimal("100")).setScale(0)
				.toString();
		request.set(3, "200000");
		// 退货金额
		request.set(4, StringUtil.stringFillLeftZero(str, 12));
		// 流水号
		request.set(11, at.getVoucherNo());
//		request.set(22, "021");
		request.set(35, track2Data);
		if (StringUtils.isNotEmpty(track3Data)) {
			request.set(36, track3Data);
		}
		request.set(37, srcRefNo);
		// 授权码
		if (StringUtils.isNotEmpty(str)) {
			request.set(38, srcAuthNo);
		}
		request.set(41, at.getTerminalNo());
		request.set(42, at.getMerchantNo());
		request.set(60, "25" + at.getBatchNo());
		request.set(61, srcBatchNo + srcSerialNo + srcDate);
        return send(request, at.getAcqName());

/*        {
            request.setResponseMTI();
            request.set(39,"40");
        }
		return request;*/
	}

	/**
	 * 消费
	 * 
	 * @return ISOMsg
	 * @throws ISOException
	 * @throws HsmException
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws Exception
	 */
	public ISOMsg sale(ISOMsg req, String track2Data, String track3Data, String pin,
			BigDecimal transAmount, AcqTerminal at)
			throws ISOException, HsmException, NotFoundException, IOException {
		ISOMsg request = getMsg(at);
		request.setMTI("0200");
		request.set(2, req.getString(2));
		request.set(3, "000000");
		String str = transAmount.multiply(new BigDecimal("100")).setScale(0)
				.toString();
		request.set(4, StringUtil.stringFillLeftZero(str, 12));
		request.set(11, at.getVoucherNo());
		if(StringUtils.isNotEmpty(req.getString(14))){
			request.set(14, req.getString(14));
		}
		request.set(22, req.getString(22));
		if(StringUtils.isNotEmpty(req.getString(23))){
			request.set(23, req.getString(23));
		}
		request.set(25, "00");
		request.set(26, req.getString(26));
		request.set(35, track2Data);
		if (StringUtils.isNotEmpty(track3Data)) {
			request.set(36, track3Data);
		}
		request.set(41, at.getTerminalNo());
		request.set(42, at.getMerchantNo());
		request.set(49, "156");
		
		request.set(52, ISOUtil.hex2byte(setPin(request, pin, at)));
		request.set(53, "2600000000000000");
		if(StringUtils.isNotEmpty(req.getString(55))){
			request.set(55, req.getString(55));
		}
		request.set(60, "22" + at.getBatchNo());
		request.set(52, setPin(request, pin, at));
		request.set(64, new byte[8]);
		// TODO，此方法传入的是加密的tak，现在传的是明文tak有问题，需要在 getnECBMAC方法中将解密代码去掉
		String tak = JCEHandler.decryptData(at.getTmkTak(), at.getTmk());
		byte[] mac = cryp.genECBMAC(request.pack(), IHsm.KEY_TYPE.TAK, tak);
		request.set(64, ISOUtil.hexString(mac));
		ISOMsg response = null;
        response = send(request, at.getAcqName());
		return response;
	}
	

	/**
	 * 将请求报文发送到收单机构
	 * 
	 * @param request
	 *            请求报文
	 * @return 相应报文
	 * @throws NotFoundException
	 * @throws ISOException
	 * @throws IOException
	 * @throws Exception
	 */
	public ISOMsg send(ISOMsg request, String acqName) throws NotFoundException, ISOException,
			IOException {
		log.info(Utils.dump(request));
		ISOMsg response = null;
		BaseChannel channel = null;

		//交易分发
            if(Constants.EEEPAY.equals(acqName)){
                channel = new HEXChannel("120.132.177.198", 4000,
                        PackagerNavigator.COMMON_PACKAGER);
/*                channel = new HEXChannel("120.132.177.194", 4000,
                        PackagerNavigator.COMMON_PACKAGER);*/
                channel.setHeader(ISOUtil.hex2byte("6000060000602200000000"));
                channel.setTimeout(50000);
                channel.connect();
                channel.setLogger(null, null);
            }else if(Constants.HKRT.equals(acqName)){
                channel = new HEXChannel("127.0.0.1", 5555,
                        PackagerNavigator.HKRT_PACKAGER);
                channel.setHeader(ISOUtil.hex2byte("6000060000602200000000"));
                channel.setTimeout(50000);
                channel.connect();
                channel.setLogger(null, null);
            }else if(Constants.ZFT.equals(acqName)){
                channel = new HEXChannel("127.0.0.1", 7985,
                        PackagerNavigator.ZFT_PACKAGER);
                channel.setHeader(ISOUtil.hex2byte("6000040000000000000000"));
                channel.setTimeout(50000);
                channel.connect();
                channel.setLogger(null, null);
            }else{
                channel = new HEXChannel("127.0.0.1", 6666,
                        PackagerNavigator.COMMON_PACKAGER);
                channel.setHeader(ISOUtil.hex2byte("6000060000603000000000"));
                channel.setTimeout(50000);
                channel.connect();
                channel.setLogger(null, null);
            }

            // 进行对相应的预处理
            response = makeResponse(request);

            channel.send(request);

            response = channel.receive();

		// response.dump(System.out, "");
		if (null != response && response.hasField(56))
			log.info(new String(response.getBytes(56), "GBK"));
		
		log.info(Utils.dump(response));
		return response;
	}

	/**
	 * 将请求报文发送到收单机构
	 * 
	 * @param request
	 *            请求报文
	 * @return 相应报文
	 * @throws NotFoundException
	 * @throws ISOException
	 * @throws IOException
	 * @throws Exception
	 */
	public ISOMsg sendTest(ISOMsg request) throws NotFoundException,
			ISOException, IOException {

		if (1 == 1) {
			request.set(13, new SimpleDateFormat("MMdd").format(new Date()));
			request.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
			request.set(37, "120203529987");
			request.set(38, "678121");
			request.set(39, "00");
			request.set(54, "1002156C000189763915");
			return request;
		}

		// ////////////
		BaseChannel channel = new HEXChannel("115.28.36.50", 4000,
				PackagerNavigator.COMMON_PACKAGER);
		channel.setHeader(ISOUtil.hex2byte("6000060000602200000000"));
		channel.connect();
		// 进行对相应的预处理
		ISOMsg response = makeResponse(request);
		channel.send(request);
		response = channel.receive();
		if (null != response && response.hasField(56))
			log.info(new String(response.getBytes(56), "GBK"));
		return response;
	}

	public static Packager getPackager() {
		Packager pack = null;
		try {
			pack = new Packager();
			SimpleConfiguration cfg = new SimpleConfiguration();
			cfg.put("packager-config", "classpath:packager/chinaums_pos.xml");
			pack.setConfiguration(cfg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pack;
	}

	/**
	 * 对响应的报文进行处理，以便于自动设置响应的mti，如请求0200，响应会自动设置成0210
	 * 
	 * @param isoReq
	 *            请求报文
	 * @return 响应报文
	 * @throws ISOException
	 */
	public ISOMsg makeResponse(ISOMsg isoReq) throws ISOException {
		ISOMsg response = new ISOMsg();
		// 先设置mti，后面会根据这个mti进行转换成响应的mti
		response.setMTI(isoReq.getMTI());
		response.setResponseMTI();
		return response;
	}

	// 根据终端tpk密钥设置收单机构需要的pinblock
	public String setPin(ISOMsg isoRes, String pin, AcqTerminal at) {
		String accountNo = isoRes.getString(2);
		if (StringUtils.isEmpty(accountNo)) {
			accountNo = isoRes.getString(35);
			accountNo = accountNo.substring(0, accountNo.indexOf("="));
		}
		accountNo = accountNo.substring(accountNo.length() - 13,
				accountNo.length() - 1);
		// 将卡号与密码进行异或
		byte[] x = ISOUtil.xor(ISOUtil.hex2byte("06" + pin + "FFFFFFFF"),
				ISOUtil.hex2byte("0000" + accountNo));
		String tpk = JCEHandler.decryptData(at.getTmkTpk(), at.getTmk());

		String pinblock = JCEHandler.encryptData(ISOUtil.hexString(x), tpk);
		
		return pinblock;
	}

	public ISOMsg getMsg(AcqTerminal at) {
		ISOMsg request = new ISOMsg();
		if(Constants.HKRT.equals(at.getAcqName())){
			request.setPackager(PackagerNavigator.HKRT_PACKAGER);
		}else{
			request.setPackager(PackagerNavigator.COMMON_PACKAGER);
		}
		return request;
	}

	public static void main(String[] args) {
		AcqRequest ar = new AcqRequest();
		try {
			String track2Data = "6222521311350221=1509106442";
			BigDecimal transAmount = new BigDecimal("3588.00");

			AcqTerminal at = new AcqTerminal();
			at.setMerchantNo("100000000000000");
			at.setTerminalNo("10000001");
			at.setVoucherNo("100015");
			at.setBatchNo("100000");
			ar.reversal(track2Data, null, null, transAmount, "062995973205",
					"475609", "100000", "100013", at);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
