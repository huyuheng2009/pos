package com.yogapay.core.exception;

/**
 * 业务类型不匹配或无次交易类型
 * 
 * @author dj
 * 
 */
public class RouteNotFindException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RouteNotFindException() {
		super();
	}

	public RouteNotFindException(String message) {
		super(message);
	}
}
