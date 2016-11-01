package com.yogapay.core.server;

import java.math.BigInteger;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yogapay.core.utils.Dao;

/**
 * 生成POS终端号
 * 
 * @author donjek
 * 
 */
public class GenSyncNo implements ApplicationContextAware {
	private static GenSyncNo instance = null;
	private static ApplicationContext applicationContext; // Spring应用上下文环境

	private GenSyncNo() {

	}

	public static synchronized GenSyncNo getInstance() {
		if (null == instance) {
			instance = new GenSyncNo();
		}
		return instance;
	}

	// 商户编号
	public synchronized String getNextMerchantNo() throws SQLException {
		return getNext("merchant_no_seq", "100000000000000");
	}

	// 终端号
	public synchronized String getNextTerminalNo() throws SQLException {
		return getNext("terminal_no_seq", "10000000");
	}

	// 订单号
	public synchronized String getNextOrderNo() {
		String str = "10000000";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			str = sdf.format(new Date()) + getNext("order_no", "10000000");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return str;
	}

	// 代理商编号
	public synchronized String getNextAgentNo() throws SQLException {
		return getNext("agent_no_seq", "1000");
	}

	// 获取POS机分配的批次号
	public synchronized String getNextPosAllotNo() throws SQLException {
		return getNext("terminal_allot_seq", "1000");
	}

	// 获取系统发冲正的流水号
	public synchronized String getNextSysVoucherNo() throws SQLException {
		return getNext("terminal_sys_voucher_seq", "100000");
	}

	// 获取POS机参考号
	public synchronized String getNextPosReferNo() throws SQLException {
		return getNext("pos_refer_no", "100000000000");
	}

	private synchronized String getNext(String seqName, String defValue)
			throws SQLException {
		Dao dao = applicationContext.getBean(Dao.class);
		Map<String, Object> map = dao.findFirst("select nextval('" + seqName
				+ "') as t");

		BigInteger v = new BigInteger(defValue);
		Object t = map.get("t");
		if (null != t) {
			String src = t.toString();
			v = new BigInteger(src);
			v.add(new BigInteger("1"));
		}

		return v.toString();
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;

	}

}
