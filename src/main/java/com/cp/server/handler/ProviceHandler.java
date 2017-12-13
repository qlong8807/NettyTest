package com.cp.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.Package;
import com.cp.server.SocketServerLogUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;

/**
 * @author zyl
 * @date 2017年11月28日
 * @desc 省平台服务端socket消息处理器
 */
public class ProviceHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(ProviceHandler.class);
	private UpLoadFileHandler upLoadFileHandler;
	private DownLoadFileHandler downLoadFileHandler;
	private DESedeHandler desedeHandler;

	public ProviceHandler(String data_dir) {
		upLoadFileHandler = new UpLoadFileHandler(data_dir);
		downLoadFileHandler = new DownLoadFileHandler(data_dir);
		desedeHandler = new DESedeHandler();
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Package pkg = (Package) msg;
		String version = pkg.getVersion();
		if (!"01".equals(version)) {
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "无效的消息版本", null);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_03);
			ctx.writeAndFlush(pkg);
			return;
		}
		String msg_type = pkg.getMsg_type();
		// 不是4001和4002就需要判断是否已经进行身份验证
		if (!Constant.PRO_UP_REQUEST_CMD.equals(msg_type) && !Constant.PRO_DOWN_REQUEST_CMD.equals(msg_type)
				&& !Constant.PRO_REQUEST_DESKEY.equals(msg_type)) {
			Attribute<String> attr = ctx.channel().attr(Constant.NETTY_IS_LOGIN);
			String sign = attr.get();
			if (!"islogin".equals(sign)) {
				SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "未登录，不能继续操作", null);
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_02);
				ctx.writeAndFlush(pkg);
				ctx.close();
				return;
			}
		}
		switch (msg_type) {
		case Constant.PRO_UP_REQUEST_CMD:
			upLoadFileHandler.validateClient4001(ctx, pkg);
			break;
		case Constant.PRO_FILE_NUM_NOTIFY_CMD:
			upLoadFileHandler.fileNumNotify4006(ctx, pkg);
			break;
		case Constant.PRO_FILE_INFO_NOTIFY_CMD:
			upLoadFileHandler.fileInfoNotify4003(ctx, pkg);
			break;
		case Constant.PRO_DATA_CMD:
			upLoadFileHandler.fileUpData4004(ctx, pkg);
			break;
		case Constant.PRO_END_CMD:
			upLoadFileHandler.fileEnd4007(ctx, pkg);
			break;
		case Constant.PRO_DOWN_REQUEST_CMD:
			downLoadFileHandler.fileDownRequest4002(ctx, pkg);
			break;
		case Constant.PRO_BREAK_NOTIFY_CMD:
			downLoadFileHandler.fileBreakNotify4005(ctx, pkg);
			break;
		case Constant.PRO_RESPONSE:
			downLoadFileHandler.clientResponse4008(ctx, pkg);
			break;
		case Constant.PRO_REQUEST_DESKEY:
			desedeHandler.requestDESedeKey4009(ctx, pkg);
			break;
		default:
			logger.error("服务端收到一个未知类型的报文：{}", pkg.toString());
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "无效的消息类型码", null);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_02);
			ctx.writeAndFlush(pkg);
			break;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String remoteAddress = ctx.channel().remoteAddress().toString();
		logger.info("客户端<{}>连上了服务端", remoteAddress);
		SocketServerLogUtil.insertLog(Constant.INFO, "0", ctx, "客户端连上了服务端", null);
	}
	

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		logger.info("客户端<{}>链接已断开",ctx.channel().remoteAddress());
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("服务端：客户端<{}>连接已关闭，错误信息：{}", ctx.channel().remoteAddress(), cause.getMessage());
		SocketServerLogUtil.insertLog(Constant.INFO, "0", ctx, "服务端：客户端连接已关闭", null);
		cause.printStackTrace();
		ctx.close();// 出现异常时关闭channel
	}
}
