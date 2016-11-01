package com.yogapay.mobile.utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.utils.Dao;
public class Commons {
	
	//QPOS解密磁道信息
	public static String decodeTracks(String trackKsn, String bdk, String encTracks){
		byte[] trackksnByte = ISOUtil.hex2byte(trackKsn);
		byte[] bdkByte = ISOUtil.hex2byte(bdk);
		//第一步根据ksn和bdk获取到IPEK
		byte[] ipekByte = generateIPEK(trackksnByte, bdkByte);

		//第二步根据ksn和IPEK获取DUKPT Key
		byte[] dukptByte = getDUKPTKey(trackksnByte, ipekByte);

		//第三步根据DUKPT KEY得到Data Key Variant
		byte[] dataKeyVariantByte = getDataKeyVariant(dukptByte);
		int count = encTracks.length() / 16;
		String tracks2 = "";
		for(int i=0;i<count;i++){
		    String temp = encTracks.substring(i*16, (i+1)*16);
//		    System.out.println("第"+i+"组密文磁道="+temp);
//		    System.out.println("第"+i+"组解密之后的磁道="+ISOUtil.hexString(JCEHandler.decryptData(ISOUtil.hex2byte(temp), dataKeyVariantByte)));
		    tracks2 += ISOUtil.hexString(JCEHandler.decryptData(ISOUtil.hex2byte(temp), dataKeyVariantByte));
		}
		System.out.println("tracks2="+tracks2);
		String track2Data = tracks2.substring(0, tracks2.indexOf("F"))
				.replace("D", "=");
		return track2Data;
	}
	
	//QPOS解密密码块
	public static String decodePinBlock(String pinKsn, String bdk,  String cardNo, String encPinBlock){
		
		byte[] pinKsnByte = ISOUtil.hex2byte(pinKsn);
		byte[] bdkByte = ISOUtil.hex2byte(bdk);
		//第一步根据ksn和bdk获取到IPEK
		byte[] ipekByte = generateIPEK(pinKsnByte, bdkByte);

		//第二步根据ksn和IPEK获取DUKPT Key
		byte[] dukptByte = getDUKPTKey(pinKsnByte, ipekByte);

		//第三步根据DUKPT KEY得到Data Key Variant
		byte[] pinKeyVariant = getPinKeyVariant(dukptByte);
		String clearPk = ISOUtil.hexString(pinKeyVariant);
		String pinBlock = FinanceUtil.unPinBlock(cardNo, encPinBlock,
				clearPk);
		return pinBlock;
	}
	
	public static byte[] generateIPEK(byte[] ksn, byte[] bdk) {
		byte[] ipekByte = new byte[16];
		byte[] temp = new byte[8];
		byte[] temp2 = new byte[8];
		byte[] keyTemp = new byte[16];
		System.arraycopy(bdk, 0, keyTemp, 0, 16);
		System.arraycopy(ksn, 0, temp, 0, 8);
		temp[7] &= 0xE0;
		temp2 = JCEHandler.encryptData(temp, keyTemp);
		System.arraycopy(temp2, 0, ipekByte, 0, 8);
		keyTemp[0] ^= 0xC0;
		keyTemp[1] ^= 0xC0;
		keyTemp[2] ^= 0xC0;
		keyTemp[3] ^= 0xC0;
		keyTemp[8] ^= 0xC0;
		keyTemp[9] ^= 0xC0;
		keyTemp[10] ^= 0xC0;
		keyTemp[11] ^= 0xC0;
		temp2 = JCEHandler.encryptData(temp, keyTemp);
		System.arraycopy(temp2, 0, ipekByte, 8, 8);
		System.out.println("IPEK=" + ISOUtil.hexString(ipekByte));
		return ipekByte;
	}

	public static byte[] getDUKPTKey(byte[] ksn, byte[] ipek) {
		byte[] dukptKeyByte = new byte[16];
		byte[] cnt = new byte[3];
		byte[] temp3 = new byte[8];
		int shift;
		System.arraycopy(ipek, 0, dukptKeyByte, 0, 16);
		cnt[0] = (byte) (ksn[7] & 0x1F);
		cnt[1] = ksn[8];
		cnt[2] = ksn[9];
		System.arraycopy(ksn, 2, temp3, 0, 6);
		temp3[5] &= 0xE0;
		shift = 0x10;
		while (shift > 0) {
			if ((cnt[0] & shift) > 0) {
				temp3[5] |= shift;
				NRKGP(dukptKeyByte, temp3);
			}
			shift >>= 1;
		}
		shift = 0x80;
		while (shift > 0) {
			if ((cnt[1] & shift) > 0) {
				temp3[6] |= shift;
				NRKGP(dukptKeyByte, temp3);
			}
			shift >>= 1;
		}
		shift = 0x80;
		while (shift > 0) {
			if ((cnt[2] & shift) > 0) {
				temp3[7] |= shift;
				NRKGP(dukptKeyByte, temp3);
			}
			shift >>= 1;
		}
		System.out.println("dukptKeyByte=" + ISOUtil.hexString(dukptKeyByte));
		return dukptKeyByte;
	}

	static void NRKGP(byte[] key, byte[] ksn) {
		byte[] temp = new byte[8];
		byte[] key_l = new byte[8];
		byte[] key_r = new byte[8];
		byte[] key_temp = new byte[8];
		int i;
		System.arraycopy(key, 0, key_temp, 0, 8);
		for (i = 0; i < 8; i++) {
			temp[i] = (byte) (ksn[i] ^ key[8 + i]);
		}
		key_r = JCEHandler.encryptData(temp, key_temp);
		for (i = 0; i < 8; i++) {
			key_r[i] ^= key[8 + i];
		}
		key_temp[0] ^= 0xC0;
		key_temp[1] ^= 0xC0;
		key_temp[2] ^= 0xC0;
		key_temp[3] ^= 0xC0;
		key[8] ^= 0xC0;
		key[9] ^= 0xC0;
		key[10] ^= 0xC0;
		key[11] ^= 0xC0;
		for (i = 0; i < 8; i++) {
			temp[i] = (byte) (ksn[i] ^ key[8 + i]);
		}
		key_l = JCEHandler.encryptData(temp, key_temp);
		for (i = 0; i < 8; i++)
			key[i] = (byte) (key_l[i] ^ key[8 + i]);
		System.arraycopy(key_r, 0, key, 8, 8);
	}

	public static byte[] getDataKeyVariant(byte[] dukpt) {
		dukpt[5] ^= 0xFF;
		dukpt[13] ^= 0xFF;
		System.out.println("dataKeyVariant=" + ISOUtil.hexString(dukpt));
		return dukpt;
	}

	public static byte[] getPinKeyVariant(byte[] dukpt) {
		dukpt[7] ^= 0xFF;
		dukpt[15] ^= 0xFF;
		System.out.println("dataKeyVariant=" + ISOUtil.hexString(dukpt));
		return dukpt;
	}
}
