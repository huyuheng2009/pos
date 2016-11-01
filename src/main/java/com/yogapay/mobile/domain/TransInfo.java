package com.yogapay.mobile.domain;

import java.math.BigDecimal;
import java.util.Date;

public class TransInfo {

	private Long id;
	private String userName;
	private String transType;
	private String transStatus;
	private BigDecimal transAmnount;
	private BigDecimal transFee;
	private String acqMerchantNo;
	private String acqMerchantTerminal;
	private BigDecimal acq_fee;
	private String cardNo;
	private String cardType;
	private String cardName;
	private BigDecimal profit;
	private String syncNo;
	private Date createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTransType() {
		return transType;
	}

	public void setTransType(String transType) {
		this.transType = transType;
	}

	public String getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
	}

	public BigDecimal getTransAmnount() {
		return transAmnount;
	}

	public void setTransAmnount(BigDecimal transAmnount) {
		this.transAmnount = transAmnount;
	}

	public BigDecimal getTransFee() {
		return transFee;
	}

	public void setTransFee(BigDecimal transFee) {
		this.transFee = transFee;
	}

	public String getAcqMerchantNo() {
		return acqMerchantNo;
	}

	public void setAcqMerchantNo(String acqMerchantNo) {
		this.acqMerchantNo = acqMerchantNo;
	}

	public String getAcqMerchantTerminal() {
		return acqMerchantTerminal;
	}

	public void setAcqMerchantTerminal(String acqMerchantTerminal) {
		this.acqMerchantTerminal = acqMerchantTerminal;
	}

	public BigDecimal getAcq_fee() {
		return acq_fee;
	}

	public void setAcq_fee(BigDecimal acq_fee) {
		this.acq_fee = acq_fee;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public String getSyncNo() {
		return syncNo;
	}

	public void setSyncNo(String syncNo) {
		this.syncNo = syncNo;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
