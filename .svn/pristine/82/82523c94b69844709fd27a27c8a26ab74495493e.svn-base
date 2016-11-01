/**
 * 项目: posboss
 * 包名：com.yogapay.boss.service
 * 文件名: BlackListService
 * 创建时间: 2014/11/27 15:07
 * 支付界科技有限公司版权所有，保留所有权利
 */
package com.yogapay.core.service;

import com.yogapay.core.domain.BlackCard;
import com.yogapay.core.domain.BlackMerchant;
import com.yogapay.core.domain.BlackTerminal;
import com.yogapay.core.utils.Dao;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;

/**
 * @Todo: 黑名单
 * @Author: Zhanggc
 */
@Service
public class BlackListService {

    @Resource
    private Dao dao;

    public int saveMerchant(BlackMerchant merchant) throws SQLException {
        String sql = "insert into black_merchant(merchant_no,create_time) values(?,?)";
        return dao.update(sql,new Object[]{merchant.getMercahntNo(),merchant.getCreateTime()});
    }

    public int saveCard(BlackCard card) throws SQLException {
        String sql = "insert into black_card(card_no,create_time) values(?,?)";
        return dao.update(sql,new Object[]{card.getCardNo(),card.getCreateTime()});
    }

    public int saveTerminal(BlackTerminal terminal) throws SQLException {
        String sql = "insert into black_terminal(terminal_no,create_time) values(?,?)";
        return dao.update(sql,new Object[]{terminal.getTerminalNo(),terminal.getCreateTime()});
    }

    public int delMerchant(String merchantNo) throws SQLException {
        if(StringUtils.isBlank(merchantNo)){return 0;}
        String sql = "delete from black_merchant where merchant_no=?";
        return dao.update(sql, merchantNo);
    }

    public int delCard(String cardNo) throws SQLException {
        if(StringUtils.isBlank(cardNo)){return 0;}
        String sql = "delete from black_card where card_no=?";
        return dao.update(sql, cardNo);
    }

    public int delTerminal(String terminalNo) throws SQLException {
        if(StringUtils.isBlank(terminalNo)){return 0;}
        String sql = "delete from black_terminal where terminal_no=?";
        return dao.update(sql, terminalNo);
    }

    public Page<Map<String,Object>> listMerchant(PageRequest pageRequest){
        String sql = "select * from black_merchant";
        return dao.find(sql,new Object[]{},pageRequest);
    }

    public Page<Map<String,Object>> listCard(PageRequest pageRequest){
        String sql = "select * from black_card";
        return dao.find(sql,new Object[]{},pageRequest);
    }

    public Page<Map<String,Object>> listTerminal(PageRequest pageRequest){
        String sql = "select * from black_terminal";
        return dao.find(sql,new Object[]{},pageRequest);
    }

    public BlackCard findFirstBlackCard(Map<String,Object> params) throws SQLException {
        String sql = "select * from black_card where card_no = ?";
        if(null == params){
            return null;
        }
        return dao.findFirst(BlackCard.class,sql,new Object[]{params.get("cardNo")});
    }

    public BlackTerminal findFirstBlackTerminal(Map<String,Object> params) throws SQLException {
        String sql = "select * from black_terminal where terminal_no = ?";
        if(null == params){
            return null;
        }
        return dao.findFirst(BlackTerminal.class,sql,new Object[]{params.get("terminalNo")});
    }

    public BlackMerchant findFirstBlackMerchant(Map<String,Object> params) throws SQLException {
        String sql = "select * from black_merchant where merchant_no = ?";
        if(null == params){
            return null;
        }
        return dao.findFirst(BlackMerchant.class,sql,new Object[]{params.get("merchantNo")});
    }

}
