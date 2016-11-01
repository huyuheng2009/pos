/**
 * 
 */
package com.yogapay.core.utils;

import org.apache.commons.httpclient.methods.PostMethod;

/**
 * hdb
 * 2013-5-13 上午10:59:00 
 */
public class PostUTF8Method extends PostMethod {
	
	public PostUTF8Method(String url){
		super(url);
	}
	
	@Override
	public String getRequestCharSet(){
		return "UTF-8";
	}

}
