package com.cp.client;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * DESedeUtil {3DES加密解密的工具类 }
 */
public class DESedeUtil {

	public static void main(String[] args) throws Exception {
		String PASSWORD_CRYPT_KEY = generate3DesKey(24);
		String msg = "3DES加密解密案例,KEY:";
		System.out.println("【加密前】：" + msg);
		System.out.println("【加密前长度】：" + msg.length());

		// 加密
		byte[] secretArr = DESedeUtil.encryptMode(msg.getBytes(Constant.GB2312_STR), PASSWORD_CRYPT_KEY);
		System.out.println("【加密后】：" + new String(secretArr));
		System.out.println("【加密后长度】：" + secretArr.length);

		// 解密
		byte[] myMsgArr = DESedeUtil.decryptMode(secretArr, PASSWORD_CRYPT_KEY);
		System.out.println("【解密后】：" + new String(myMsgArr,Constant.GB2312_STR));
	}

	// 定义加密算法，有DES、DESede(即3DES)、Blowfish
	private static final String Algorithm = "DESede";

	/**
	 * 加密方法
	 * 
	 * @param src
	 *            源数据的字节数组
	 * @return
	 */
	public static byte[] encryptMode(byte[] src, String PASSWORD_CRYPT_KEY) {
		try {
			SecretKey deskey = new SecretKeySpec(build3DesKey(PASSWORD_CRYPT_KEY), Algorithm); // 生成密钥
			Cipher c1 = Cipher.getInstance(Algorithm); // 实例化负责加密/解密的Cipher工具类
			c1.init(Cipher.ENCRYPT_MODE, deskey); // 初始化为加密模式
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密函数
	 * 
	 * @param src
	 *            密文的字节数组
	 * @return
	 */
	public static byte[] decryptMode(byte[] src, String PASSWORD_CRYPT_KEY) {
		try {
			SecretKey deskey = new SecretKeySpec(build3DesKey(PASSWORD_CRYPT_KEY), Algorithm);
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.DECRYPT_MODE, deskey); // 初始化为解密模式
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据字符串生成密钥字节数组
	 * 
	 * @param keyStr 密钥字符串
	 * 
	 * @return 长度为24的字节数组
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
		byte[] key = new byte[24]; // 声明一个24位的字节数组，默认里面都是0
		byte[] temp = keyStr.getBytes(Constant.GB2312_STR); // 将字符串转成字节数组
		/*
		 * 执行数组拷贝 System.arraycopy(源数组，从源数组哪里开始拷贝，目标数组，拷贝多少位)
		 */
		if (key.length > temp.length) {
			// 如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
			System.arraycopy(temp, 0, key, 0, temp.length);
		} else {
			// 如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
			System.arraycopy(temp, 0, key, 0, key.length);
		}
		return key;
	}
	/**
	 * 随机生成长度为length的a-zA-Z0-9字符串
	 * @param length
	 * @return
	 */
	public static String generate3DesKey(int length) {
		String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  
	    Random random = new Random();  
	    StringBuffer sb = new StringBuffer();  
	    for (int i = 0; i < length; i++) {  
	        int number = random.nextInt(base.length());  
	        sb.append(base.charAt(number));  
	    }  
	    return sb.toString();  
	}
}