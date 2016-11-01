package com.yogapay.core.domain;

public class PosRate {
	private String acqName;
	private String cardType;
	private String merchantNo;
	private String rateType;
	private String baseRate;
	private String topRate;
	private String setpRate;

	public String getAcqName() {
		return acqName;
	}

	public void setAcqName(String acqName) {
		this.acqName = acqName;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getRateType() {
		return rateType;
	}

	public void setRateType(String rateType) {
		this.rateType = rateType;
	}

	public String getBaseRate() {
		return baseRate;
	}

	public void setBaseRate(String baseRate) {
		this.baseRate = baseRate;
	}

	public String getTopRate() {
		return topRate;
	}

	public void setTopRate(String topRate) {
		this.topRate = topRate;
	}

	public String getSetpRate() {
		return setpRate;
	}

	public void setSetpRate(String setpRate) {
		this.setpRate = setpRate;
	}

}
