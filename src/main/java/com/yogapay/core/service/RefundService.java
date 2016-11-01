package com.yogapay.core.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.annotation.Resource;

import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.domain.TransInfo;
import com.yogapay.core.enums.TransStatus;
import com.yogapay.core.enums.TransType;
import com.yogapay.core.exception.MaxAmountException;
import com.yogapay.core.exception.TransInfoNotFindException;
import com.yogapay.core.utils.Dao;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.server.AcqRequest;
import com.yogapay.mobile.utils.FinanceUtil;

/**
 * 退货
 * 
 * @author donjek
 * 
 */
@Service
public class RefundService extends BizBaseService {
	@Resource
	private AcqRequest acqRequest;
	@Resource
	private Dao dao;

	public ResponseMsg doTrx(ISOMsg request) throws Exception {
		ResponseMsg rm = new ResponseMsg();
		// 交易金额
		BigDecimal srcAmount = FinanceUtil.transAmount(request.getString(4));
		// 参考号
		String srcRefNo = request.getString(37);
		// 终端号
		String srcTerminalNo = request.getString(41);
		// 商户号
		String srcMerchantNo = request.getString(42);

		// 原日期
		String srcDate = request.getString(61).substring(12, 16);

		// 查询原交易
		TransInfo ti = findTransInfoBySrc(srcTerminalNo, srcMerchantNo, null,
				null, srcRefNo, TransType.SALE, TransStatus.SUCCESS, srcDate);
		if (null == ti) {
			throw new TransInfoNotFindException();
		}
		if (null == ti || !ti.getTransType().equals(TransType.SALE.toString())
				|| !ti.getTransStatus().equals(TransStatus.SUCCESS.toString())) {
			throw new TransInfoNotFindException();
		}
		// 原路由终端
		AcqTerminal at = getAcqTerminal(ti.getAcqMerchantNo(),
				ti.getAcqTerminalNo());
		// 创建订单
		CardBin cb = cardBin(FinanceUtil.getAccountNo(request));
		TransInfo t = buildTransInfo(request, at, cb, TransType.REFUND);
		t.setSrcOrderNo(ti.getOrderNo());
		createOrder(t);

		// 退货成功总金额是否大于订单金额
		String sql = "select sum(trans_amount) a from trans_info where terminal_no=? and merchant_no=?   and terminal_ref_no=?     and card_no=? and trans_type=? and trans_status=?";
		if (srcAmount.compareTo(ti.getTransAmount()) == 1) {
			throw new MaxAmountException();
		}
		Map<String, Object> result = dao.findFirst(
				sql,
				new Object[] { srcTerminalNo, srcMerchantNo,
						ti.getTerminalRefNo(), ti.getCardNo(),
						TransType.REFUND.toString(),
						TransStatus.SUCCESS.toString() });
		if (null != result.get("a")) {
			String refundCountAmount = result.get("a").toString();
			BigDecimal b = new BigDecimal(refundCountAmount);
			if (b.compareTo(ti.getTransAmount()) == 0
					|| b.add(srcAmount).compareTo(ti.getTransAmount()) == 1) {
				throw new MaxAmountException();
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
		srcDate = sdf.format(ti.getTransTime());

		ISOMsg responseMsg = acqRequest.refund(request.getString(35),
				request.getString(36), srcAmount, ti.getAcqAuthNo(),
				ti.getAcqBatchNo(), ti.getAcqVoucherNo(), ti.getAcqRefNo(),
				srcDate, at);

		if ("00".equals(responseMsg.getString(39))) {
			updateOrder(TransStatus.SUCCESS, request, responseMsg);
			// 更新原来交易
			updateOrder(TransStatus.REVERSAL, ti.getMerchantNo(),
					ti.getTerminalNo(), ti.getTerminalBatchNo(),
					ti.getTerminalVoucherNo(), ti.getCardNo(), "",ti.getTransCode(),ti.getMti());
		} else {
			updateOrder(TransStatus.FAIL, request, responseMsg);
		}
		// TODO 这里数据库读取原始数据
		rm.setAcqResponseCode(responseMsg.getString("39"));
		rm.setIosMsg(responseMsg);
		return rm;
	}
}
