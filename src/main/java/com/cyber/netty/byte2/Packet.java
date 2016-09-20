/**
 * 
 */
package com.cyber.netty.byte2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * @author zyl
 * @date 2016年9月18日
 * 
 */
public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	private int version;// 协议版本，最大值为256
	private int command;//命令编号，确定消息体是哪个对象，最大值为65535
	private int uniqueMark;//唯一标识，最大值为65535
	private int serialNumber = 0;// 流水号，最大值为65535
	private int from;// 来个哪个唯一标识，最大值为65535
	private int to;// 发向哪个唯一标识，最大值为65535
	private long createTime = System.currentTimeMillis() / 1000;
	private int packetTotal = 0;// 总包数，最大值为65535
	private int packetSerial = 0;// 包序号，最大值为65535
	private byte[] content = new byte[0];// 消息体
	private Map<Object, Object> parameters;
	public int getCommand() {
		return command;
	}
	public void setCommand(int command) {
		this.command = command;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public int getPacketTotal() {
		return packetTotal;
	}
	public void setPacketTotal(int packetTotal) {
		this.packetTotal = packetTotal;
	}
	/**
	 * 包序号
	 * @return
	 */
	public int getPacketSerial() {
		return packetSerial;
	}
	public void setPacketSerial(int packetSerial) {
		this.packetSerial = packetSerial;
	}
	public byte[] getContent() {
		return content;
	}
	public int getUniqueMark() {
		return uniqueMark;
	}
	public void setUniqueMark(int uniqueMark) {
		this.uniqueMark = uniqueMark;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public Map<Object, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<Object, Object> parameters) {
		this.parameters = parameters;
	}
	@Override
	public String toString() {
		return "Packet [version=" + version + ", command=" + command
				+ ", uniqueMark=" + uniqueMark + ", serialNumber="
				+ serialNumber + ", from=" + from + ", to=" + to
				+ ", createTime=" + createTime + ", packetTotal=" + packetTotal
				+ ", packetSerial=" + packetSerial + ", content="
				+ Arrays.toString(content) + ", parameters=" + parameters + "]";
	}

}
