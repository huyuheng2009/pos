package com.yogapay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.annotation.Resource;

import com.yogapay.core.domain.PosMerchant;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.domain.TransInfo;
import com.yogapay.core.enums.TransStatus;
import com.yogapay.core.enums.TransType;
import com.yogapay.core.exception.CardBinNotFoundException;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.server.AcqRequest;
import com.yogapay.mobile.utils.FinanceUtil;

/**
 * 冲正
 * 
 * @author donjek
 * 
 */
@Service
public class RollbackService extends BizBaseService {
	@Resource
	private AcqRequest acqRequest;

	public ResponseMsg doTrx(ISOMsg request) throws SQLException,
			CardBinNotFoundException, ISOException, NotFoundException,
			IOException {
		ResponseMsg rm = new ResponseMsg();
		// 交易金额
		BigDecimal srcAmount = FinanceUtil.transAmount(request.getString(4));
		// 参考号
		// 终端号
		String srcTerminalNo = request.getString(41);
		// 商户号
		String srcMerchantNo = request.getString(42);
		// 原流水号
		String srcVoucherNo = request.getString(11);

        ISOMsg responseMsg = null;

        PosMerchant posMerchant = findMerchant(srcMerchantNo);
        //T+0 商户禁止撤销
        if(null!=posMerchant&&null!=posMerchant.getSettleDays()&&0!=posMerchant.getSettleDays()){
            // 查询原交易
            TransInfo ti = findTransInfoBySrc(srcTerminalNo, srcMerchantNo, null,
                    srcVoucherNo, null, null, null, null);
            // 原终端
            AcqTerminal at = getAcqTerminal(ti.getAcqMerchantNo(),
                    ti.getAcqTerminalNo());
            // 设置原流水号
            at.setVoucherNo(ti.getAcqVoucherNo());
            // 创建订单
            CardBin cb = cardBin(ti.getCardNo());

            TransInfo t = buildTransInfo(request, at, cb, TransType.ROLLBACK);
            t.setSrcOrderNo(ti.getOrderNo());
            createOrder(t);

            responseMsg = acqRequest.rollback(request,request.getString(35),
                    request.getString(36), ti.getTransCode(), srcAmount,
                    ti.getAcqAuthNo(), ti.getAcqVoucherNo(), at);
            if ("00".equals(responseMsg.getString(39))) {
                updateOrder(TransStatus.SUCCESS, request, responseMsg);
                // 更新原来交易
                if (ti.getTransStatus().equals(TransStatus.SUCCESS.toString())) {
                    updateOrder(TransStatus.ROLLBACK, ti.getMerchantNo(),
                            ti.getTerminalNo(), ti.getTerminalBatchNo(),
                            ti.getTerminalVoucherNo(), ti.getCardNo(), "",
                            ti.getTransCode(), ti.getMti());
                }

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
