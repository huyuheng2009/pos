package com.yogapay.core.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.yogapay.core.domain.*;
import com.yogapay.core.utils.*;

import org.apache.commons.lang.StringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yogapay.core.enums.AcqName;
import com.yogapay.core.enums.TransStatus;
import com.yogapay.core.enums.TransType;
import com.yogapay.core.exception.CardBinNotFoundException;
import com.yogapay.core.exception.RouteNotFindException;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.server.GenSyncNo;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.enums.CardType;
import com.yogapay.mobile.server.AcqRequest;
import com.yogapay.mobile.utils.FinanceUtil;

@Service
public class BizBaseService {
	private static Logger log = LoggerFactory.getLogger(BizBaseService.class);
	@Resource
	private AcqRequest acqRequest;
	@Resource
	private Dao dao;
    @Resource
    private BlackListService blackListService;

	// 系统发冲正
	public ISOMsg sysRollback(ISOMsg request, AcqTerminal at, CardBin cb)
			throws SQLException, ISOException, NotFoundException, IOException {
		log.info("系统发生冲正,{},{}",
				new String[] { request.getString(41), request.getString(42) });
		ISOMsg requestRollback = (ISOMsg) request.clone();
		BigDecimal amount = FinanceUtil.transAmount(requestRollback
				.getString(4));

        ISOMsg responseMsg = null;

        PosMerchant posMerchant = findMerchant(requestRollback.getString(42));
        //T+0 商户禁止冲正
        if(null!=posMerchant&&null!=posMerchant.getSettleDays()&&0!=posMerchant.getSettleDays()){
            // 查询原交易
            TransInfo ti = findTransInfoBySrc(requestRollback.getString(41),
                    requestRollback.getString(42), null,
                    requestRollback.getString(11), null, null, null, null);
            // 由于此冲正不是pos发的，所以要用系统的批次号流水号
            // requestRollback.set(11,
            // GenSyncNo.getInstance().getNextSysVoucherNo());
            // 批次号
            String terminalBatchNo = requestRollback.getString(60).substring(2, 8);
            String f60 = requestRollback.getString(60);
            // f60 = f60.replace(terminalBatchNo, Constants.SYS_BATCH);
            requestRollback.set(60, f60);
            requestRollback.setMTI("0400");
            TransInfo t = buildTransInfo(requestRollback, at, cb,
                    TransType.ROLLBACK);
            t.setSrcOrderNo(ti.getOrderNo());
            createOrder(t);
            responseMsg = acqRequest.rollback(
                    requestRollback,requestRollback.getString(35), requestRollback.getString(36),
                    ti.getTransCode(), amount, ti.getAcqAuthNo(),
                    ti.getAcqVoucherNo(), at);

            if ("00".equals(responseMsg.getString(39))) {// 更新原订单
                updateOrder(TransStatus.SUCCESS, requestRollback, responseMsg);
                // 更新原来交易
                if (ti.getTransStatus().equals(TransStatus.SUCCESS.toString())) {
                    updateOrder(TransStatus.ROLLBACK, ti.getMerchantNo(),
                            ti.getTerminalNo(), ti.getTerminalBatchNo(),
                            ti.getTerminalVoucherNo(), ti.getCardNo(), "",
                            ti.getTransCode(), ti.getMti());
                }
            } else {
                updateOrder(TransStatus.FAIL, requestRollback, responseMsg);
            }
        }else {
            responseMsg = request;
            responseMsg.setResponseMTI();
            responseMsg.set(39,"40");
        }

		return responseMsg;
	}

	// 获取收单商户终端
	public AcqTerminal getAcqTerminal(String merchantNo, String terminalNo)
			throws SQLException {
		String sql = "select * from acq_terminal where    terminal_no=? and merchant_no=?";

		AcqTerminal at = dao.findFirst(AcqTerminal.class, sql, new Object[] {
				terminalNo, merchantNo });
		String voucherNo1 = null;;
		try {
			voucherNo1 = ISOUtil.padleft(Integer.parseInt(at.getVoucherNo())+1+"", 6, '0');
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ISOException e) {
			e.printStackTrace();
		}
		// 更新流水号
		sql = "update acq_terminal set voucher_no=?,last_use_time=now() where  terminal_no=? and merchant_no=? ";

		dao.update(
				sql,
				new Object[] {voucherNo1, at.getTerminalNo(),
						at.getMerchantNo() });

		return at;
	}

