package com.yogapay.mobile.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.mobile.domain.AcqTerminal;

public class FinanceUtil {

	// pos上送上来的金额转换成bigdecimal
	public static BigDecimal transAmount(String amount) {
		if (StringUtils.isEmpty(amount)) {
			return new BigDecimal("0");
		} else {
			String str = amount
					.substring(amount.length() - 12, amount.length());

			BigDecimal b = new BigDecimal(str).divide(new BigDecimal(100))
					.setScale(3, RoundingMode.HALF_UP);
			return b;
		}

	}// pos上送来的pinblock转换成收单机构的pinblock

	public static String transPinBlock(String accountNo, String pinBlock,
			PosTerminal posTerminal, AcqTerminal acqTerminal) {
		// 先进行解密
		String clearPk = JCEHandler.decryptData(posTerminal.getTmkTpk(),
				posTerminal.getTmk());
		String clearPin = unPinBlock(accountNo, pinBlock, clearPk);
		// 在进行加密
		clearPk = JCEHandler.decryptData(acqTerminal.getTmkTpk(),
				acqTerminal.getTmk());
		return pinBlock(accountNo, clearPin, clearPk);
	}

	public static String getAccountNo(ISOMsg request) {
		// 账户
		String accountNo = request.getString(35);
		return getAccountNo(accountNo);
	}

	public static String getAccountNo(String tracnData) {
		// 账户
		String accountNo = tracnData.replace("D", "=");
		return accountNo = accountNo.substring(0, accountNo.indexOf("="));
	}

	// 根据明文pk进行密码解密
	public static String unPinBlock(String accountNo, String pinBlock,
			String clearPk) {
		String clearPin = "000000";
		if (StringUtils.isNotEmpty(pinBlock)) {
			String dest = JCEHandler.decryptData(pinBlock, clearPk);
			accountNo = accountNo.substring(accountNo.length() - 13,
					accountNo.length() - 1);

			byte[] x = ISOUtil.xor(ISOUtil.hex2byte(dest),
					ISOUtil.hex2byte("0000" + accountNo));
			clearPin = ISOUtil.hexString(x);
			if (clearPin.length() == 16) {
				clearPin = clearPin.substring(2, clearPin.indexOf("F"));
			}

		}
		return clearPin;
	}

	// 根据明文的pk进行密码的加密
	public static String pinBlock(String accountNo, String pin, String clearPk) {
		accountNo = accountNo.substring(accountNo.length() - 13,
				accountNo.length() - 1);
		int pinLen = pin.length();
		String len = StringUtils.leftPad(pinLen + "", 2, "0");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 14 - pinLen; i++) {
			sb.append("F");
		}
		byte[] x = ISOUtil.xor(ISOUtil.hex2byte(len + pin + sb.toString()),
				ISOUtil.hex2byte("0000" + accountNo));
		String pinblock = JCEHandler.encryptData(ISOUtil.hexString(x), clearPk);
		return pinblock;
	}

}
