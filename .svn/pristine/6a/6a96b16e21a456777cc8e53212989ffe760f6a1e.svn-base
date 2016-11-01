/**
 * 项目: pos
 * 包名：com.yogapay.core.service
 * 文件名: TransWarnService
 * 创建时间: 2014/12/18 15:33
 * 支付界科技有限公司版权所有，保留所有权利
 */
package com.yogapay.core.service;

import com.yogapay.core.domain.MobileMerchant;
import com.yogapay.core.domain.PosMerchant;
import com.yogapay.core.domain.TransInfo;
import com.yogapay.core.domain.TransWarn;
import com.yogapay.core.enums.TransStatus;
import com.yogapay.core.utils.Dao;
import org.apache.commons.lang.StringUtils;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Todo: 交易警告服务
 * @Author: Zhanggc
 */
@Service
public class TransWarnService extends BizBaseService {
    @Resource
    Dao dao;


    //查找指定商户最近一次交易
    public TransInfo findLastTrans(String merchantNo) throws SQLException {
        if (StringUtils.isBlank(merchantNo)) {
            return null;
        }
        String sql = "select * from trans_info where merchant_no=? order by create_time desc";
        return dao.findFirst(TransInfo.class, sql, merchantNo);
    }

    //交易异常警告
    public void warn(ISOMsg request) throws SQLException {
        String merchantNo = request.getString(42);
        TransInfo transInfo = findLastTrans(merchantNo);
        TransWarn transWarn = findFirstTransWarn(merchantNo);
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> ifParams = new HashMap<String, String>();
        //上次交易异常
        if (TransStatus.INIT.toString().equals(transInfo.getTransStatus())) {
            if (null != transWarn) {
                if (null == transWarn.getCounter()) {
                    //0 replace null for next convinient
                    transWarn.setCounter(0);
                }
                //警告器counter超过4次，则发送短信通知商户
                if (4 <= transWarn.getCounter()) {
                    PosMerchant merchant = findMerchant(merchantNo);
                    final List<String> phoneList = new ArrayList<String>();
                    if (null != merchant) {
                        MobileMerchant mobileMerchant = findMobileMerchant(merchantNo);
                        if ("P".equals(merchant.getMerchantType())) {
                            phoneList.add(merchant.getBizPhone());
                        } else if ("M".equals(merchant.getMerchantType()) && null != mobileMerchant) {
                            phoneList.add(mobileMerchant.getMobileNo());
                        } else {
                            //属于异常情况
                            phoneList.add(merchant.getBizPhone());
                            if (null != mobileMerchant) phoneList.add(mobileMerchant.getMobileNo());
                        }
                    }

                    Date now = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    //比较当前时间与（最近一次时间加上间隔时间）大小
                    long compare = transWarn.getLastWarnTime()==null?1:(now.getTime() - (transWarn.getLastWarnTime().getTime() + transWarn.getSeconds() * 1000));
                    if (compare >= 0) {
                        //用池发送短信，降低pos超时风险
                        CacheService.smpools.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //暂时取消商户发送短信
                                    phoneList.clear();
                                    //给技术支持发送警告短信
                                    sendMessage(phoneList);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        ifParams.put("merchantNo", merchantNo);
                        params.put("counter", 0 + "");
                        params.put("lastWarnTime", format.format(now));
                        //当发送短信时候，counter 清零
                        updateTransWarn(params, ifParams);
                    }else{
                        ifParams.put("merchantNo", merchantNo);
                        params.put("counter", (1 + transWarn.getCounter()) + "");
                        //counter increase 1
                        updateTransWarn(params, ifParams);
                    }
                } else {
                    ifParams.put("merchantNo", merchantNo);
                    params.put("counter", (1 + transWarn.getCounter()) + "");
                    //counter increase 1
                    updateTransWarn(params, ifParams);
                }
            } else {
                //不存在则创建，且counter 置1
                transWarn = new TransWarn();
                transWarn.setMerchantNo(merchantNo);
                transWarn.setCounter(1);
                transWarn.setLastWarnTime(null);
                //默认5 minute
                transWarn.setSeconds(5*60);
                saveTransWarn(transWarn);
            }
        } else {
            //上次交易正常，即将清除该商户警告计数器且置1
            ifParams.put("merchantNo", merchantNo);
            params.put("counter", 1 + "");
            updateTransWarn(params, ifParams);
        }
    }

}
