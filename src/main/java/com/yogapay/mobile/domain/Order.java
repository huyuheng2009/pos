package com.yogapay.mobile.domain;

import java.math.BigDecimal;
import java.util.Date;

public class Order {

	private Long id;
	private String userName;
	private String transType;
	private String transStatus;
	private BigDecimal transAmount;
	private String cardNo;
	private Date createTime;
	private String syncNo;
	private String merchantFee;
	private String acqResponseMsg;
	private String acqResponseCode;
	private BigDecimal settleAmount;
	private String settleAccount;
	private String settleName;

	public BigDecimal getSettleAmount() {
		return settleAmount;
	}

	public void setSettleAmount(BigDecimal settleAmount) {
		this.settleAmount = settleAmount;
	}

	public String getSettleAccount() {
		return settleAccount;
	}

	public void setSettleAccount(String settleAccount) {
		this.settleAccount = settleAccount;
	}

	public String getSettleName() {
		return settleName;
	}

	public void setSettleName(String settleName) {
		this.settleName = settleName;
	}

	public String getAcqResponseCode() {
		return acqResponseCode;
	}

	public void setAcqResponseCode(String acqResponseCode) {
		this.acqResponseCode = acqResponseCode;
	}

	public String getAcqResponseMsg() {
		return acqResponseMsg;
	}

	public void setAcqResponseMsg(String acqResponseMsg) {
		this.acqResponseMsg = acqResponseMsg;
	}

	public String getMerchantFee() {
		return merchantFee;
	}

	public void setMerchantFee(String merchantFee) {
		this.merchantFee = merchantFee;
	}

	public String getSyncNo() {
		return syncNo;
	}

	public void setSyncNo(String syncNo) {
		this.syncNo = syncNo;
	}

	public String getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
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

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getTransAmount() {
		return transAmount;
	}

	public void setTransAmount(BigDecimal transAmount) {
		this.transAmount = transAmount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
