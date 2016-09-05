/**
 * 
 */
package com.cyber.netty.string.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import com.cyber.encry.Base64Util;
import com.cyber.encry.JsonUtil;
import com.cyber.netty.bytes.MessageProtocol;

/**
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class MyStringDecoder extends MessageToMessageDecoder<Object> {

	@Override
	protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out)
			throws Exception {
		String decodeString = (String) msg;
		String json = Base64Util.decode(decodeString);
		MessageProtocol mp = (MessageProtocol) JsonUtil.jsonToObject(json, MessageProtocol.class);
		out.add(mp);
	}

}
