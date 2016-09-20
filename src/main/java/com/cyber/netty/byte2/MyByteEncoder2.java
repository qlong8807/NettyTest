/**
 * 
 */
package com.cyber.netty.byte2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyber.netty.bytes.Convert;

/**
 * @author zyl
 * @param <T>
 * @date 2016年9月2日
 * 
 */
public class MyByteEncoder2 extends MessageToByteEncoder<Object>{
	private static Logger logger = LoggerFactory.getLogger(MyByteEncoder2.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		Packet outpacket = (Packet) msg;
		byte[] version = Convert.intTobytes(outpacket.getVersion(), 1);
		byte[] cmdId = Convert.longTobytes(outpacket.getCommand(), 2);
		byte[] uniqueMark = Convert.longTobytes(outpacket.getUniqueMark(), 2);
		byte[] serialNumber = Convert.longTobytes(outpacket.getSerialNumber(), 2);
		byte[] content = outpacket.getContent();
		
		if(content != null){
			if(content.length > Constant.PACKET_MAX_LENGTH){
				List<byte[]> list = PacketProcessor.dataSegmentation(content,Constant.PACKET_MAX_LENGTH);
				int packetCount = list.size();
				//包总数
				byte[] pkCount = Convert.longTobytes(packetCount, 2);
				for(int i = 0 ; i < packetCount ; i ++){
					//包序号，从1开始
					byte[] packetSerial = Convert.longTobytes( i + 1, 2);
					//包长度
					byte[] packetLength = Convert.longTobytes(list.get(i).length, 4);
					this.write(version, cmdId, uniqueMark, serialNumber, packetLength,pkCount,packetSerial, list.get(i), out);
				}
			}else{
				byte[] packetLength = Convert.longTobytes(content.length , 4);
				byte[] pkCount = Convert.longTobytes(0, 2);
				byte[] packetSerial = Convert.longTobytes(0, 2);
				this.write(version, cmdId, uniqueMark, serialNumber, packetLength,pkCount,packetSerial, content, out);
			}
		}
	}
	private void write(byte[] version ,byte[] cmdId,byte[] uniqueMark,byte[] serialNumber, byte[] packetLength,
			 byte[] pkCount,byte[] packetSerial, byte[] pkContent,ByteBuf out) {
		//消息头：版本1byte 指令号2bytes 唯一标识2bytes 流水号2bytes 长度4bytes 包总数2bytes 包序号2bytes
		int contentLength = pkContent != null ? pkContent.length : 0;
		int pkLength = contentLength +  16;
		byte[] data = new byte[pkLength];
		ArraysUtils.arrayappend(data, 0, version);
		ArraysUtils.arrayappend(data, 1, cmdId);//指令号
		ArraysUtils.arrayappend(data, 3, uniqueMark);//唯一标识
		ArraysUtils.arrayappend(data, 5, serialNumber);//流水号
		ArraysUtils.arrayappend(data, 7, packetLength);
		ArraysUtils.arrayappend(data, 11, pkCount);
		ArraysUtils.arrayappend(data, 13, packetSerial);
		ArraysUtils.arrayappend(data, 15, pkContent);
		byte[] validate = new byte[] { PacketProcessor.checkPackage(data, 0, data.length - 1) };
		ArraysUtils.arrayappend(data, data.length - 1 ,validate);
		//进行转义
		byte[] bytes = PacketProcessor.escape(data, Constant.escapeByte, Constant.toEscapeByte);
		//添加包头包尾
		byte[] buffer = new byte[bytes.length+2];
		buffer[0] = Constant.pkBegin;
		System.arraycopy(bytes, 0, buffer, 1, bytes.length);
		buffer[bytes.length+1] = Constant.pkEnd;
		logger.info("encode: "+buffer.length+"--"+Convert.bytesToHexString(buffer));
		out.writeBytes(buffer);
	}
}
