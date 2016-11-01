package com.yogapay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;

import javax.annotation.Resource;

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
import com.yogapay.core.exception.HsmException;
import com.yogapay.core.exception.RouteNotFindException;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.server.AcqRequest;
import com.yogapay.mobile.utils.FinanceUtil;

/**
 * 余额查询业务逻辑
 * 
 * @author donjek
 * 
 */
@Service
public class QueryService extends BizBaseService {
	@Resource
	private AcqRequest acqRequest;
    @Resource
    private TransWarnService transWarnService;

	public ResponseMsg doTrx(ISOMsg request) throws ISOException, HsmException,
            NotFoundException, SQLException,
            RouteNotFindException, CardBinNotFoundException, ParseException, IOException {
		ResponseMsg rm = new ResponseMsg();
		ISOMsg responseMsg = null;
		// 识别卡bin
		CardBin cb = cardBin(FinanceUtil.getAccountNo(request));
		
		//风控规则
		responseMsg = queryRiskValue(request, cb);
		if(responseMsg.hasField(39)){
			rm.setAcqResponseCode(responseMsg.getString("39"));
			rm.setIosMsg(responseMsg);
			return rm;
		}
		
		// 查找路由
		AcqTerminal at = route(request.getString(41), request.getString(42),
				new BigDecimal("0"),cb);
		// 创建订单
		TransInfo ti = buildTransInfo(request, at, cb, TransType.QUERY);
		createOrder(ti);
		// 交易
        try {
            responseMsg = acqRequest.query(request, request.getString(35),
                    request.getString(36), clearPin(request),at);
        } catch (IOException e) {
            e.printStackTrace();
            //记录交易警告
            try {
                transWarnService.warn(request);
            } catch (Exception e1) {//此异常不用捕捉
                e1.printStackTrace();
            }
            //重新抛出异常（防止破坏之前业务逻辑）
            throw new IOException(e);
        }

        // 更新订单
		if ("00".equals(responseMsg.getString(39))) {
			updateOrder(TransStatus.SUCCESS, request, responseMsg);
		} else {
			updateOrder(TransStatus.FAIL, request, responseMsg);
		}

		rm.setAcqResponseCode(responseMsg.getString("39"));
		rm.setIosMsg(responseMsg);
		rm.setOrderNo(ti.getOrderNo());
		return rm;
	}
}
