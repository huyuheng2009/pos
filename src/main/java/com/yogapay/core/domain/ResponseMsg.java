package com.yogapay.core.domain;

import org.jpos.iso.ISOMsg;

public class ResponseMsg {

	private ISOMsg isoMsg;

	private String acqResponseCode;

	private String orderNo;

	public String getAcqResponseCode() {
		return acqResponseCode;
	}

	public void setAcqResponseCode(String acqResponseCode) {
		this.acqResponseCode = acqResponseCode;
	}

	public ISOMsg getIosMsg() {
		return isoMsg;
	}

	public void setIosMsg(ISOMsg iosMsg) {
		this.isoMsg = iosMsg;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

}
