package com.yogapay.core.utils;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yogapay.core.domain.SysConfig;

/**
 * 系统缓存，查询尽量使用缓存进行查询，更新数据时记得清空缓存，否则更新的数据无法生效
 * 
 * @author dj
 * 
 */
public class MemoryCache implements ApplicationContextAware {
	private static List<SysConfig> configList = null;
	private static ApplicationContext applicationContext; // Spring应用上下文环境

	/**
	 * 缓存中读取收单机构信息
	 * 
	 * @param enname
	 *            收单机构英文标识
	 * @return AcqOrg
	 */
	public static List<SysConfig> getAllConfig() {
		Dao dao = applicationContext.getBean(Dao.class);
		if (null == configList) {
			try {
				String sql = "select * from sys_config";
				configList = dao.find(SysConfig.class, sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return configList;
	}

	public static SysConfig findByKey(String key) {
		if (null == configList) {
			getAllConfig();
		}
		for (SysConfig c : configList) {
			if (c.getParamsKey().equals(key)) {
				return c;
			}
		}
		return null;

	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;

	}

}