	public AcqTerminal route(String terminalNo, String merchantNo,
			BigDecimal amount, CardBin cb) throws SQLException,
			RouteNotFindException {

		// 先找路由的配置
		String sql = "select * from trans_route where src_key=? and review_status=1 order by id desc";
		Map<String, Object> result = dao.findFirst(sql, "T" + terminalNo);
		if (null == result) {
			result = dao.findFirst(sql, "M" + merchantNo);
		}
		if (null == result) {
			throw new RouteNotFindException(terminalNo + "," + merchantNo);
		}
		String acqName = "";
		String targetKey = result.get("target_key").toString();
		AcqTerminal at = new AcqTerminal();
		String type = targetKey.substring(0, 1);
		targetKey = targetKey.substring(1, targetKey.length());
		// 如果规则配置到商户上
		if ("M".equals(type)) {
			acqName = result.get("acq_name").toString();
			at.setAcqName(acqName);
			// 按照商户号找终端
			sql = "select t.* from  acq_terminal t,acq_merchant m where t.merchant_no=m.merchant_no and m.open_status=1 and t.merchant_no =? and t.acq_name=? and t.open_status=1 order by last_use_time limit 0,1";
			result = dao.findFirst(sql, new Object[] { targetKey, acqName });
			if (null == result) {
				throw new RouteNotFindException(terminalNo + "," + merchantNo);
			}
		} else if ("G".equals(type)) {
			BigDecimal fee = null;
			String targetAcqMerchantNo = null;
			String targetAcqName = null;
//			String cardType = "";
//			if (cb.getCardType().equals("借记卡")) {
//				cardType = CardType.DEBIT.toString();
//			}
//			if (cb.getCardType().equals("预付费卡")) {
//				cardType = CardType.DEBIT.toString();
//			}
//			if (cb.getCardType().equals("贷记卡")) {
//				cardType = CardType.CREDIT.toString();
//			}
//			if (cb.getCardType().equals("准贷记卡")) {
//				cardType = CardType.CREDIT.toString();
//			}
//			if (cb.getCardType().equals("公务卡")) {
//				cardType = CardType.CREDIT.toString();
//			}
			sql = "select am.* from trans_route_rel trr left join acq_merchant am on trr.target = am.merchant_no where trr.group_code=? order by am.last_use_time limit 0,1";
			AcqMerchant acqMerchant = dao.findFirst(AcqMerchant.class, sql, targetKey);
			targetAcqMerchantNo = acqMerchant.getMerchantNo();
			targetAcqName = acqMerchant.getAcqName();
			acqName = acqMerchant.getAcqName();
			sql = "update acq_merchant am set am.last_use_time=? where am.merchant_no=?";
			dao.update(sql,new Object[]{new Date(), acqMerchant.getMerchantNo()});
//			for (Map<String, Object> map : results) {
//				String target = map.get("target").toString();
//				acqName = map.get("acq_name").toString();
//				sql = "select r.* from pos_rate r ,acq_merchant m where r.`merchant_no`=m.`merchant_no` and r.`acq_name`=? and m.`open_status`=1 and m.`merchant_no`=? and r.card_type=?";
//				PosRate posRate = dao.findFirst(PosRate.class, sql,
//						new Object[] { acqName, target, cardType });
//				if (null == posRate) {
//					continue;
//				}
				// 开始计算费率
//				if (null == fee) {
//					fee = fee(posRate, amount);
//					targetAcqMerchantNo = posRate.getMerchantNo();
//					targetAcqName = posRate.getAcqName();
//				} else {
//					if (fee(posRate, amount).compareTo(fee) == -1) {
//						fee = fee(posRate, amount);
//						targetAcqMerchantNo = posRate.getMerchantNo();
//						targetAcqName = posRate.getAcqName();
//					}
//				}
//			}

			if (StringUtils.isNotEmpty(targetAcqMerchantNo)
					&& StringUtils.isNotEmpty(targetAcqName)) {
				at.setAcqName(targetAcqName);
				sql = "select * from `acq_terminal` where merchant_no =? and acq_name=? and open_status=1 order by last_use_time limit 0,1";
				result = dao.findFirst(sql, new Object[] { targetAcqMerchantNo,
						acqName });
			} else {
				result = null;
			}
		}

		if (null == result) {
			throw new RouteNotFindException(terminalNo + "," + merchantNo);

		}
		at.setMerchantNo(result.get("merchant_no").toString());
		at.setTerminalNo(result.get("terminal_no").toString());
		at.setTmk(result.get("tmk").toString());
		at.setTmkTak(result.get("tmk_tak").toString());
		at.setTmkTpk(result.get("tmk_tpk").toString());
		at.setBatchNo(result.get("batch_no").toString());
		at.setVoucherNo(result.get("voucher_no").toString());
		String voucherNo1 = null;;
		try {
			voucherNo1 = ISOUtil.padleft(Integer.parseInt(at.getVoucherNo())+1+"", 6, '0');
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ISOException e) {
			e.printStackTrace();
		}

		// 更新流水号
		sql = "update acq_terminal set voucher_no=?,last_use_time=now() where acq_name=? and terminal_no=? and merchant_no=? ";
		dao.update(sql, new Object[] { voucherNo1, acqName, at.getTerminalNo(),
				at.getMerchantNo() });
		return at;

	}

	// 根据商户号终端号获取普通终端信息
	public PosTerminal getPosTerminal(String merchantNo, String terminalNo)
			throws SQLException {
		PosTerminal pos = null;
		String sql = "select * from pos_terminal where terminal_no=? and merchant_no=?";
		pos = dao.findFirst(PosTerminal.class, sql, new Object[] { terminalNo,
				merchantNo });
		return pos;
	}

