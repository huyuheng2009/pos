package com.yogapay.core.server.tlv;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

import java.io.UnsupportedEncodingException;

/**
 * @author yinheli <me@yinheli.com>
 */
public class GBKTLVMsg {
	private String tag;
	private String value;

	public GBKTLVMsg() {
	}

	public GBKTLVMsg(String tag, String value) {
		this.tag = tag;
		this.value = value;
	}

	public String getTag() {
		return this.tag;
	}

	public String getValue() {
		return this.value;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setValue(String newValue) {
		this.value = newValue;
	}

	public String getTLV()
			throws ISOException {
		return this.tag + getL() + this.value;
	}

	public String getL()
			throws ISOException {
		try {
			String tmp = this.value.getBytes("GBK").length + "";
			return ISOUtil.padleft(tmp, 2, '0');
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}