/**
 * 
 */
package com.cyber.netty.bytes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.cyber.encry.JsonUtil;

/**
 * @author zyl
 * @param <T>
 * @date 2016年9月2日
 * 
 */
public class MyByteEncoder extends MessageToByteEncoder<Object>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		//这里现在用的java序列化，可以选择其他序列化方法
		byte[] body = JsonUtil.objectToJson(msg).getBytes();
        int dataLength = body.length;  //读取消息的长度
        out.writeInt(dataLength);  //先将消息长度写入，也就是消息头
        out.writeBytes(body);  //消息体中包含我们要发送的数据
	}

}