	// 交易完成后的订单修改
	public int updateOrder(TransStatus ts, ISOMsg pos, ISOMsg acq)
			throws SQLException, UnsupportedEncodingException {
		String acqResponseCode = acq.getString(39);
		String acqResponseMsg = "";
		if (acq.hasField(56)) {
			acqResponseMsg = new String(acq.getBytes(56), "GBK");
		} else {
			acqResponseMsg = acqResponseMsg(acq.getString(39));
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date transTime = null;
		if (acq.hasField(12) && acq.hasField(13)) {
			Calendar ca = Calendar.getInstance();
			ca.setTime(new java.util.Date());
			String year = "" + ca.get(Calendar.YEAR);
			try {
				transTime = sdf.parse(year + acq.getString(13)
						+ acq.getString(12));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		String refNo = "000000000000";
		if (acq.hasField(37)) {
			refNo = acq.getString(37);
		}

		String mti = "";
		try {
			mti = pos.getMTI();
		} catch (ISOException e) {
			e.printStackTrace();
		}
		return updateOrder(ts, acqResponseCode, acqResponseMsg, refNo,
				acq.getString(38), pos.getString(42), pos.getString(41), pos
						.getString(60).substring(2, 8), pos.getString(11),
				pos.getString(3), mti, acq.getString(37), acq.getString(38),
				transTime);
	}

	// 交易完成后的订单修改
	public int updateOrder(TransStatus ts, String acqResponseCode,
			String acqResponseMsg, String terminalRefNo, String terminalAuthNo,
			String merchantNo, String terminalNo, String terminalBatchNo,
			String terminalVoucherNo, String transCode, String mti,
			String acqRefNo, String acqAuthNo, Date transTime)
			throws SQLException {
		String sql = "update trans_info set response_code=?,response_msg=?, trans_status=?,acq_response_code=?,acq_response_msg=?,terminal_ref_no=?,terminal_auth_no=?, acq_ref_no=?,acq_auth_no=?,trans_time=? where merchant_no=? and terminal_no=? and terminal_batch_no=? and terminal_voucher_no =? and trans_code=? and mti=?  and to_days(create_time) = to_days(?)";
		return dao.update(sql,
				new Object[] { acqResponseCode, acqResponseMsg, ts.toString(),
						acqResponseCode, acqResponseMsg, terminalRefNo,
						terminalAuthNo, acqRefNo, acqAuthNo, transTime,
						merchantNo, terminalNo, terminalBatchNo,
						terminalVoucherNo, transCode, mti ,new Date() });
	}

	// 撤销，退货，冲正等，订单修改
	public int updateOrder(TransStatus ts, String merchantNo,
			String terminalNo, String batchNo, String voucherNo, String cardNo,
			String responseCode, String transCode, String mti)
			throws SQLException {
		List<Object> params = new ArrayList<Object>();
		StringBuffer sb = new StringBuffer();
		sb.append(" trans_status=?,");
		params.add(ts.toString());
		if (StringUtils.isNotEmpty(responseCode)) {
			sb.append(" acq_response_code=?,");
			params.add(responseCode);
		}
		sb.setLength(sb.length() - 1);
		params.add(merchantNo);
		params.add(terminalNo);
		params.add(batchNo);
		params.add(voucherNo);
		params.add(cardNo);
		params.add(transCode);
		params.add(mti);
		params.add(new Date());
		String sql = "update trans_info set "
				+ sb.toString()
				+ " where merchant_no=? and terminal_no=? and terminal_batch_no=? and terminal_voucher_no=? and card_no=? and trans_code=? and mti=? and to_days(create_time) = to_days(?)";
		return dao.update(sql, params.toArray());
	}

	// 封装订单信息
	public TransInfo buildTransInfo(ISOMsg request, AcqTerminal at, CardBin cb,
			TransType tt) {
		TransInfo ti = new TransInfo();
		try {
			ti.setMti(request.getMTI());
		} catch (ISOException e) {
			e.printStackTrace();
		}
		ti.setOrderNo(GenSyncNo.getInstance().getNextOrderNo());
		ti.setTransType(tt.toString());
		/********* 终端信息提取 **************/
		// 交易处理码
		ti.setTransCode(request.getString(3));
		// 交易金额
		BigDecimal amount = FinanceUtil.transAmount(request.getString(4));
		ti.setTransAmount(amount);
		/*// 二磁道
		String track2Data = request.getString(35);
		ti.setTrack2(track2Data);*/
		/* //三磁道
		if (request.hasField(36)) {
			String track3Data = request.getString(36);
			ti.setTrack3(track3Data);
		}*/
		String merchantNo = request.getString(42);
		String terminalNo = request.getString(41);

		ti.setCardNo(cb.getCardNo());
		ti.setCardIssuerBank(cb.getBankName());
		ti.setCardName(cb.getCardName());
		if (cb.getCardType().equals("借记卡")) {
			ti.setCardType(CardType.DEBIT.toString());
		}
		if (cb.getCardType().equals("预付费卡")) {
			ti.setCardType(CardType.PREPAID.toString());
		}
		if (cb.getCardType().equals("贷记卡")) {
			ti.setCardType(CardType.CREDIT.toString());
		}
		if (cb.getCardType().equals("准贷记卡")) {
			ti.setCardType(CardType.QUASICREDIT.toString());
		}
		if (cb.getCardType().equals("公务卡")) {
			ti.setCardType(CardType.OFFICIAL.toString());
		}

		ti.setMerchantNo(merchantNo);
		ti.setTerminalNo(terminalNo);
		// 批次号
		String terminalBatchNo = request.getString(60).substring(2, 8);
		// 凭证号
		String terminalVoucherNo = request.getString(11);
		ti.setTerminalBatchNo(terminalBatchNo);
		ti.setTerminalVoucherNo(terminalVoucherNo);

		ti.setAcqBatchNo(at.getBatchNo());
		ti.setAcqMerchantNo(at.getMerchantNo());
		ti.setAcqTerminalNo(at.getTerminalNo());
		ti.setAcqVoucherNo(at.getVoucherNo());
		ti.setAcqName(at.getAcqName());
		return ti;
	}

	// 查询原交易，用来做撤销，退货
	public TransInfo findTransInfoBySrc(String terminalNo, String merchantNo,
			String batchNo, String voucherNo, String refNo, TransType ts,
			TransStatus status, String date) throws SQLException {

		List<String> params = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		params.add(terminalNo);
		params.add(merchantNo);
		if (null != ts) {
			sb.append(" and trans_type=?");
			params.add(ts.toString());
		}

		if (StringUtils.isNotEmpty(batchNo)) {
			sb.append(" and terminal_batch_no=?");
			params.add(batchNo);
		}
		if (StringUtils.isNotEmpty(voucherNo)) {
			sb.append(" and terminal_voucher_no=?");
			params.add(voucherNo);
		}
		if (StringUtils.isNotEmpty(refNo)) {
			sb.append(" and terminal_ref_no=?");
			params.add(refNo);
		}
		if (null != status) {
			sb.append(" and trans_status=?");
			params.add(status.toString());
		}

		String sql = "select * from trans_info where terminal_no=? and merchant_no=?  "
				+ sb.toString() + " order by id desc limit 0,1";
		TransInfo ti = dao.findFirst(TransInfo.class, sql, params.toArray());
		if (null != ti) {
			if (StringUtils.isNotEmpty(date)) {
				SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
				String srcDate = sdf.format(ti.getTransTime());
				if (StringUtils.isNotEmpty(date) && !date.equals(srcDate)) {
					return null;
				}
			}
		}

		return ti;
	}

	// 组装一个订单需要的当前费率
	public String buildRate(PosRate rate) {
		StringBuffer merchantRate = new StringBuffer();
		if (StringUtils.isNotEmpty(rate.getBaseRate())) {
			merchantRate.append(rate.getBaseRate() + "%");
		}
		if (StringUtils.isNotEmpty(rate.getTopRate())) {
			merchantRate.append("-" + rate.getTopRate() + "元");
		}
		if (StringUtils.isNotEmpty(rate.getSetpRate())) {
			merchantRate.append(rate.getSetpRate());
		}
		return merchantRate.toString();
	}

	// 创建订单，每笔消费都要创建一个订单
	public void createOrder(TransInfo ti) throws SQLException {
		BigDecimal merchantFee = new BigDecimal(0);
		BigDecimal acqMerchantFee = new BigDecimal(0);
		String merchantRate = null;
		String acqRate = null;

		String cardType = "";
		if (ti.getCardType().equals(CardType.DEBIT.toString())) {
			cardType = CardType.DEBIT.toString();
		}
		if (ti.getCardType().equals(CardType.PREPAID.toString())) {
			cardType = CardType.DEBIT.toString();
		}
		if (ti.getCardType().equals(CardType.CREDIT.toString())) {
			cardType = CardType.CREDIT.toString();
		}
		if (ti.getCardType().equals(CardType.QUASICREDIT.toString())) {
			cardType = CardType.CREDIT.toString();
		}
		if (ti.getCardType().equals(CardType.OFFICIAL.toString())) {
			cardType = CardType.CREDIT.toString();
		}
		// 普通商户计费规则
		PosRate rate = findRate(AcqName.SELF.toString(), ti.getMerchantNo(),
				cardType);
		merchantRate = buildRate(rate);
		// 收单商户计费
		PosRate acqrate = findRate(ti.getAcqName(), ti.getAcqMerchantNo(),
				cardType);
		acqRate = buildRate(acqrate);
		// 收单商户计费规则

		if (ti.getTransType().equals(TransType.SALE.toString())) {
			/********* 普通手续费计算 **************/
			merchantFee = fee(rate, ti.getTransAmount());
			/********* 收单手续费计算 **************/
			acqMerchantFee = fee(acqrate, ti.getTransAmount());
		}
		// 查询商户结算
		String sql = "select * from pos_merchant where merchant_no=?";
		PosMerchant merchant = dao.findFirst(PosMerchant.class, sql,
				ti.getMerchantNo());
		int days = 1;
		if (null != merchant.getSettleDays()) {
			days = merchant.getSettleDays();
		}
		Date settleDate = HolidayUtil.settleDate(days);
		sql = "insert into trans_info(mti,merchant_settle_date,order_no,src_order_no,merchant_rate,acq_rate,trans_code,trans_type,trans_status,trans_amount,card_no,track2,track3,card_issuer_bank,card_type,card_name,merchant_no,terminal_no,merchant_fee,terminal_batch_no,terminal_voucher_no,acq_merchant_no,acq_terminal_no,acq_fee,acq_batch_no,acq_voucher_no,create_time,merchant_settle_type,pay_type) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		dao.update(
				sql,
				new Object[] { ti.getMti(),settleDate,ti.getOrderNo(),
						ti.getSrcOrderNo(), merchantRate, acqRate,
						ti.getTransCode(), ti.getTransType(),
						TransStatus.INIT.toString(), ti.getTransAmount(),
						ti.getCardNo(), ti.getTrack2(), ti.getTrack3(),
						ti.getCardIssuerBank(), ti.getCardType(),
						ti.getCardName(), ti.getMerchantNo(),
						ti.getTerminalNo(), merchantFee,
						ti.getTerminalBatchNo(), ti.getTerminalVoucherNo(),
						ti.getAcqMerchantNo(), ti.getAcqTerminalNo(),
						acqMerchantFee, ti.getAcqBatchNo(),
						ti.getAcqVoucherNo(),
                        new Date(),
						merchant.getType(),
                        merchant.getPayType()
                } );
	}

	private static Map<String, String> f39Maps = null;

	// 获取收单机构返回码
	public String acqResponseMsg(String f39) throws SQLException {
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
		if (StringUtils.isNotEmpty(f39)) {
			return f39Maps.get(f39);
		}
		return null;

	}

	// 手续费计算
	public BigDecimal fee(PosRate rate, BigDecimal amount) {
		// 最后计算的结果
		BigDecimal bd = null;
		BigDecimal zero = new BigDecimal("0");
		if (amount.equals(zero)) {
			return zero;
		}
		// 先算基础扣率，如果基础扣率不为空
		if (StringUtils.isNotEmpty(rate.getBaseRate())) {
			bd = amount.multiply(new BigDecimal(rate.getBaseRate())
					.movePointLeft(2));
		}
		// 看看是否有封顶，如果有封顶，切基础扣率手续费大于封顶的，者选择封顶手续费
		if (StringUtils.isNotEmpty(rate.getTopRate())) {
			BigDecimal top = new BigDecimal(rate.getTopRate());
			if (top.compareTo(bd) == -1) {
				bd = top;
			}
		}
		// 阶梯费率
		if (StringUtils.isNotEmpty(rate.getSetpRate())) {

		}

		bd = bd.setScale(2, BigDecimal.ROUND_UP);
		return bd;
	}

	// 根据商户号获取普通商户信息
	public PosMerchant findMerchant(String merchantNo) throws SQLException {
        if(StringUtils.isBlank(merchantNo)) return null;
		String sql = "select * from pos_merchant where merchant_no = ?";
		return dao.findFirst(PosMerchant.class, sql, merchantNo);
	}

    // 根据 商户编号 查找
    public MobileMerchant findMobileMerchant(String merchantNo) throws SQLException{
        if(StringUtils.isBlank(merchantNo)) return null;
        String sql = "SELECT * FRoM mobile_merchant_view WHERE merchant_no = ?";
        return dao.findFirst(MobileMerchant.class, sql,merchantNo);
    }

	// 根据收单机构名称商户号获取手续费
	public PosRate findRate(String acqName, String merchantNo, String cardType)
			throws SQLException {
		String sql = "select * from pos_rate where acq_name = ? and merchant_no=? and card_type=?";
		return dao.findFirst(PosRate.class, sql, new Object[] { acqName,
				merchantNo, cardType });
	}

	// 根据商户号获取收单商户信息
	public AcqPosMerchant findAcqMerchant(String acqName, String merchantNo)
			throws SQLException {
		String sql = "select * from acq_merchant where acq_name=? and merchant_no = ?";
		return dao.findFirst(AcqPosMerchant.class, sql, new Object[] { acqName,
				merchantNo });
	}

	public CardBin cardBin(String cardNo) throws CardBinNotFoundException {
		String sql = "select * from card_bin c  where  c.card_length = length(?) AND c.verify_code = left(?,  c.verify_length)  ";
		try {
			CardBin cb = dao.findFirst(CardBin.class, sql, new Object[] {
					cardNo, cardNo });
			if (null == cb) {
				throw new CardBinNotFoundException(cardNo);
			} else {
				cb.setCardNo(cardNo);
				return cb;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 根据isoMsg 进行密码的明文获取，其实就算把以上几个方法整合了下
	 * 
	 * @param isoMsg
	 * @return
	 * @throws SQLException
	 */
	public String clearPin(ISOMsg isoMsg) throws SQLException {
		// 账户
		String accountNo = isoMsg.getString(35);
		accountNo = accountNo.replace("D", "=");
		accountNo = accountNo.substring(0, accountNo.indexOf("="));

		PosTerminal posTerminal = getPosTerminal(isoMsg.getString(42),
				isoMsg.getString(41));
		String clearPk = JCEHandler.decryptData(posTerminal.getTmkTpk(),
				posTerminal.getTmk());
		String clearPin = "000000";
		if(StringUtils.isNotEmpty(isoMsg.getString(52))){
			clearPin = FinanceUtil.unPinBlock(accountNo,
					isoMsg.getString(52), clearPk);
		}
		return clearPin;
	}
	
	/**
	 * 风控规则检验
	 * @throws SQLException 
	 * @throws ISOException 
	 * @throws ParseException 
	 */
	public ISOMsg saleRiskValue(ISOMsg request, CardBin cb) throws SQLException, ISOException, ParseException{
		ISOMsg response = (ISOMsg) request.clone();
		long amount = Long.parseLong(request.getString(4));
		String terminalNo = request.getString(41);
		String merchantNo = request.getString(42);
		
		String sql = "select * from risk_value";
		RiskValue riskValue = dao.findFirst(RiskValue.class, sql);
		
		String sql1 = "select IFNULL(sum(t.trans_amount),0) sum from trans_info t where t.terminal_no = ? and t.trans_type='SALE' and t.trans_status='SUCCESS' and date(t.create_time) = curdate()";
		BigDecimal sum = (BigDecimal)dao.findFirst(sql1, terminalNo).get("sum");
		 long sumDaily = sum.longValue();
		String sql2 = "select IFNULL(sum(t.trans_amount),0) sum from trans_info t where t.terminal_no = ? and t.trans_type='SALE' and t.trans_status='SUCCESS' and t.card_type!='DEBIT' and date(t.create_time) = curdate()";
		BigDecimal sum1 = (BigDecimal) dao.findFirst(sql2, terminalNo).get("sum");
		 long sumCreditDaily = sum1.longValue();
		
		String sql3 = "select count(id) c from trans_info where terminal_no=? and card_no=?  and trans_status='SUCCESS' and trans_type='SALE' and to_days(create_time) = to_days(?)";
		long sameCardCount = (Long) dao.findFirst(sql3, new Object[]{terminalNo, cb.getCardNo(),new Date()}).get("c");
		String sql4 = "select count(id) c from trans_info where terminal_no=?  and trans_status='SUCCESS' and trans_type='SALE'  and to_days(create_time) = to_days(?)";
		long count = (Long) dao.findFirst(sql4, new Object[]{terminalNo,new Date()}).get("c");

		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");

        HashMap params = new HashMap<String,Object>();
        params.put("merchantNo",merchantNo);
        BlackMerchant blackMerchant = blackListService.findFirstBlackMerchant(params);
        params.clear();
        params.put("cardNo",cb.getCardNo());
        BlackCard blackCard = blackListService.findFirstBlackCard(params);
        params.clear();
        params.put("terminalNo",terminalNo);
        BlackTerminal blackTerminal = blackListService.findFirstBlackTerminal(params);

        //无效卡号
        if(null==cb){
            response.set(39, "14");
            return response;
        }

        //黑名单商户或终端号
        if(null!=blackMerchant||null!=blackTerminal){
            response.set(39, "03");
            return response;
        }

        //黑名单卡号
        if(null!=blackCard){
            response.set(39, "14");
            return response;
        }

		if(riskValue != null){
			
			//查找商户类型
			String sql5 = "select type from pos_merchant pm where pm.merchant_no=?";
			String type =(String)dao.findFirst(sql5,merchantNo).get("type");
			if(!"餐娱类".equals(type)){
				if(StringUtils.isNotEmpty(riskValue.getLimitTimeBegin())){
					if((sdf.parse(s.format(now) + " " + riskValue.getLimitTimeBegin()).getTime() - now.getTime()) > 0){
						response.set(39, "58");
						return response;
					}
				}
				
				if(StringUtils.isNotEmpty(riskValue.getLimitTimeEnd())){
					if((sdf.parse(s.format(now) + " " + riskValue.getLimitTimeEnd()).getTime() - now.getTime()) < 0){
						response.set(39, "58");
						return response;
					}
				}
			}
			//判断同卡一天消费笔数
			if(riskValue.getSameCardCount() != null){
				if(sameCardCount >= riskValue.getSameCardCount()){
					response.set(39, "65");
					return response;
				}
			}
			
			//判断一天消费笔数
			if(riskValue.getCount() != null){
				if(count >= riskValue.getCount()){
					response.set(39, "65");
					return response;
				}
			}
			
			//判断最小消费金额和最大消费金额
			if(riskValue.getEachMax() != null){
				if(amount > riskValue.getEachMax()){
					response.set(39, "13");
					return response;
				}
			}
			if(riskValue.getEachMin() != null){
				if(amount < riskValue.getEachMin()){
					response.set(39, "13");
					return response;
				}
			}
			//判断累计一天消费金额
			if(riskValue.getTerminalDailyMax() != null){
				if((amount + sumDaily) > riskValue.getTerminalDailyMax()){
					response.set(39, "13");
					return response;
				}
			}
			
			//判断是不是借记卡消费
			if(!"借记卡".equals(cb.getCardType())){
				//判断信用卡单笔最大金额
				if( riskValue.getCreditCardEachMax() != null){
					if(amount > riskValue.getCreditCardEachMax()){
						response.set(39, "13");
						return response;
					}
				}
				
				//判断单终端信用卡交易累计金额
				if(riskValue.getCreditCardTerminalDailyMax() != null){
					if((amount +sumCreditDaily) > riskValue.getCreditCardTerminalDailyMax()){
						response.set(39, "13");
						return response;
					}
				}
			}
		}
		
		return response;
	}
	
	public ISOMsg queryRiskValue(ISOMsg request, CardBin cb) throws SQLException, ParseException, ISOException{
		ISOMsg response = (ISOMsg) request.clone();
        String terminalNo = request.getString(41);
        String merchantNo = request.getString(42);

		String sql = "select * from risk_value";
		RiskValue riskValue = dao.findFirst(RiskValue.class, sql);
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");

        HashMap params = new HashMap<String,Object>();
        params.put("merchantNo",merchantNo);
        BlackMerchant blackMerchant = blackListService.findFirstBlackMerchant(params);
        params.clear();
        params.put("cardNo",cb.getCardNo());
        BlackCard blackCard = blackListService.findFirstBlackCard(params);
        params.clear();
        params.put("terminalNo",terminalNo);
        BlackTerminal blackTerminal = blackListService.findFirstBlackTerminal(params);

        //无效卡号
        if(null==cb){
            response.set(39, "14");
            return response;
        }

        //黑名单商户或终端号
        if(null!=blackMerchant||null!=blackTerminal){
            response.set(39, "03");
            return response;
        }

        //黑名单卡号
        if(null!=blackCard){
            response.set(39, "14");
            return response;
        }


        if(riskValue != null){
			if(StringUtils.isNotEmpty(riskValue.getLimitTimeBegin())){
				if((sdf.parse(s.format(now) + " " + riskValue.getLimitTimeBegin()).getTime() - now.getTime()) > 0){
					response.set(39, "58");
					return response;
				}
			}
			
			if(StringUtils.isNotEmpty(riskValue.getLimitTimeEnd())){
				if((sdf.parse(s.format(now) + " " + riskValue.getLimitTimeEnd()).getTime() - now.getTime()) < 0){
					response.set(39, "58");
					return response;
				}
			}
		}
		return response;
	}

    public boolean createAccountInfo(MerchantAccountInfo accountInfo) throws SQLException {
        if(null==accountInfo){return false;}
        String sql = "insert into merchant_account_info(account_id,create_time,amount,operate_type) values(?,?,?,?)";
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(accountInfo.getAccountId());
        params.add(new Date());
        params.add(accountInfo.getAmount());
        params.add(accountInfo.getOperateType());
        int row = dao.update(sql,params.toArray());
        if(row==0) return false;
        return true;
    }

    public boolean createAccount(MerchantAccount account) throws SQLException {
        if(null==account){return false;}
        String sql = "insert into merchant_account(merchant_no,amount,freeze_amount,avaliable_amount,status,create_time) values(?,?,?,?,?,?)";
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(account.getMerchantNo());
        params.add(account.getAmount());
        params.add(account.getFreezeAmount());
        params.add(account.getAvaliableAmount());
        params.add(account.getStatus());
        params.add(new Date());
        int row = dao.update(sql,params.toArray());
        if(row==0) return false;
        return true;
    }

    public boolean createJifen(MerchantJifen jifen) throws SQLException {
        if(null==jifen){return false;}
        String sql = "insert into merchant_jifen(jifen,merchant_no,rule,create_time) values(?,?,?,?)";
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(jifen.getJifen());
        params.add(jifen.getMerchantNo());
        params.add(jifen.getRule());
        params.add(new Date());
        int row = dao.update(sql,params.toArray());
        if(row==0) return false;
        return true;
    }

    public boolean createJifenInfo(MerchantJifenInfo jifenInfo) throws SQLException {
        if(null==jifenInfo){return false;}
        String sql = "insert into merchant_jifen_info(jifen_id,jifen,operate_type,create_time) values(?,?,?,?)";
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(jifenInfo.getJifenId());
        params.add(jifenInfo.getJifen());
        params.add(jifenInfo.getOperateType());
        params.add(new Date());
        int row = dao.update(sql,params.toArray());
        if(row==0) return false;
        return true;
    }

    public MerchantAccount findAccountFirst(Map<String,String> params) throws SQLException {
        StringBuffer sqlWhere = new StringBuffer(" where 1=1 ");
        ArrayList<Object> paramList = new ArrayList<Object>();
        if(null!=params){
            if(StringUtils.isNotBlank(params.get("merchantNo"))){
                paramList.add(params.get("merchantNo"));
                sqlWhere.append(" and merchant_no=? ");
            }
        }
        String sql = "select * from merchant_account "+sqlWhere;
        return dao.findFirst(MerchantAccount.class,sql,paramList.toArray());
    }

    public MerchantAccountInfo findAccountInfoFirst(Map<String,String> params) throws SQLException {
        StringBuffer sqlWhere = new StringBuffer(" where 1=1 and a.account_id = b.id ");
        ArrayList<Object> paramList = new ArrayList<Object>();
        if(null!=params){
            if(StringUtils.isNotBlank(params.get("merchantNo"))){
                paramList.add(params.get("merchantNo"));
                sqlWhere.append(" and b.merchant_no=? ");
            }
            if(StringUtils.isNotBlank(params.get("sthirdVoucher"))){
                paramList.add(params.get("sthirdVoucher"));
                sqlWhere.append(" and sthird_voucher=? ");
            }
        }
        String sql = "select a.* from merchant_account_info a,merchant_account b "+sqlWhere;
        return dao.findFirst(MerchantAccountInfo.class,sql,paramList.toArray());
    }

    public MerchantJifen findJifenFirst(Map<String,String> params) throws SQLException {
        StringBuffer sqlWhere = new StringBuffer(" where 1=1 ");
        ArrayList<Object> paramList = new ArrayList<Object>();
        if(null!=params){
            if(StringUtils.isNotBlank(params.get("merchantNo"))){
                paramList.add(params.get("merchantNo"));
                sqlWhere.append(" and merchant_no=? ");
            }
        }
        String sql = "select * from merchant_jifen "+sqlWhere;
        return dao.findFirst(MerchantJifen.class,sql,paramList.toArray());
    }

    public boolean updateAccount(Map<String,String> ifParams,Map<String,String> params) throws SQLException {
        if(null==params&&params.size()==0) return false;
        StringBuffer sql = new StringBuffer("update merchant_account set ");
        StringBuffer sqlWhere = new StringBuffer(" where 1=1 ");
        ArrayList<Object> paramList = new ArrayList<Object>();
        if(StringUtils.isNotBlank(params.get("inAmount"))){
            sql.append(" amount=amount+?,avaliable_amount=avaliable_amount+?,");
            paramList.add(params.get("inAmount"));
            paramList.add(params.get("inAmount"));
        }
        if(StringUtils.isNotBlank(params.get("outAmount"))){
            sql.append(" amount=amount-?,avaliable_amount=avaliable_amount-?,");
            paramList.add(params.get("outAmount"));
            paramList.add(params.get("outAmount"));
        }
        if(StringUtils.isNotBlank(ifParams.get("merchantNo"))){
            sqlWhere.append(" and merchant_no=? ");
            paramList.add(ifParams.get("merchantNo"));
        }
        int row = dao.update(sql.substring(0,sql.length()-1)+sqlWhere,paramList.toArray());
        if(0==row) return false;
        return true;
    }

    public boolean updateJifen(Map<String,String> ifParams,Map<String,String> params) throws SQLException {
        if(null==params&&params.size()==0) return false;
        StringBuffer sql = new StringBuffer("update merchant_jifen set ");
        StringBuffer sqlWhere = new StringBuffer(" where 1=1 ");
        ArrayList<Object> paramList = new ArrayList<Object>();
        if(StringUtils.isNotBlank(params.get("inAmount"))){
            sql.append(" jifen=jifen+?,");
            paramList.add(params.get("inAmount"));
        }
        if(StringUtils.isNotBlank(params.get("outAmount"))){
            sql.append(" jifen=jifen-?,");
            paramList.add(params.get("outAmount"));
        }
        if(StringUtils.isNotBlank(ifParams.get("merchantNo"))){
            sqlWhere.append(" and merchant_no=? ");
            paramList.add(ifParams.get("merchantNo"));
        }
        int row = dao.update(sql.substring(0,sql.length()-1)+sqlWhere,paramList.toArray());
        if(0==row) return false;
        return true;
    }

    //删除交易警告
    public boolean deleteTransWarn(String merchantNo) throws SQLException{
        if(StringUtils.isBlank(merchantNo)) return false;
        String sql = "delete from trans_warn where merchant_no=?";
        int rows = dao.update(sql,merchantNo);
        if(rows>0) return true;
        return false;
    }

    //保存交易警告
    public boolean saveTransWarn(TransWarn warn)throws SQLException{
        if(null==warn) return false;
        String sql = "insert into trans_warn (merchant_no,counter,last_warn_time,seconds) values(?,?,?,?)";
        int rows = dao.update(sql,new Object[]{warn.getMerchantNo(),warn.getCounter(),warn.getLastWarnTime(),warn.getSeconds()});
        if(rows>0) return true;
        return false;
    }

    //更新最后一次发送时间或新增计数
    public boolean updateTransWarn(Map<String,String>  params,Map<String,String> ifParams)throws SQLException{
        if(null==params&&params.size()==0) return false;
        StringBuffer sql = new StringBuffer("update trans_warn set ");
        StringBuffer sqlWhere = new StringBuffer(" where 1=1 ");
        ArrayList<Object> paramList = new ArrayList<Object>();
        if(StringUtils.isNotBlank(params.get("lastWarnTime"))){
            sql.append(" last_warn_time=?,");
            paramList.add(params.get("lastWarnTime"));
        }
        if(StringUtils.isNotBlank(params.get("counter"))){
            sql.append(" counter=?,");
            paramList.add(params.get("counter"));
        }
        if(StringUtils.isNotBlank(ifParams.get("merchantNo"))){
            sqlWhere.append(" and merchant_no=? ");
            paramList.add(ifParams.get("merchantNo"));
        }
        int rows = dao.update(sql.substring(0,sql.length()-1)+sqlWhere,paramList.toArray());
        if(0==rows) return false;
        return true;
    }

    //返回一条trans_warn
    public TransWarn findFirstTransWarn(String merchantNo)throws SQLException{
        if(StringUtils.isBlank(merchantNo)) return null;
        String sql = "select * from trans_warn where merchant_no=? ";
        return dao.findFirst(TransWarn.class,sql,merchantNo);
    }

    //发送短信
    public void sendMessage(List<String> phoneList)throws Exception{
        if (null != phoneList) {
            //通知技术支持人员
            {
               phoneList.add("18988790466");//zhanggc
                phoneList.add("18589091930");//hanlei
            }
            Map<String, String> values = new HashMap<String, String>();
            values.put("operation", "S");
            values.put("note.businessCode", "YQZL");
            values.put("content", "系统出现连续交易超时现象，请及时处理【和付通】");
            for(String phone:phoneList){
                if(StringUtils.isNotBlank(phone)){
                    values.put("target", phone);
                    values.put("check", SHA.SHA1Encode(values.get("target") + "yogapayHFT" + values.get("content") + values.get("note.businessCode")).toUpperCase());
                    Sms.sendHttpGet(ConstantsLoader.getProperty("message_host"),values);
                }
            }
        }
    }
}
