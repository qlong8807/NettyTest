/**
 * 
 */
package com.cyber.netty.string.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import com.cyber.encry.Base64Util;
import com.cyber.encry.JsonUtil;

/**
 * @author zyl
 * @param <T>
 * @date 2016年9月2日
 * 
 */
public class MyStringEncoder extends MessageToMessageEncoder<Object>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg,
			List<Object> out) throws Exception {
		String objectToJson = JsonUtil.objectToJson(msg);
		String encode = Base64Util.encode(objectToJson)+"\r\n";
		out.add(encode);
	}

}
