package com.yogapay.mobile.domain;

import com.yogapay.core.domain.ToString;

public class AcqTerminal extends ToString{

	private String acqName;
	// 收单商户终端编号
	private String terminalNo;
	// 收单商户终端tmk明文
	private String tmk;
	// 收单商户终端tmk下tak密文
	private String tmkTak;
	// 收单商户终端tmk下tpk密文
	private String tmkTpk;
	// 收单机构商户编号
	private String merchantNo;
	// 手续费陈本
	private String rate;
	private String batchNo;
	private String voucherNo;

	public String getAcqName() {
		return acqName;
	}

	public void setAcqName(String acqName) {
		this.acqName = acqName;
	}

	public String getTerminalNo() {
		return terminalNo;
	}

	public void setTerminalNo(String terminalNo) {
		this.terminalNo = terminalNo;
	}

	public String getTmk() {
		return tmk;
	}

	public void setTmk(String tmk) {
		this.tmk = tmk;
	}

	public String getTmkTak() {
		return tmkTak;
	}

	public void setTmkTak(String tmkTak) {
		this.tmkTak = tmkTak;
	}

	public String getTmkTpk() {
		return tmkTpk;
	}

	public void setTmkTpk(String tmkTpk) {
		this.tmkTpk = tmkTpk;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getVoucherNo() {
		return voucherNo;
	}

	public void setVoucherNo(String voucherNo) {
		this.voucherNo = voucherNo;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

}
