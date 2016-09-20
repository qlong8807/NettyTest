package com.cyber.netty.byte2.session;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public class MutualSession implements Serializable {
	private static final long serialVersionUID = 1L;
	private long id;
	private ChannelHandlerContext channel;
	private long firstConnectTime;
	private long lastMutualTime;
	private byte[] stickPack;

	public MutualSession() {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ChannelHandlerContext getChannel() {
		return channel;
	}

	public void setChannel(ChannelHandlerContext channel) {
		this.channel = channel;
	}

	public long getFirstConnectTime() {
		return firstConnectTime;
	}

	public void setFirstConnectTime(long firstConnectTime) {
		this.firstConnectTime = firstConnectTime;
	}

	public long getLastMutualTime() {
		return lastMutualTime;
	}

	public void setLastMutualTime(long lastMutualTime) {
		this.lastMutualTime = lastMutualTime;
	}

	public byte[] getStickPack() {
		byte[] temp = stickPack;
		stickPack = null;
		return temp;
	}

	public void setStickPack(byte[] stickPack) {
		this.stickPack = stickPack;
	}

}
