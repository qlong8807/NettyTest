package com.cyber.netty.byte2;

public class MessageProtocol2 implements java.io.Serializable {
	private static final long serialVersionUID = 5532354177040459398L;
	// 1 心跳包 2业务类型，3 非法的消息提示，4 登录验证
	private int messageType;
	private String content;
	// 验证通过后，给客户端返回一个标识，用于后面的操作验证
	private String validateSerialize;

	public int getMessageType() {
		return messageType;
	}

	public String getValidateSerialize() {
		return validateSerialize;
	}

	public void setValidateSerialize(String validateSerialize) {
		this.validateSerialize = validateSerialize;
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
		return "MessageProtocol [messageType=" + messageType + ", content="
				+ content + ", validateSerialize=" + validateSerialize + "]";
	}

}
