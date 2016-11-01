package com.yogapay.core.security.rsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.jpos.iso.ISOUtil;

import com.yogapay.mobile.utils.Constants;

public class Test {
	static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD1mecWBLMB1snW3J089PGK/yICyWRzXnheUuIHD756S9g9XT0QqeR2l8k8L946VnTWLm3QmtpkS32c2ejfarvVnzkuJrYZyGZivN2hswz+PRxwresR8n/8NQOJ9hu9XVURL24owRKICQg5pD3lqRVL0MFxW+BJB/BZn+uSUFQMIwIDAQAB";
	static String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPWZ5xYEswHWydbcnTz08Yr/IgLJZHNeeF5S4gcPvnpL2D1dPRCp5HaXyTwv3jpWdNYubdCa2mRLfZzZ6N9qu9WfOS4mthnIZmK83aGzDP49HHCt6xHyf/w1A4n2G71dVREvbijBEogJCDmkPeWpFUvQwXFb4EkH8Fmf65JQVAwjAgMBAAECgYAW56OFiiqnoUBxqWGArddY/zJM0DtuBwFyyogJ4I4DGc+w6WEojK+h38YEtvIivq1mzC2xpr93WxL77dap/2pE8y1ss2OVN2aPHbSdkGMy/BDQn2USJbtr8CC1DIL1a7NPWWD8dN8yDf3lS0EILin38ZzLkepEyVQS27GigQREAQJBAP9Julqbmba5M4M0YAtsa0l0DCTszEijnPg3A4nychsKWPROovkZlNaksX9/W2rcE+3JmxDBIZI1TvlUCholZNMCQQD2SUIr7JG0CA2J7Hhl632JnSOFZ2wUhILxNjFt1h0TA+PuCoDYPQQRjZ00kCfDfqiod0jxvwp+ElJeBtqHqGlxAkAFDGAzCoCvrFnoblC36Rz2BuV2lXg0t4eTIQNg5vp6rmmz6xot8uOOmxMngk08f72lJid63VbcnVFCfPb2LWchAkEAzr20ZHbT4JKZ+tucPcIuwaQ9OzEUEy0hViat24vPIDU10o7SlbKyhaGhA4y3NG5QWgq4GubJggcTSYbrTtFaoQJBAOZNJRcTrm8AcTv0SBoo8REYXI+CjiwXwVTEJrAxx2Sc4t7zxsYBJTSTrRH9F1bQPEgwtDUnFRrhizZsNrBflOc=";
	public static final String KEY_ALGORITHM = "RSA";
	private static final String PUBLIC_KEY = "RSAPublicKey";
	private static final String PRIVATE_KEY = "RSAPrivateKey";

	public static void main(String[] args) throws Exception {
		// Map<String, Object> keyMap = initKey();
		// publicKey = RSAUtils.getPublicKey(keyMap);
		// privateKey = RSAUtils.getPrivateKey(keyMap);
		// System.err.println("公钥: \n\r" + publicKey);
		// System.err.println("私钥： \n\r" + privateKey);

		test2();
	}

	/**
	 * 初始化密钥
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> initKey() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator
				.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(1024);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		// 公钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		// 私钥
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	static void test() throws Exception {
		System.err.println("公钥加密——私钥解密");
		String source = "888888";
		System.out.println("\r加密前文字：\r\n" + source);
		byte[] data = source.getBytes();
		byte[] encodedData = RSAUtils.encryptByPublicKey(data,
				Constants.RSA_PUB_KEY);

		String ss = ISOUtil.hexString(encodedData);
		System.out.println("加密后文字：\r\n" + ss);

		byte[] decodedData = RSAUtils.decryptByPrivateKey(ISOUtil.hex2byte(ss),
				Constants.RSA_PRI_KEY);

		String target = new String(decodedData);
		System.out.println("解密后文字: \r\n" + target);
	}

	static void test2() throws Exception {

		String a="b";
		System.out.println(ISOUtil.hexString(RSAUtils.encryptByPublicKey(ISOUtil.hex2byte(a), Constants.RSA_PUB_KEY)));
		
		String ss = "873e7cbeb025d6400fa08e248efe2900b26a4ba185e22b73dcee982016817e6290cd6a4c05a8499bd7d30985d01542ceef29518607e866fb35e290a0480cc030796c0c92acab342289918eaf355275f2e923303188dc677b34588519959a38cfb70c64c5403dbf3c2feac7958d833ad5e1639f2ef56534ebb85352aabc72573e";
		System.out.println("加密后文字：\r\n" + ss);

		byte[] decodedData = RSAUtils.decryptByPrivateKey(ISOUtil.hex2byte(ss),
				Constants.RSA_PRI_KEY);

		String target = new String(decodedData);
		System.out.println("解密后文字: \r\n" + target);
	}

	static void test3() throws Exception {

		String pk = "5d2c434f36038277efadf497499a618ed85f24dcaede53b1919e0332a89646f027ad8b4fa4f518245c44101aa8d6f905e22fb08b112f54c7ff8ac77197b2172baa043f304d50148fcd2b2c762299437a72c68df376fbfb4a949c92f44a5e629af98ac0685c88ec5eef5e23a6f2131513699cba50fdde06c07a7315cc6df9167d";
		String source = "aaa";
		byte[] data = source.getBytes();
		byte[] encodedData = RSAUtils.encryptByPublicKey(data, pk);

		String ss = ISOUtil.hexString(encodedData);
		System.out.println("加密后文字：\r\n" + ss);
	}
}
