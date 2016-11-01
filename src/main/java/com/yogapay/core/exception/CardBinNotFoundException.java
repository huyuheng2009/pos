package com.yogapay.core.exception;

/**
 * 无法找到卡bin
 * 
 * @author dj
 * 
 */
public class CardBinNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public CardBinNotFoundException() {
		super();
	}

	public CardBinNotFoundException(String message) {
		super(message);
	}

	public CardBinNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public CardBinNotFoundException(Throwable cause) {
		super(cause);
	}
}