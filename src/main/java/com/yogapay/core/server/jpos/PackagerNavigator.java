package com.yogapay.core.server.jpos;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * isopackager 导航器, 仅用于实例化和索引系统中所有的 packager 定义
 * 				该包中放置所有的 ISO8583 packager 定义 
 * @author dj
 *
 */
public final class PackagerNavigator {
	
	private static final Logger log = LoggerFactory.getLogger(PackagerNavigator.class);
	
	/**
	 * 标准版, 金融POS, 接入规范
	 */
	public static final ISOPackager COMMON_PACKAGER = load("chinaums_pos.xml");
	
	private static ISOPackager load(String name) {
		try {
			log.info("load:{}",name);
			return new GenericPackager(PackagerNavigator.class.getResourceAsStream("/packager/" + name));
		} catch (Exception e) {
			log.error("Load packager error! ", e);
		}
		
		return null;
	}

	 
	public final static void init() {
		// do notinng
		// this init method do nothing, just load class to JVM
	}

}
