package com.yogapay.mobile.service;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.security.rsa.RSAUtils;
import com.yogapay.core.utils.Dao;
import com.yogapay.mobile.domain.Receive;
import com.yogapay.mobile.utils.Constants;

/**
 * 公共方法
 * 
 * @author donjek
 * 
 */
@Service
public class CommonService {

	@Resource
	private Dao dao;

	// 获取配置项值
	public String getValue(String key) {
		String sql = "select params_value from sys_config where params_key=?";
		try {
			Map<String, Object> values = dao.findFirst(sql, key);
			return values.get("params_value").toString();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public CardBin cardBin(String cardNo) {
		String sql = "select * from card_bin c  where  c.card_length = length(?) AND c.verify_code = left(?,  c.verify_length)  ";
		try {
			CardBin cb = dao.findFirst(CardBin.class, sql, new Object[] {
					cardNo, cardNo });
			if (null == cb) {
				cb = new CardBin();
			}
			cb.setCardNo(cardNo);
			return cb;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	// 解密数据
	public String decodedData(String ecodedData) {
		try {
			byte[] decodedData = RSAUtils.decryptByPrivateKey(
					ISOUtil.hex2byte(ecodedData), Constants.RSA_PRI_KEY);
			return new String(decodedData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public CardBin cardBin(Receive receive) {
		String track2Data = "";
		if (StringUtils.isEmpty(receive.getRandomNumber())) {
			track2Data = decodedData(StringUtils.trim(receive.getTrack()));
		} else if (receive.getRandomNumber().equals("123456789")) {
			// 中磁
			String track = receive.getTrack();
			String data = "";
			if ((track.length() % 16) != 0) {
				int tmp = track.length() % 16;
				int data_len = track.length() - 80 - tmp;
				data = track.substring(32, 32 + data_len);
			} else {
				int data_len = track.length() - 80;
				data = track.substring(32, 32 + data_len);
			}
			byte[] b1 = Utils.hexStringToBytes(data);
			byte[] b2 = Utils.hexStringToBytes(Constants.IMAG_KEY);
			String result2 = "";
			try {
				result2 = new String(Utils.decodeTripleDES(b1, b2));
				System.out.println(result2 + "======result2==");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			track2Data = result2.substring(result2.indexOf(";") + 1,
					result2.lastIndexOf("?"));

		}else {
			// 艾创
			byte[] b1 = ISOUtil.hex2byte(receive.getRandomNumber());
			byte[] b2 = ISOUtil.hex2byte("FFFFFFFFFFFFFFFF");
			String left = JCEHandler.encryptData(receive.getRandomNumber(),
					Constants.ITRON_KEY);
			String right = ISOUtil.hexString(ISOUtil.xor(b1, b2));
			right = JCEHandler
					.encryptData(ISOUtil.hexString(ISOUtil.xor(b1, b2)),
							Constants.ITRON_KEY);
			String result = JCEHandler.decryptData(
					StringUtils.trim(receive.getTrack()), left + right);
			track2Data = result.substring(0, result.indexOf("F")).replace("D",
					"=");

		}
		String cardNo = track2Data.split("=")[0];

		String sql = "select * from card_bin c  where  c.card_length = length(?) AND c.verify_code = left(?,  c.verify_length)  ";
		try {
			CardBin cb = dao.findFirst(CardBin.class, sql, new Object[] {
					cardNo, cardNo });
			if (null == cb) {
				cb = new CardBin();
			}
			cb.setCardNo(cardNo);
			cb.setTrackMsg(track2Data);
			return cb;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
}
