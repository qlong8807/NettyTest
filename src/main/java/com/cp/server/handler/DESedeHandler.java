package com.cp.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.DESedeUtil;
import com.cp.client.Package;
import com.cp.client.StringUtil;
import com.cp.server.SocketServerLogUtil;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zyl
 * @date 2017年11月28日
 * @desc
 */
public class DESedeHandler {

	private Logger logger = LoggerFactory.getLogger(DESedeHandler.class);

//	private BuinstitutionService buinstitutionService = (BuinstitutionService) SpringContextUtil
//			.getBean("buinstitutionService");
//	private InstitutionDESedeService institutionDESedeService = (InstitutionDESedeService) SpringContextUtil
//			.getBean("institutionDESedeService");

	public void requestDESedeKey4009(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		String name = content.substring(0, 40).trim();// 机构简称
		String code = content.substring(40, 51).trim();// 机构代码
		// 判断是否验证通过
		boolean flag = true;
		if (flag) {
			// 机构号存在，生成一个key
			//TODO 如何生成秘钥key，生成后保存到数据库并发给客户端
			String key = DESedeUtil.generate3DesKey(24);
			//保存key
			pkg.setMsg_type(Constant.PRO_RESPONSE_DESKEY);
			pkg.setContent(key + StringUtil.addZeroForStr_L("", 64));
			ctx.writeAndFlush(pkg);
		} else {
			// 鉴权不通过
			logger.error("鉴权不通过，客户端<{}>,机构代码<{}>。", ctx.channel().remoteAddress(), code);
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "上传鉴权不通过", code);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_50);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}
}
