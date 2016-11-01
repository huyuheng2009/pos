package com.yogapay.mobile.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.server.jpos.PackagerNavigator;
import com.yogapay.core.service.BizBaseService;
import com.yogapay.core.service.SaleService;
import com.yogapay.core.utils.Dao;
import com.yogapay.core.utils.StringUtil;
import com.yogapay.mobile.domain.AcqResult;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.domain.Receive;
import com.yogapay.mobile.domain.UserInfo;
import com.yogapay.mobile.enums.TransType;
import com.yogapay.mobile.utils.Commons;
import com.yogapay.mobile.utils.FinanceUtil;
@Service
public class CodFinanceService {
	@Resource
	private Dao dao;
	@Resource
	private SaleService saleService;
	@Resource
	private BizBaseService bizBaseService;
	ResponseMsg rm = null;
	
	// 收款
	public AcqResult payment(Receive receive, BigDecimal amount) throws Exception {
		String sqlKey = "select * from secret_key where device_id=? and device_type='qpos'";
		Map<String, Object> paramsKey = dao.findFirst(sqlKey, receive.getDeviceNo());
		String bdk = paramsKey.get("bdk").toString();
		String trackKsn = receive.getTrackKsn();
		String pinKsn = receive.getPinKsn();
		String track2Data = Commons.decodeTracks(trackKsn, bdk,  receive.getTrack());
		String cardNo = null;
		if(track2Data != null){
			cardNo =track2Data.substring(0, track2Data.indexOf("="));
		}
		CardBin cb = cardBin(cardNo);
		String pinBlock = null;
		if(StringUtils.isNotEmpty(receive.getPin()) && StringUtils.isNotEmpty(receive.getPinKsn())){
			pinBlock = Commons.decodePinBlock(pinKsn, bdk, cardNo,  receive.getPin());
		}
		rm = pay("000000", amount, track2Data, pinBlock);
		AcqResult ar = new AcqResult();
		// 交易回来的状态修改
		if (rm != null && StringUtils.isNotEmpty(rm.getAcqResponseCode())) {
			String f39 = rm.getAcqResponseCode();
			ar.setAmount(amount);
			String f39Msg = acqResponseMsg(rm.getIosMsg());
			ar.setCb(cb);
			ar.setIsoMsg(rm.getIosMsg());
			ar.setResponseCode(f39);
			ar.setResponseMsg(f39Msg);
		}else {
			ar.setCb(cb);
			ar.setResponseCode("D1");
			ar.setResponseMsg("网络超时");
		}	
		return ar;
	}
	
	public ResponseMsg pay(String transCode, BigDecimal amount,
			String track2Data, String pinBlock)
			throws Exception {
		ISOMsg isoMsg = new ISOMsg();
		isoMsg.setPackager(PackagerNavigator.COMMON_PACKAGER);
		isoMsg.setMTI("0200");
		isoMsg.set(2, FinanceUtil.getAccountNo(track2Data));
		String s = System.currentTimeMillis() + "";
		s = s.substring(s.length() - 6, s.length());
		if (transCode.equals("000000")) {
			String str = amount.multiply(new BigDecimal("100")).setScale(0)
					.toString();
			isoMsg.set(4, StringUtil.stringFillLeftZero(str, 12));
		}
		isoMsg.set(3, "000000");
		isoMsg.set(11, s);
		isoMsg.set(22, "021");
		isoMsg.set(35, track2Data);
		isoMsg.set(41, "10000175");
		isoMsg.set(42, "270440053991236");
		isoMsg.set(60, "22000001");
		if(StringUtils.isNotEmpty(pinBlock)){
			setPin(isoMsg, pinBlock);
		}else{
			isoMsg.set(52, pinBlock);
		}
			
		return saleService.doTrx(isoMsg);
	}
	
	public void setPin(ISOMsg isoRes, String pinBlock) throws Exception {
		String accountNo = isoRes.getString(2);
		String terminalNo = isoRes.getString(41);
		if (StringUtils.isEmpty(accountNo)) {
			accountNo = isoRes.getString(35);
			accountNo = accountNo.substring(0, accountNo.indexOf("="));
		}
		PosTerminal posTerminal = bizBaseService.getPosTerminal(isoRes.getString(42),
				isoRes.getString(41));
		String clearPk = JCEHandler.decryptData(posTerminal.getTmkTpk(),
				posTerminal.getTmk());
		pinBlock = FinanceUtil.pinBlock(accountNo, pinBlock, clearPk);
		isoRes.set(52, pinBlock);
	}
	
	//获取卡bin信息
		public CardBin cardBin(String cardNo) {
			String sql = "select * from card_bin c  where  c.card_length = length(?) AND c.verify_code = left(?,  c.verify_length)  ";
			try {
				CardBin cb = dao.findFirst(CardBin.class, sql, new Object[] {
						cardNo, cardNo });
				if (null == cb) {
					cb = new CardBin();
				}
				cb.setCardNo(cardNo);
				return cb;
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		}
		
		private static Map<String, String> f39Maps = null;

		// 获取收单机构返回码
		public String acqResponseMsg(ISOMsg isoMsg) throws SQLException {
			if (null == f39Maps) {
				f39Maps = new HashMap<String, String>();
				String sql = "select * from response_code";
				List<Map<String, Object>> result = dao.find(sql);
				for (Map<String, Object> m : result) {
					String code = m.get("response_code").toString();
					String text = m.get("response_text").toString();
					f39Maps.put(code, text);
				}
			}

			if (null != isoMsg) {
				if (StringUtils.isNotEmpty(isoMsg.getString(39))) {
					String f39 = isoMsg.getString(39);
					return f39Maps.get(f39);
				}
			}
			return null;
		}
}
