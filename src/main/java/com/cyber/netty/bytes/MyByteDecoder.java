/**
 * 
 */
package com.cyber.netty.bytes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import com.cyber.encry.JsonUtil;

/**
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class MyByteDecoder extends ByteToMessageDecoder {
	/**
	 * 这个HEAD_LENGTH是我们用于表示头长度的字节数。 由于上面我们传的是一个int类型的值，所以这里HEAD_LENGTH的值为4.
	 */
	private static final int HEAD_LENGTH = 4;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		if (in.readableBytes() < HEAD_LENGTH) {
			return;
		}
		in.markReaderIndex(); // 我们标记一下当前的readIndex的位置
		// 读取传送过来的消息的长度。ByteBuf的readInt()方法会让他的readIndex增加4
		int dataLength = in.readInt();
		// 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
		if (dataLength < 0) {
			ctx.close();
		}

		// 读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex.
		// 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		// 读到的长度满足我们的要求，把传送过来的数据取出来
		byte[] body = new byte[dataLength];
		in.readBytes(body);
		// 将byte数据转化为我们需要的对象
		Object o = JsonUtil.jsonToObject(new String(body),MessageProtocol.class);
		out.add(o);
	}

}
