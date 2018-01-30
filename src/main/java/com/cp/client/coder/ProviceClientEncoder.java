/**
 * 
 */
package com.cp.client.coder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.BytesUtil;
import com.cp.client.Constant;
import com.cp.client.DESedeUtil;
import com.cp.client.Package;
import com.cp.client.StringUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;

/**
 * @author zyl
 * @param <T>
 * @date 2016年9月2日
 * 
 */
public class ProviceClientEncoder extends MessageToByteEncoder<Package> {
	
	private static Logger logger = LoggerFactory.getLogger(ProviceClientEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Package pkg, ByteBuf out) throws Exception {
		logger.info("客户端-->服务端:{}",pkg.getMsg_type());
		System.err.println(pkg.getMsg_type()+"--->"+pkg.getContent());
		//先对content进行加密
		byte[] pre = (pkg.getClient_sync_info() + pkg.getZip_flag() + pkg.getEncrypt_flag() + pkg.getVersion()
		+ pkg.getMsg_type()).getBytes(Constant.GB2312_STR);
		byte[] content = null;
		if("1".equals(pkg.getEncrypt_flag()) && !Constant.PRO_UP_REQUEST_CMD.equals(pkg.getMsg_type())
				 && !Constant.PRO_DOWN_REQUEST_CMD.equals(pkg.getMsg_type()) && !Constant.PRO_REQUEST_DESKEY.equals(pkg.getMsg_type())
				 && !Constant.PRO_RESPONSE_DESKEY.equals(pkg.getMsg_type())) {
			//3des加密
			Attribute<String> attr2 = ctx.channel().attr(Constant.DES_KEY);
			String key = attr2.get();
			logger.info("加密秘钥：{}",key);
			content = DESedeUtil.encryptMode(pkg.getContent().getBytes(Constant.GB2312_STR),key);
		}else {
			content = pkg.getContent().getBytes(Constant.GB2312_STR);
		}
		byte[] sub = pkg.getMac().getBytes(Constant.GB2312_STR);
		byte[] msg1 = byteMerger(pre, content);
		byte[] msg = byteMerger(msg1, sub);
		
		int length = msg.length;
		String len = StringUtil.addZeroForStr_L(length + "", 4);
		byte[] len_bytes = len.getBytes(Constant.GB2312_STR);
		byte[] result = byteMerger(len_bytes, msg);
		if(Constant.PRO_DATA_CMD.equals(pkg.getMsg_type())) {
			logger.info("客户端-->服务端<{}>的报文是：{}",ctx.channel().remoteAddress().toString(),pkg.getClient_sync_info() + pkg.getZip_flag() + pkg.getEncrypt_flag() + pkg.getVersion()
			+ pkg.getMsg_type() +"content" + pkg.getMac());
		}else {
			logger.info("客户端-->服务端<{}>的报文是：{}",ctx.channel().remoteAddress().toString(),BytesUtil.bytesToHexString(result));
		}
		out.writeBytes(result); // 消息体中包含我们要发送的数据
	}
	public static byte[] byteMerger(byte[] bt1, byte[] bt2){  
        byte[] bt = new byte[bt1.length+bt2.length];  
        System.arraycopy(bt1, 0, bt, 0, bt1.length);  
        System.arraycopy(bt2, 0, bt, bt1.length, bt2.length);  
        return bt;  
    } 
}
