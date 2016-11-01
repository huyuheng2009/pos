package com.yogapay.core.server.op;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.yogapay.core.domain.PosMerchant;
import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.server.GenSyncNo;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.server.tlv.GBKTLVList;
import com.yogapay.core.service.PosService;
import com.yogapay.mobile.service.CommonService;

public class Init extends OpBase implements IOperator {

	@Override
	public ISOMsg doTrx(ISOMsg request) throws Exception {
		// TODO Auto-generated method stub
		ISOMsg response = new ISOMsg();
		response.setMTI(request.getMTI());
		response.setResponseMTI();

		// byte[] header = response.getHeader();
		// header[7] = 0x09;
		// response.setHeader(header);

		byte[] a = request.getBytes(62);
		String sn = new String(a);
		sn = sn.substring(sn.length() - 8, sn.length());
		// 交易流水号
		response.set(11, request.getString(11));
		// 时间
		response.set(12, ISODate.getTime(new Date()));
		// 日期
		response.set(13, ISODate.getDate(new Date()));

		// 参考号12位
		String referNo = "100000000000";
		try {
			referNo = GenSyncNo.getInstance().getNextPosReferNo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.set(37, referNo);
		response.set(39, "00");
		PosService posService = applicationContext.getBean(PosService.class);
		CommonService commonService = applicationContext.getBean(CommonService.class);
		PosTerminal pt = posService.findBySn(sn);
		if (null != pt && StringUtils.isNotEmpty(pt.getMerchantNo())
				&& pt.getOpenStatus() == 1) {
			PosMerchant pm = posService.findByMno(pt.getMerchantNo());
			GBKTLVList tvlList = new GBKTLVList();
			tvlList.append("C0", "欢迎使用POS刷卡");
			tvlList.append("C1", pm.getShortName());
			tvlList.append("C2", "POS签购单");
			tvlList.append("C3", "FFFFFFFFFF");
			tvlList.append("C4", "111");
			// 小票是否打印商户名称，0不打印，1打印
			tvlList.append("C5", "1");
			// 小票打印发卡行收单行，0不打印，1打印第1位：收单行，第2位：发卡行
			tvlList.append("C6", "11");
			// 小票底部的广告语内容，GBK编码
			tvlList.append("C7", "感谢您的惠顾");
			// 小票底部的客服电话
			tvlList.append("C8", "88888888");
			// 小票是否打印广告语和客服电话，0不打印，1打印， 第1位：广告，第2位：电话
			tvlList.append("C9", "10");
			
			tvlList.append("N0", commonService.getValue("SERVER_IP"));
			tvlList.append("N1", "4000");
			tvlList.append("N2", "89898900");

			tvlList.append("T1", pt.getTerminalNo());
			tvlList.append("T2", pm.getMerchantNo());
			response.set(56, tvlList.pack().getBytes("gbk"));

			// 密钥格式为：
			// 主密钥+主密钥加密密钥+主密钥校验值
			//
			// 以3DES为例：
			// 假如TMK为全0，密钥加密密钥为全F，则下发的密钥为：
			// CAAAAF4DEAF1DBAECAAAAF4DEAF1DBAE FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
			// 8CA64DE9
			String tmk = pt.getTmk();
			String key = JCEHandler.encryptData(tmk, "7973FA538CF17843");
			key = key + "7973FA538CF17843";
			String ck = JCEHandler.encryptData("0000000000000000", tmk);
			key = key + ck.substring(0, 8);
			// key = "F616DD76F290635EFFFFFFFFFFFFFFFF82E13665";
			response.set(62, ISOUtil.hex2byte(key));
		} else {
			response.set(39, "03");
		}

		response.set(60, request.getString(60));

		return response;
	}
}
