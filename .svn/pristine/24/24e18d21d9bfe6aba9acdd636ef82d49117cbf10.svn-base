package com.yogapay.core.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.PosMerchant;
import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.utils.Dao;

/**
 * POS机具查询
 * 
 * @author donjek
 * 
 */
@Service
public class PosService {

	@Resource
	private Dao dao;

	public PosTerminal findByTno(String merchantNo, String terminalNo)
			throws SQLException {
		PosTerminal pos = null;
		String sql = "select * from pos_terminal where terminal_no=? and merchant_no=?";
		pos = dao.findFirst(PosTerminal.class, sql, new Object[] { terminalNo,
				merchantNo });
		return pos;
	}

	public PosTerminal findBySn(String sn) throws SQLException {
		PosTerminal pos = null;
		String sql = "select * from pos_terminal where sn=?";
		pos = dao.findFirst(PosTerminal.class, sql, sn);
		return pos;
	}

	public PosMerchant findByMno(String merchantNo) throws SQLException {
		PosMerchant pos = null;
		String sql = "select * from pos_merchant where merchant_no=?  ";
		pos = dao
				.findFirst(PosMerchant.class, sql, new Object[] { merchantNo });
		return pos;
	}

	// 更新终端信息
	public void update(PosTerminal posTerminal) throws SQLException {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		if (StringUtils.isNotEmpty(posTerminal.getTmkTpk())) {
			sb.append(" tmk_tpk=?,");
			params.add(posTerminal.getTmkTpk());
		}
		if (StringUtils.isNotEmpty(posTerminal.getTmkTak())) {
			sb.append(" tmk_tak=?,");
			params.add(posTerminal.getTmkTak());
		}
		if (StringUtils.isNotEmpty(posTerminal.getBatchNo())) {
			sb.append(" batch_no=?,");
			params.add(posTerminal.getBatchNo());
		}
		if (StringUtils.isNotEmpty(posTerminal.getVoucherNo())) {
			sb.append(" voucher_no=?,");
			params.add(posTerminal.getVoucherNo());
		}
        {
            sb.append(" last_check_in=?,");
            params.add(new Date());
        }
		sb.setLength(sb.length() - 1);
//		params.add(new Date());
		params.add(posTerminal.getTerminalNo());
		params.add(posTerminal.getMerchantNo());
		String sql = "update pos_terminal set " + sb.toString()
				+ " where terminal_no=? and merchant_no=?";
		dao.update(sql, params.toArray());
	}

	// 验证pos是否可用，商户是否开通，true正常，false无效商户
	public boolean isValidation(String merchantNo, String terminalNo)
			throws SQLException {
		String sql = "select count(*)  as cnt from pos_merchant m,pos_terminal t where m.merchant_no=t.merchant_no and m.open_status=1 and t.open_status=1 and t.terminal_no=? and m.merchant_no=?";
		Object[] params = { terminalNo, merchantNo };
		Map<String, Object> result = dao.findFirst(sql, params);
		if (StringUtils.isEmpty(result.get("cnt").toString())
				|| "0".equals(result.get("cnt").toString())) {
			return false;
		}
		return true;
	}

}
