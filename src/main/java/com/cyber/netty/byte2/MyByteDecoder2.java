/**
 * 
 */
package com.cyber.netty.byte2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyber.netty.byte2.session.MutualSession;
import com.cyber.netty.byte2.session.MutualSessionManage;
import com.cyber.netty.bytes.Convert;

/**
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class MyByteDecoder2 extends ByteToMessageDecoder {
	private static final Logger logger = LoggerFactory.getLogger(MyByteDecoder2.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		int limit = in.readableBytes();
		byte[] currentBytes = new byte[limit];
		in.readBytes(currentBytes);
		int hashCode = ctx.channel().remoteAddress().hashCode();
		MutualSession mutualSession = MutualSessionManage.getInstance().getMutualSessionForSessionId(hashCode);
		if(mutualSession != null){
			byte[] lastBytes = mutualSession.getStickPack();
			byte[] bytes = ArraysUtils.arraycopy(lastBytes, currentBytes);
			List<byte[]> packets = PacketProcessor.subpackage(bytes , Constant.pkBegin , Constant.pkEnd , 0);
			for (byte[] packet : packets) {
				if(packet != null){
					if(packet[0] == Constant.pkBegin && packet[packet.length -1] == Constant.pkEnd){
						this.buildPacket(packet,out);
					}else if(packet[0] == Constant.pkBegin){
						mutualSession.setStickPack(packet);
					}
				}
			}
		}else{
			logger.error("无此session，断开连接");
			ctx.close();
		}
	}
	private void buildPacket(byte[] packet,List<Object> out) {
//		logger.info("decode:"+Convert.bytesToHexString(packet));
		//去包头包尾
		byte[] tempBytes = new byte[packet.length - 2];
		System.arraycopy(packet, 1, tempBytes, 0, tempBytes.length);
		//转义还原
		byte[] bytes = PacketProcessor.unEscape(tempBytes, Constant.toEscapeByte, Constant.escapeByte);
		//取出源数据检验码
		int checkCode = bytes[bytes.length - 1];
		int tempCode = PacketProcessor.checkPackage(bytes, 0, bytes.length - 2);
		if (checkCode != tempCode) {
			logger.error("校验码错误 , result[" + tempCode + "],source[" + checkCode
					+ "].source data :" + Convert.bytesToHexString(packet));
			return;
		}
		
		int version = Convert.byte2Int(ArraysUtils.subarrays(bytes, 0, 1), 1);
		int cmdId = Convert.byte2Int(ArraysUtils.subarrays(bytes, 1, 2), 2);
		int uniqueMark = Convert.byte2Int(ArraysUtils.subarrays(bytes, 3, 2),2);
		int serialNumber = Convert.byte2Int(ArraysUtils.subarrays(bytes, 5, 2), 2);
		int packetLength = Convert.byte2Int(ArraysUtils.subarrays(bytes, 7, 4), 4);
		int pkCount = Convert.byte2Int(ArraysUtils.subarrays(bytes, 11, 2), 2);
		int pkSerial = Convert.byte2Int(ArraysUtils.subarrays(bytes, 13, 2), 2);
		byte[] content = ArraysUtils.subarrays(bytes, 15, packetLength);
		
		Packet outpacket = new Packet();
		outpacket.setVersion(version);
		outpacket.setCommand(cmdId);
		outpacket.setUniqueMark(uniqueMark);
		outpacket.setSerialNumber(serialNumber);
		outpacket.setPacketTotal(pkCount);
		outpacket.setPacketSerial(pkSerial);
		outpacket.setFrom(uniqueMark);
		outpacket.setContent(content);
		//判断是否分包
		if(pkCount > 0){
//			System.err.println("子包："+packetLength+"==="+content.length);
//			System.err.println("子包："+Convert.bytesToHexString(outpacket.getContent()));
			boolean isComplete = PacketProcessor.mergeBlock(outpacket);
			if(isComplete){
				outpacket = PacketProcessor.getCompletePacket(uniqueMark, serialNumber);
				out.add(outpacket);
			}
		}else{
			out.add(outpacket);
		}
	}
}
