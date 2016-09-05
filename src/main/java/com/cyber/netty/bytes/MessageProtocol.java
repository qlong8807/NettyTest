package com.cyber.netty.bytes;

/**
 * 
* @ClassName: Message 
* @Description:包类型 byte 型 包长度 int 型  消息体 byte[]
* @author cssuger@163.com 
* @date 2016年9月1日 下午8:15:55 
*
 */

public class MessageProtocol implements java.io.Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5532354177040459398L;
    //1 心跳包 2业务类型，3 非法的消息提示，4 登录验证
	private int messageType;
	
	private String content;
	
	
	public MessageProtocol(){
		
	}
	
	public MessageProtocol(int messageType,String content){
		this.messageType = messageType;
		this.content = content;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Message [messageType=" + messageType + ", content=" + content
				+ "]";
	}

	

	
	
	
}
