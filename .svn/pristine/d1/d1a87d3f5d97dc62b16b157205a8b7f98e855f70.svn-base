package com.yogapay.mobile.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.yogapay.mobile.domain.AcqResult;
import com.yogapay.mobile.domain.Receive;
import com.yogapay.mobile.enums.TransType;
import com.yogapay.mobile.service.CodFinanceService;
import com.yogapay.mobile.service.CommonService;
import com.yogapay.mobile.service.MobileFinanceService;
import com.yogapay.mobile.utils.Constants;
@Controller
@RequestMapping("/cod")
public class CodController extends BaseController {
	
	private static final Logger log = LoggerFactory
			.getLogger(MobileFinanceService.class);
	
	@Resource
	private CommonService commonService;
	@Resource
	private CodFinanceService codFinanceService;
	private Map<Object, Object> result = null;
	
	// 收款
	@RequestMapping(value = "payment")
	public void payment(HttpServletResponse response,
			@RequestParam Map<String, String> params, Receive receive,
			BigDecimal amount) {
		log.info("物流收款");
		try {
			AcqResult ar = codFinanceService.payment(receive, amount);
			Map<Object, Object> content = buildAcqResult(receive, ar);
			content.put("amount", amount);
			content.put("type", TransType.收款.toString());
			if (null != ar.getIsoMsg()) {
				content.put("merchantNo", ar.getIsoMsg().getString(42));
				String time = ar.getIsoMsg().getString(12);
				String date = ar.getIsoMsg().getString(13);
				content.put("transDate", date);
				content.put("transTime", time);
				content.put("batchNo", ar.getIsoMsg().getString(60).substring(2, 8));
				content.put("referenceNo", ar.getIsoMsg().getString(37));
				content.put("authNo", ar.getIsoMsg().getString(38));
			}

			if (ar.getResponseCode().equals("00")) {
				result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.FINANCE_ERROR, null);
		}
		log.info("result="+JSON.toJSONString(result));
		outJson(JSON.toJSONString(result), response);
	}
	
	// 构造结果返回
		public Map<Object, Object> buildAcqResult(Receive receive, AcqResult ar) {
			Map<Object, Object> content = new HashMap<Object, Object>();
			content.put("cardNo", ar.getCb().getCardNo());
			content.put("cardName", ar.getCb().getCardName());
			content.put("bankName", ar.getCb().getBankName());
			content.put("cardType", ar.getCb().getCardType());
			content.put("transAmount", ar.getAmount());
			content.put("responseCode", ar.getResponseCode());
			content.put("responseMsg", ar.getResponseMsg());

			return content;
		}
		
		// 构造结果返回
		public Map<Object, Object> buildResult(String errorCode, String msg,
				Map<Object, Object> content) {
			Map<Object, Object> head = new HashMap<Object, Object>();
			head.put("status", errorCode);
			head.put("error", msg);

			Map<Object, Object> result = new HashMap<Object, Object>();
			result.put("head", head);
			result.put("content", content);
			return result;
		}
}
