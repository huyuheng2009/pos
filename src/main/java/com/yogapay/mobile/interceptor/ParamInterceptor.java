package com.yogapay.mobile.interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.yogapay.core.utils.Md5;
import com.yogapay.mobile.controller.BaseController;
import com.yogapay.mobile.utils.Constants;

public class ParamInterceptor extends BaseController implements
		HandlerInterceptor {

	private static final Logger log = LoggerFactory
			.getLogger(ParamInterceptor.class);

	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {

	}

	public TreeMap<String, String> sortParameter(HttpServletRequest arg0) {
		TreeMap<String, String> tm = new TreeMap<String, String>();
		Enumeration e = arg0.getParameterNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String value = arg0.getParameter(name);
			tm.put(name, value);
		}
		return tm;
	}

	public String hmac(TreeMap<String, String> map) {
		Set<String> key = map.keySet();
		String hmac = "";
		StringBuffer sbb = new StringBuffer();
		for (String k : key) {
			if (!k.equals("hmac")) {
				sbb.append(map.get(k));
			}
		}
		if (sbb.length() != 0) {
			hmac = Md5.md5Str(sbb.toString()+Constants.RSA_PUB_KEY);
		}
		return hmac;
	}

	@Override
	public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2) throws Exception {
		StringBuffer sb = new StringBuffer();
		String oldHmac = "";
		Enumeration e = arg0.getHeaderNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String value = arg0.getHeader(name);
			sb.append(name + ":" + value + " \n");

		}
		sb.append("------------------------\n");
		e = arg0.getParameterNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String value = arg0.getParameter(name);
			if (name.equals("hmac")) {
				oldHmac = value;
			}
			sb.append(name + ":" + value + " \n");
		}
		log.info("request params: \n{}", sb.toString());
		String newHmac = hmac(sortParameter(arg0));
		if (StringUtils.isNotEmpty(newHmac) && StringUtils.isNotEmpty(oldHmac)) {
			if (!newHmac.equals(oldHmac)) {
				log.info("hmac error,ip:{}", arg0.getServerName());
				return false;
			}
		}
		return true;
	}
}
