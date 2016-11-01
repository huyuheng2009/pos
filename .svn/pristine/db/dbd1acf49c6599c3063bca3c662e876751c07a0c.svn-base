package com.yogapay.core.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Todo: 应用变量加载
 * @Author: zhanggc
 * @Date: 2014-3-20
 */
public class ConstantsLoader {
	private static Properties p;
	public static enum Path{
		APPPATH("/config/pros.properties");
		private String value;
		Path(String value){this.value = value;}
		public String Value(){
			return this.value;
		}
	}

	static{
		 p = new Properties();
		 try {
			p.load(ConstantsLoader.class.getResourceAsStream(Path.APPPATH.Value()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void init(Path path){
		 p = new Properties();
		try {
			p.load(ConstantsLoader.class.getResourceAsStream(path.Value()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String key){
		if(p.size()==0) return null;
		return p.get(key)==null?null:p.get(key).toString();
	}
	
	public static Map<String,String> getPropertys(){
		Map<String,String> map = new HashMap<String,String>();
		for(Object key:p.keySet()) map.put((String)key, getProperty((String)key));
		return map;
	}
}
