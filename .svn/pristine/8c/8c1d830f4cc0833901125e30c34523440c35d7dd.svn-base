package com.yogapay.mobile.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.security.rsa.RSAUtils;
import com.yogapay.core.server.jpos.PackagerNavigator;
import com.yogapay.core.service.QueryService;
import com.yogapay.core.service.SaleService;
import com.yogapay.core.utils.Dao;
import com.yogapay.core.utils.StringUtil;
import com.yogapay.mobile.domain.AcqResult;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.domain.Alipay;
import com.yogapay.mobile.domain.Credit;
import com.yogapay.mobile.domain.House;
import com.yogapay.mobile.domain.Receive;
import com.yogapay.mobile.domain.UserCard;
import com.yogapay.mobile.domain.UserInfo;
import com.yogapay.mobile.enums.TransType;
import com.yogapay.mobile.utils.Commons;
import com.yogapay.mobile.utils.Constants;
import com.yogapay.mobile.utils.FinanceUtil;

/**
 * 负责客户端请求响应
 * 
 * @author donjek
 * 
 */
@Service
public class MobileFinanceService {

	@Resource
	private Dao dao;
	@Resource
	private SaleService saleService;
	@Resource
	private QueryService queryService;

	/**
	 * 余额查询
	 * 
	 * @throws Exception
	 */
	public AcqResult query(Receive receive) throws Exception {
		Date settleDate = settle();
		AcqResult ar = createOrder(receive, TransType.余额查询, new BigDecimal(0),
				new BigDecimal(0), null, null, null, null);

		ar.setSettleTime(settleDate);
		return ar;

	}

	public void setPin(ISOMsg isoRes, String pinBlock) throws Exception {
		String accountNo = isoRes.getString(2);
		if (StringUtils.isEmpty(accountNo)) {
			accountNo = isoRes.getString(35);
			accountNo = accountNo.substring(0, accountNo.indexOf("="));
		}
		String clearPk = JCEHandler.decryptData("BA7A0753714636CD",
				"8888888899999999");
		pinBlock = FinanceUtil.pinBlock(accountNo, pinBlock, clearPk);
		isoRes.set(52, pinBlock);
	}

	// 交房租
	public AcqResult house(Receive receive, House house) throws Exception {
		BigDecimal fee = fee(receive, house.getAmount());

		Date settleDate = settle();
		// 创建消费
		BigDecimal transAmount = fee.add(house.getAmount());
		CardBin cb = cardBin(house.getCardNo());
		AcqResult ar = createOrder(receive, TransType.交房租, transAmount, fee,
				house.getAmount(), house.getCardNo(), house.getOwner(),
				cb.getBankName());

		ar.setSettleTime(settleDate);
		ar.setFee(fee);
		return ar;

	}

	// 收款
	public AcqResult payment(Receive receive, BigDecimal amount) throws Exception {
		BigDecimal fee = fee(receive, amount);

		Date settleDate = settle();
		// 创建消费
		BigDecimal transAmount = fee.add(amount);
		
		String sql = "select * from user_info where user_name = ? and app_name=?";
		UserInfo result = dao.findFirst(UserInfo.class, sql, new Object[] {
			receive.getUserName(), receive.getAppName() });

		CardBin cb = cardBin(result.getSettleNo());
		
		AcqResult ar = createOrder(receive, TransType.收款, transAmount, fee,
				amount, result.getSettleNo(), result.getSettleName(),
				cb.getBankName());

		ar.setSettleTime(settleDate);
		ar.setFee(fee);
		return ar;

	}

	// 支付宝充值
	public AcqResult alipay(Receive receive, Alipay alipay) throws Exception {
		Date settleDate = settle();
		BigDecimal fee = fee(receive, alipay.getAmount());
		BigDecimal transAmount = fee.add(alipay.getAmount());
		// 创建消费
		AcqResult ar = createOrder(receive, TransType.支付宝充值, transAmount, fee,
				alipay.getAmount(), alipay.getAccountNo(),
				alipay.getAccountName(), null);
		ar.setSettleTime(settleDate);
		ar.setFee(fee);
		return ar;

	}

	// 信用卡还款
	public AcqResult credit(Receive receive, Credit credit) throws Exception {
		Date settleDate = settle();
		BigDecimal fee = fee(receive, credit.getAmount());
		BigDecimal transAmount = fee.add(credit.getAmount());
		CardBin cb = cardBin(credit.getCardNo());
		// 创建消费
		AcqResult ar = createOrder(receive, TransType.信用卡还款, transAmount, fee,
				credit.getAmount(), credit.getCardNo(), credit.getOwner(),
				cb.getBankName());
		ar.setSettleTime(settleDate);
		ar.setFee(fee);
		return ar;

	}

