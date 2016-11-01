/* ==================================================================   
 * Created 2013-11-21����9:35:20 by ֣ΰ
 * ==================================================================  
 *  EMV  Level2
 * Copyright (c) SZZCS S&T Co.ltd ShenZhen, 2012-2013 
 * ================================================================== 
 * �������дż���������޹�˾ӵ�и��ļ���ʹ�á����ơ��޸ĺͷַ������Ȩ
 * �������õ������Ϣ������� <http://www.szzcs.com/cn>
 *
 * SZZCS S&T Co.ltd ShenZhen owns permission to use, copy, modify and 
 * distribute this documentation.
 * For more information on EMV, please 
 * see <http://www.szzcs.com/cn>.  
 * ================================================================== 
 */
package com.yogapay.mobile.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Decrypted {
	// ��ǿ�:%;6222024000075670535=49121202929991818?
	// ���ÿ�:%B5201521270715742^LIN SHENG
	// ^1307101000000000000000521000000?;5201521270715742=13071010000052100000?
	public static String decrypted(String key, String encrypted)
			throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		String data = null;
		if ((encrypted.length() % 16) != 0) {// ����������׼ ����19λ
			int tmp = encrypted.length() % 16;
			int data_len = encrypted.length() - 80 - tmp;
			data = encrypted.substring(32, 32 + data_len);
		} else {// ��ʱ�׼���ÿ�16λ
			int data_len = encrypted.length() - 80;
			data = encrypted.substring(32, 32 + data_len);
		}
		byte[] key_ = Utils.hexStringToBytes(key);
		byte[] data_ = Utils.hexStringToBytes(data);
		return new String(Utils.decodeTripleDES(data_, key_));
	}
}
