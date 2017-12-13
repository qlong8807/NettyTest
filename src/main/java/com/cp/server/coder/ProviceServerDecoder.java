/**
 * 
 */
package com.cp.server.coder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.DESedeUtil;
import com.cp.client.Package;
import com.cp.server.SocketServerLogUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;

/**
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class ProviceServerDecoder extends ByteToMessageDecoder {
	
	private static Logger logger = LoggerFactory.getLogger(ProviceServerDecoder.class);
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
		ByteBuf readBytes = in.readBytes(HEAD_LENGTH);
		byte[] t1 = new byte[HEAD_LENGTH];
		readBytes.readBytes(t1);
		String head_len = new String(t1);
		int dataLength = 0;
		try {
			dataLength = Integer.parseInt(head_len);
		} catch (Exception e) {
			logger.error("服务端<--客户端的包长度<{}>不是数字，直接返回系统错误。",head_len);
			Package pkg = new Package();
			pkg.setClient_sync_info("000000000000");
			pkg.setZip_flag("0");
			pkg.setEncrypt_flag("0");
			pkg.setVersion("01");
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_50);
			pkg.setMac("0000000000000000");
			ctx.writeAndFlush(pkg);
			ctx.close();
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "包长度不是数字，返回50响应码", null);
			return;
		}
		// 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
		if (dataLength < 1) {
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
		//除过长度的消息，至少有36个字节，如果少于36个字节则包有问题。
		if(body.length < 36) {
			Package pkg = new Package();
			pkg.setClient_sync_info("000000000000");
			pkg.setZip_flag("0");
			pkg.setEncrypt_flag("0");
			pkg.setVersion("01");
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_04);
			pkg.setMac("0000000000000000");
			ctx.writeAndFlush(pkg);
			ctx.close();
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "报文长度小于40个字符，返回04", null);
			return;
		}
		// 从body中取出前20个字节，构建包头
		byte[] p_head = new byte[20];
		byte[] p_body = new byte[dataLength-36];
		byte[] p_mac = new byte[16];
		System.arraycopy(body, 0, p_head, 0, 20);
		System.arraycopy(body, 20, p_body, 0, dataLength-36);
		System.arraycopy(body, dataLength-16, p_mac, 0, 16);
		String p_head_string = new String(p_head, Constant.GB2312_STR);
		String head = head_len+p_head_string;//head为整个包头
		String mac = new String(p_mac, Constant.GB2312_STR);
		
		Package pkg = new Package();
		pkg.setLength(head.substring(0, 4));
		pkg.setClient_sync_info(head.substring(4, 16));
		pkg.setZip_flag(head.substring(16, 17));
		pkg.setEncrypt_flag(head.substring(17, 18));
		pkg.setVersion(head.substring(18, 20));
		pkg.setMsg_type(head.substring(20, 24));
		pkg.setMac(mac);
		
		if("1".equals(pkg.getZip_flag())) {
			Attribute<String> attr = ctx.channel().attr(Constant.INST_CODE);
			String inst_code = attr.get();
			logger.error("该报文采用LZ77算法压缩，当前系统未实现压缩功能。机构代码：{}",inst_code);
		}
		if("1".equals(pkg.getEncrypt_flag())) {
			//如果是4001、4002、4009则放过
			if(Constant.PRO_DOWN_REQUEST_CMD.equals(pkg.getMsg_type()) || Constant.PRO_UP_REQUEST_CMD.equals(pkg.getMsg_type()) || Constant.PRO_REQUEST_DESKEY.equals(pkg.getMsg_type())) {
				//不做任何操作
				String content = new String(p_body, Constant.GB2312_STR);
				pkg.setContent(content);
			}else {
				//查询秘钥，如果没有秘钥则返回错误
				Attribute<String> attr2 = ctx.channel().attr(Constant.DES_KEY);
				String des_key = attr2.get();
				if(StringUtils.isNotBlank(des_key)) {
					byte[] decryptBody = DESedeUtil.decryptMode(p_body, des_key);
					String content = new String(decryptBody, Constant.GB2312_STR);
					pkg.setContent(content);
				}else {
					Attribute<String> attr = ctx.channel().attr(Constant.INST_CODE);
					String inst_code = attr.get();
					logger.error("该报文采用3DES加密，当前系统未查找到机构秘钥，关闭连接。机构代码：{}",inst_code);
					pkg.setMsg_type(Constant.PRO_RESPONSE);
					pkg.setContent(Constant.ANSWER_CODE_50);
					ctx.writeAndFlush(pkg);
					ctx.close();
				}
			}
		}else {
			//报文不加密
			String content = new String(p_body, Constant.GB2312_STR);
			pkg.setContent(content);
		}

		logger.info("服务端<--客户端:{}",pkg.getMsg_type());
//		logger.info("服务端<--客户端<{}>的报文是：{}",ctx.channel().remoteAddress().toString(),result);
		
		out.add(pkg);
	}
	

}