	/**
	 * 计算商户手续费
	 * 
	 * @param receive
	 * @return
	 * @throws SQLException
	 */
	public BigDecimal fee(Receive receive, BigDecimal amount)
			throws SQLException {
		receive.getUserName();
		String sql = "select * from user_info where user_name=?";
		Map<String, Object> params = dao.findFirst(sql, receive.getUserName());
		String rate = params.get("rate").toString();
		return fee(rate, amount);
	}

	// 根据商户设置计算结算日期
	public Date settle() {
		Integer settleDays = 1;
		String settleTime = "17:30";
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(settleTime.split(":")[0]));
		c.set(Calendar.MINUTE, Integer.valueOf(settleTime.split(":")[1]));
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + settleDays);
		Date d = c.getTime();
		if (c.get(Calendar.DAY_OF_WEEK) == 7) {
			c.set(Calendar.DATE, day + 3);
			d = c.getTime();
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 8) {
			c.setTime(d);
			c.set(Calendar.DATE, day + 2);
			d = c.getTime();
		}

		return d;
	}

	public ResponseMsg pay(String transCode, BigDecimal amount,
			String track2Data, AcqTerminal at, String pinBlock)
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
		isoMsg.set(41, at.getTerminalNo());
		isoMsg.set(42, at.getMerchantNo());
		isoMsg.set(60, "22" + at.getBatchNo());
		setPin(isoMsg, pinBlock);
		if (transCode.equals("000000")) {
			return saleService.doTrx(isoMsg);
		}
		return queryService.doTrx(isoMsg);
	}

	/**
	 * 创建订单并进行消费
	 * 
	 * @param receive
	 *            接收请求对象
	 * @param tt
	 *            交易类型，如果为查询，后两项为空
	 * @param amount
	 *            交易金额
	 * @throws Exception
	 */
	public AcqResult createOrder(Receive receive, TransType tt,
			BigDecimal amount, BigDecimal fee, BigDecimal settleAmount,
			String settleAccount, String settleName, String settleBank)
			throws Exception {

		if (StringUtils.isEmpty(receive.getPin())) {
			receive.setPin("42ADEEF55243ACACBD4FEA803541229CD44288EA9EB87660ABC451283DA50FB28425D6880B5FD4FD41FC9EEB109EA250C1F28E146BA8C708C72670F2EBC160445ACA0F7F1C1816EA4733ED32D62EE91067A371864C70CCAE4526825398510F7661FFA297CF5E9877F9F177EAE2B75BF0A14229CA834E54640D47B1D24278B043");
		}
		AcqResult ar = new AcqResult();
		String track2Data = "";
		String cardHolder = "";
		String sqlKey = "select * from secret_key where device_id=? and device_type='qpos'";
		Map<String, Object> paramsKey = dao.findFirst(sqlKey, receive.getDeviceNo());
		
		if (!StringUtils.isEmpty(receive.getTrack())) {
			if (StringUtils.isNotEmpty(receive.getDeviceNo())) {
				if (receive.getDeviceName().equals("qrpos")) {
					// TODO
					String bdk = paramsKey.get("bdk").toString();
					byte[] bdkByte = ISOUtil.hex2byte(bdk);
					String trackksn = paramsKey.get("track_ksn").toString();
					byte[] trackksnByte = ISOUtil.hex2byte(trackksn);
					byte[] ipekByte = Commons.generateIPEK(trackksnByte,
							bdkByte);
					byte[] dukptByte = Commons.getDUKPTKey(trackksnByte,
							ipekByte);
					byte[] dataKeyVariantByte = Commons
							.getDataKeyVariant(dukptByte);
					int count = receive.getTrack().length() / 16;
					String tracks2 = "";
					for (int i = 0; i < count; i++) {
						String temp = receive.getTrack().substring(i * 16, (i + 1) * 16);
						tracks2 += ISOUtil.hexString(JCEHandler.decryptData(
								ISOUtil.hex2byte(temp), dataKeyVariantByte));
					}
					track2Data = tracks2.substring(0, tracks2.indexOf("F"))
							.replace("D", "=");
				}

			} else if (StringUtils.isEmpty(receive.getRandomNumber())) {
				track2Data = decodedData(StringUtils.trim(receive.getTrack()));
			} else if (receive.getRandomNumber().equals("123456789")) {
				// 中磁
				String track = receive.getTrack();
				String data = "";
				if ((track.length() % 16) != 0) {
					int tmp = track.length() % 16;
					int data_len = track.length() - 80 - tmp;
					data = track.substring(32, 32 + data_len);
				} else {
					int data_len = track.length() - 80;
					data = track.substring(32, 32 + data_len);
				}
				byte[] b1 = Utils.hexStringToBytes(data);
				byte[] b2 = Utils.hexStringToBytes(Constants.IMAG_KEY);
				String result2 = "";
				try {
					result2 = new String(Utils.decodeTripleDES(b1, b2));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (result2.indexOf("^") > 1) {
					// 一磁道
					String track1 = result2.substring(result2.indexOf("%") + 1,
							result2.indexOf("?"));
					cardHolder = track1.substring(track1.indexOf("^") + 1,
							track1.lastIndexOf("^"));
					cardHolder = cardHolder.trim();
				}

				track2Data = result2.substring(result2.indexOf(";") + 1,
						result2.lastIndexOf("?"));

			} else {
				// 艾创
				byte[] b1 = ISOUtil.hex2byte(receive.getRandomNumber());
				byte[] b2 = ISOUtil.hex2byte("FFFFFFFFFFFFFFFF");
				String left = JCEHandler.encryptData(receive.getRandomNumber(),
						Constants.ITRON_KEY);
				String right = ISOUtil.hexString(ISOUtil.xor(b1, b2));
				right = JCEHandler.encryptData(
						ISOUtil.hexString(ISOUtil.xor(b1, b2)),
						Constants.ITRON_KEY);
				String result = JCEHandler.decryptData(
						StringUtils.trim(receive.getTrack()), left + right);
				track2Data = result.substring(0, result.indexOf("F")).replace(
						"D", "=");

			}
		} else {
			String sql = "select * from user_card where user_name=? and card_no=?";
			List<UserCard> have = dao.find(UserCard.class, sql, new Object[] {
					receive.getUserName(), receive.getCardNo() });
			if (have.size() == 1) {
				track2Data = have.get(0).getTrack();
			}
		}
		String pinBlock = "";
		if (StringUtils.isNotEmpty(receive.getDeviceNo())) {
			if (receive.getDeviceName().equals("qrpos")) {
				String accountNo = track2Data.substring(0,
						track2Data.indexOf("="));
				String bdk = paramsKey.get("bdk").toString();
				byte[] bdkByte = ISOUtil.hex2byte(bdk);
				String pinKsn = paramsKey.get("pin_ksn").toString();
				byte[] pinKsnByte = ISOUtil.hex2byte(pinKsn);
				byte[] ipekByte = Commons.generateIPEK(pinKsnByte, bdkByte);
				byte[] dukptByte = Commons.getDUKPTKey(pinKsnByte, ipekByte);
				byte[] pinKeyVariant = Commons.getPinKeyVariant(dukptByte);
				String clearPk = ISOUtil.hexString(pinKeyVariant);
				pinBlock = FinanceUtil.unPinBlock(accountNo, receive.getPin(),
						clearPk);
			}
		} else {
			pinBlock = decodedData(StringUtils.trim(receive.getPin()));
		}

		AcqTerminal at = getTerminal(amount, receive.getAppName());

		CardBin cb = cardBin(getAccountNo(track2Data));
		ResponseMsg rm = null;
		boolean test = receive.getUserName().equals(Constants.TEST_USERNAME) ? true
				: false;

		// 手续费规则
		String sql = "select * from user_info where user_name=?";
		Map<String, Object> params = dao.findFirst(sql, receive.getUserName());
		String rate = params.get("rate").toString();
		String rates[] = rate.split("-");
		rate = rates[0] + "%-" + rates[1] + "元";
		// 插入
		sql = "insert into trans_info_hipay(merchant_fee_rate,app_name,user_name,sync_no,trans_type,trans_status,trans_amount,card_no,create_time,merchant_fee,card_holder,settle_amount,settle_account,settle_name,settle_bank) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		dao.update(
				sql,
				new Object[] { rate, receive.getAppName(),
						receive.getUserName(), receive.getSync(),
						tt.toString(), "初始化", amount, cb.getCardNo(),
						new Date(), fee, cardHolder, settleAmount,
						settleAccount, settleName, settleBank });
		try {
			if (test) {
				ar.setCb(cb);
				if (null == amount) {
					ar.setAmount(new BigDecimal("800"));
				} else {
					ar.setAmount(amount);
				}
				ar.setResponseCode("00");
				ar.setResponseMsg("成功");
				return ar;
			}
			if (TransType.余额查询 == tt) {
				rm = pay("310000", amount, track2Data, at, pinBlock);
			} else {
				rm = pay("000000", amount, track2Data, at, pinBlock);
			}
		} catch (Exception e) {
			ar.setResponseCode("D1");
			ar.setResponseMsg("网络超时");
			throw e;
		} finally {
			ar.setCb(cb);
		}

		// 交易回来的状态修改
		if (rm != null && StringUtils.isNotEmpty(rm.getAcqResponseCode())) {
			String f39 = rm.getAcqResponseCode();
			if (f39.equals("00")) {
				if (TransType.余额查询 == tt) {
					String f54 = rm.getIosMsg().getString(54);
					f54 = f54.substring(8, f54.length());
					BigDecimal b = new BigDecimal(f54);
					ar.setAmount(b.divide(new BigDecimal(100)));
				} else {
					ar.setAmount(amount);
				}

			} else {
			}
			String f39Msg = acqResponseMsg(rm.getIosMsg());
			ar.setCb(cb);
			ar.setIsoMsg(rm.getIosMsg());
			ar.setResponseCode(f39);
			ar.setResponseMsg(f39Msg);

		} else {
			ar.setCb(cb);
			ar.setResponseCode("D1");
			ar.setResponseMsg("网络超时");
		}

		if (null != ar) {
			sql = "update trans_info_hipay set trans_status=?,order_no=? ,acq_response_code=?,acq_response_msg=? where sync_no=? and card_no=?";
			if (ar.getResponseCode().equals("00")) {
				dao.update(
						sql,
						new Object[] { "已成功", rm.getOrderNo(),
								ar.getResponseCode(), ar.getResponseMsg(),
								receive.getSync(), cb.getCardNo() });
			} else {
				dao.update(
						sql,
						new Object[] { "已失败", rm.getOrderNo(),
								ar.getResponseCode(), ar.getResponseMsg(),
								receive.getSync(), cb.getCardNo() });
			}

		}

		return ar;
	}

	// 解密数据
	public String decodedData(String ecodedData) {
		try {
			byte[] decodedData = RSAUtils.decryptByPrivateKey(
					ISOUtil.hex2byte(ecodedData), Constants.RSA_PRI_KEY);
			return new String(decodedData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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

	// 根据二磁道数据获取卡号
	public String getAccountNo(String track2data) {
		if (StringUtils.isNotEmpty(track2data)) {
			track2data = track2data.substring(0, track2data.indexOf("="));
			return track2data;
		} else
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

	// 根据syncno查询id
	public Long getTransId(String syncNo) throws SQLException {
		String sql = "select id from trans_info where sync_no=?";
		List<Map<String, Object>> result = dao.find(sql, syncNo);
		if (null == result)
			return null;
		if (StringUtils.isNotEmpty(result.get(0).get("id").toString())) {
			return Long.valueOf(result.get(0).get("id").toString());
		}
		return null;
	}

	// 手续费计算
	public BigDecimal fee(String rate, BigDecimal amount) {
		BigDecimal bd = null;
		// 封顶手续费 0.78~26
		if (rate.indexOf("-") > -1) {
			String[] rates = rate.split("-");
			BigDecimal top = new BigDecimal(rates[1]);
			bd = amount.multiply(new BigDecimal(rates[0]).movePointLeft(2));
			if (top.compareTo(bd) == -1) {
				bd = top;
			}
		} else {
			// 固定手续费 0.38
			bd = amount.multiply(new BigDecimal(rate).movePointLeft(2));
		}
		bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
		return bd;
	}

	// 根据交易金额选择合适的收单商户
	public AcqTerminal getTerminal(BigDecimal transAmount, String appName) {
		AcqTerminal at = new AcqTerminal();
		if (appName.toUpperCase().equals("HIPAY")) {
			at.setMerchantNo("ZZZZZZZZZZZZ001");
			at.setTerminalNo("ZZZZZ001");
			at.setTmk("8888888899999999");
			at.setTmkTak("BA7A0753714636CD");
			at.setTmkTpk("BA7A0753714636CD");
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			at.setBatchNo(sdf.format(new Date()));
		} else {
			at.setMerchantNo("270440053991209");
			at.setTerminalNo("10000148");
			at.setTmk("9827400AB3001200");
			at.setTmkTak("98B6971624CD07A5");
			at.setTmkTpk("9CC3DAC84FFF5E60");
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			at.setBatchNo(sdf.format(new Date()));
		}
		return at;
	}

	public static void main(String[] args) {

	}
}
