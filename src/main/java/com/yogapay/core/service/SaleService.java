package com.yogapay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.yogapay.core.domain.*;
import junit.framework.Assert;
import org.jpos.iso.ISOMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yogapay.core.enums.TransStatus;
import com.yogapay.core.enums.TransType;
import com.yogapay.mobile.domain.AcqTerminal;
import com.yogapay.mobile.server.AcqRequest;
import com.yogapay.mobile.utils.FinanceUtil;

/**
 * 消费
 * 
 * @author donjek
 * 
 */
@Service
public class SaleService extends BizBaseService {
    Logger log = LoggerFactory.getLogger(SaleService.class);
	@Resource
	private AcqRequest acqRequest;

    @Resource
    private CacheService cacheService;

    @Resource
    private TransWarnService transWarnService;

	public ResponseMsg doTrx(ISOMsg request) throws Exception {
		
		ResponseMsg rm = new ResponseMsg();
		ISOMsg responseMsg = null;
		BigDecimal amount = FinanceUtil.transAmount(request.getString(4));
		// 创建订单
		CardBin cb = cardBin(FinanceUtil.getAccountNo(request));
		
		//风控规则
		responseMsg = saleRiskValue(request, cb);
		if(responseMsg.hasField(39)){
			rm.setAcqResponseCode(responseMsg.getString("39"));
			rm.setIosMsg(responseMsg);
			return rm;
		}
		
		// 查找路由
		AcqTerminal at = route(request.getString(41), request.getString(42),
				FinanceUtil.transAmount(request.getString(4)), cb);
		TransInfo ti = buildTransInfo(request, at, cb, TransType.SALE);
		createOrder(ti);
		
		try {
			responseMsg = acqRequest
					.sale(request, request.getString(35), request.getString(36),
							clearPin(request), amount, at);
		} catch (IOException e) {
			e.printStackTrace();
            //记录交易警告
            transWarnService.warn(request);
		}
		// 发冲正
		if (null == responseMsg) {
			for (int i = 0; i < 5; i++) {
				if (null != sysRollback(request, at, cb)) {
					break;
				}
			}
		}
		if (null != responseMsg) {
			rm.setAcqResponseCode(responseMsg.getString("39"));
			rm.setIosMsg(responseMsg);
			if ("00".equals(responseMsg.getString(39))) {
				int r = updateOrder(TransStatus.SUCCESS, request, responseMsg);
				if (r == 0) {
					// 发冲正
					for (int i = 0; i < 5; i++) {
						if (null != sysRollback(request, at, cb)) {
							break;
						}
					}
					throw new SocketTimeoutException();
				}else{
			    	//账户：进账，积分：增加
                    String merchantNo = request.getString(42);
                    String refNo = "000000000000";
                    if (responseMsg.hasField(37)) {
                        refNo = responseMsg.getString(37);
                    }
                    TransInfo transInfo =  findTransInfoBySrc(request.getString(41), merchantNo, request
                            .getString(60).substring(2, 8), request.getString(11), refNo, TransType.SALE,TransStatus.SUCCESS,  responseMsg.getString(13));

                    Map<String,String> params = new HashMap<String, String>();
                    params.put("merchantNo",merchantNo);
                    MerchantAccount account = findAccountFirst(params);
                    if(null==account){
                        //创建 账户
                        account = new MerchantAccount();
                        account.setMerchantNo(merchantNo);
                        account.setAmount(new BigDecimal("0"));
                        account.setAvaliableAmount(new BigDecimal("0"));
                        account.setFreezeAmount(new BigDecimal("0"));
                        account.setStatus("1");
                        createAccount(account);
                        account = findAccountFirst(params);
                    }
                    //已存在账户
                    if(null!=account){
                        MerchantAccountInfo accountInfo = new MerchantAccountInfo();
                        accountInfo.setAccountId(account.getId());
                        accountInfo.setOperateType("in");
                        BigDecimal fee = transInfo.getMerchantFee();
                        if(null!=fee){
                            accountInfo.setAmount(amount.subtract(fee).setScale(3,RoundingMode.HALF_UP));
                        }else{
                            accountInfo.setAmount(amount);
                        }
                        //成功创建明细才更新账户
                        boolean isSuccess = createAccountInfo(accountInfo);
                        //测试
                        Assert.assertTrue(isSuccess);
                        if(isSuccess){
                            Map<String,String> ifParams = new HashMap<String,String>();
                            Map<String,String> paramList = new HashMap<String,String>();
                            ifParams.put("merchantNo",merchantNo);
                            paramList.put("inAmount",accountInfo.getAmount().toString());
                            updateAccount(ifParams,paramList);

                            //积分
                            MerchantJifen jifen = findJifenFirst(params);
                            if(null==jifen) {
                                SysDict sysDict = cacheService.findByNameKey("MERCHANT_ACCOUNT","JIFEN_RULE");
                                log.info("sysDic:{}",sysDict);
                                log.info("getDictValue:{}",sysDict.getDictValue());
                                //创建积分
                                jifen = new MerchantJifen();
                                jifen.setMerchantNo(merchantNo);
                                jifen.setJifen(new BigDecimal("0"));
                                jifen.setRule(sysDict==null?"1:1":sysDict.getDictValue());
                                isSuccess = createJifen(jifen);
                                //测试
                                Assert.assertTrue(isSuccess);
                                jifen = findJifenFirst(params);
                            }
                            if(null!=jifen){
                                MerchantJifenInfo jifenInfo = new MerchantJifenInfo();
                                jifenInfo.setJifenId(jifen.getId());
                                BigDecimal jifenPoint;
                                //计算积分
                                {
                                    log.info("rule:{}",jifen.getRule());
                                    String[] args = jifen.getRule() .split(":");
                                    jifenPoint = new BigDecimal(args[0]).divide(new BigDecimal(args[1])).multiply(amount).setScale(3, RoundingMode.HALF_UP);
                                    System.out.println("jifenPoint:"+jifenPoint);
                                }
                                jifenInfo.setJifen(jifenPoint);
                                jifenInfo.setOperateType("in");
                                //创建积分明细
                                createJifenInfo(jifenInfo);

                                ifParams = new HashMap<String,String>();
                                paramList = new HashMap<String,String>();
                                ifParams.put("merchantNo",merchantNo);
                                {
                                    String[] args = jifen.getRule() .split(":");
                                    paramList.put("inAmount",jifenPoint.toString());
                                }
                                //更新积分
                                updateJifen(ifParams,paramList);
                            }
                        }
                    }
                }
			} else {
				updateOrder(TransStatus.FAIL, request, responseMsg);
			}

		}
		rm.setOrderNo(ti.getOrderNo());
		return rm;
	}
}
