/**
 * 
 */
package com.cyber.encry.test;

/**
 * @author zyl
 * @date 2016年9月22日
 * 加密算法会使用随机生成的salt，因此每次生成的密码都不相同。
 * 验证密码的时候需要从加密密码中截取salt。
 */
public class Test {
	public static void main(String[] args) {
		String pwdString = "userpassword13240-3=13240+_3*4;\\//";
		String entryptPassword = Encodes.entryptPassword(pwdString);
		System.out.println("加密后的密码为："+entryptPassword);
		System.out.println("对密码进行验证："+Encodes.validatePassword(pwdString, entryptPassword));
		
		String encodeBase64 = Encodes.encodeBase64(pwdString);
		System.out.println("base64加密："+encodeBase64);
		String decodeBase64String = Encodes.decodeBase64String(encodeBase64);
		System.out.println("base64解密："+decodeBase64String);
	}
}
