package com.yogapay.core.server;

import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.jpos.q2.Q2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用WEB方式启动时，作为启动入口
 * 
 * @author dj
 * 
 */
public class RunServlet extends HttpServlet {
	private static Logger log = LoggerFactory.getLogger(RunServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		URL path = Thread.currentThread().getContextClassLoader()
				.getResource("");
        new Q2(path.getPath() + "server").start();
//		new Q2("F:\\Program Files(x86)\\Workspaces\\MyEclipse 10\\pos\\target\\classes\\" + "server").start();
	}
}
