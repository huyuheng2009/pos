package com.yogapay.core.service;

import java.math.BigDecimal;

import javax.annotation.Resource;

import com.yogapay.core.domain.PosMerchant;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.domain.TransInfo;
import com.yogapay.core.enums.TransStatus;
import com.yogapay.core.enums.TransType;
import com.yogapay.core.exception.TransInfoNotFindException;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.server.AcqRequest;
import com.yogapay.mobile.utils.FinanceUtil;

/**
 * 撤销
 * 
 * @author donjek
 * 
 */
@Service
public class ReversalService extends BizBaseService {
	@Resource
	private AcqRequest acqRequest;

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
		// 原批次号
		String srcBatchNo = request.getString(61).substring(0, 6);
		// 原流水号
		String srcVoucherNo = request.getString(61).substring(6, 12);

        ISOMsg responseMsg = null;

        PosMerchant posMerchant = findMerchant(srcMerchantNo);
        //T+0 商户禁止撤销
        if(null!=posMerchant&&null!=posMerchant.getSettleDays()&&0!=posMerchant.getSettleDays()){
            // 查询原交易
            TransInfo ti = findTransInfoBySrc(srcTerminalNo, srcMerchantNo,
                    srcBatchNo, srcVoucherNo, srcRefNo, null, TransStatus.SUCCESS,
                    null);
            if (null == ti) {
                throw new TransInfoNotFindException();
            }
            if (null == ti || !ti.getTransType().equals(TransType.SALE.toString())
                    || !ti.getTransStatus().equals(TransStatus.SUCCESS.toString())) {
                throw new TransInfoNotFindException();
            }
            // 原终端
            AcqTerminal at = getAcqTerminal(ti.getAcqMerchantNo(),
                    ti.getAcqTerminalNo());
            // 创建订单
            CardBin cb = cardBin(FinanceUtil.getAccountNo(request));
            TransInfo t = buildTransInfo(request, at, cb, TransType.REVERSAL);
            t.setSrcOrderNo(ti.getOrderNo());
            createOrder(t);
            responseMsg = acqRequest
                    .reversal(request.getString(35), request.getString(36),clearPin(request),
                            srcAmount, ti.getAcqRefNo(), ti.getAcqAuthNo(),
                            ti.getAcqBatchNo(), ti.getAcqVoucherNo(), at);

            // 发冲正
            if (null == responseMsg) {
                for (int i = 0; i < 5; i++) {
                    if (null != sysRollback(request, at, cb)) {
                        break;
                    }
                }

            }

            if ("00".equals(responseMsg.getString(39))) {
                // 更新撤销订单
                updateOrder(TransStatus.SUCCESS, request, responseMsg);
                // 更新原来交易
                updateOrder(TransStatus.REVERSAL, ti.getMerchantNo(),
                        ti.getTerminalNo(), ti.getTerminalBatchNo(),
                        ti.getTerminalVoucherNo(), ti.getCardNo(), "",ti.getTransCode(),ti.getMti());
            } else {
                updateOrder(TransStatus.FAIL, request, responseMsg);
            }
        }else {
            responseMsg = request;
            responseMsg.set(39,"40");
        }

		// TODO 这里数据库读取原始数据
		rm.setAcqResponseCode(responseMsg.getString("39"));
		rm.setIosMsg(responseMsg);
		return rm;
	}
}
