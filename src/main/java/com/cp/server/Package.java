package com.cp.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Package {
	
	private static Logger logger = LoggerFactory.getLogger(Package.class);
	
	/**
	 * 包长度
	 */
	private String length;
	/**
	 * 同步信息 由客户端定义的用于匹配请求消息的数据块
	 */
	private String client_sync_info;
	/**
	 * 报文内容压缩标志
	 */
	private String zip_flag = "0";
	/**
	 * 加密算法标志
	 */
	private String encrypt_flag = "0";
	/**
	 * 版本号标志
	 */
	private String version;
	/**
	 * 消息类型标志
	 */
	private String msg_type;
	/**
	 * 包体 报文内容
	 */
	private String content;
	/**
	 * MAC
	 */
	private String mac;

	public Package() {
	}

	public Package(String msg) throws Exception{
		if (StringUtils.isNotBlank(msg) && msg.length() >= 40) {
			this.length = msg.substring(0, 4);
			this.client_sync_info = msg.substring(4, 16);
			this.zip_flag = msg.substring(16, 17);
			this.encrypt_flag = msg.substring(17, 18);
			this.version = msg.substring(18, 20);
			this.msg_type = msg.substring(20, 24);
			this.content = msg.substring(24, msg.length() - 16);
			this.mac = msg.substring(msg.length() - 16);
		} else {
			logger.error("create package has a error length.msg:{}",msg);
		}
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getClient_sync_info() {
		return client_sync_info;
	}

	public void setClient_sync_info(String client_sync_info) {
		this.client_sync_info = client_sync_info;
	}

	public String getZip_flag() {
		return zip_flag;
	}

	public void setZip_flag(String zip_flag) {
		this.zip_flag = zip_flag;
	}

	public String getEncrypt_flag() {
		return encrypt_flag;
	}

	public void setEncrypt_flag(String encrypt_flag) {
		this.encrypt_flag = encrypt_flag;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMsg_type() {
		return msg_type;
	}

	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	@Override
	public String toString() {
		return "Package [length=" + length + ", client_sync_info=" + client_sync_info + ", zip_flag=" + zip_flag
				+ ", encrypt_flag=" + encrypt_flag + ", version=" + version + ", msg_type=" + msg_type + ", content="
				+ content + ", mac=" + mac + "]";
	}

	public static void main(String[] args) {
	}

}
