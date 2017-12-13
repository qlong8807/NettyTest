package com.cp.client.handler;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.Package;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MyClientHandler extends ChannelInboundHandlerAdapter {

	public int processType = 0;
	private static Logger logger = LoggerFactory.getLogger(MyClientHandler.class);
	private ClientUploadHandler clientUploadHandler = new ClientUploadHandler();
	private ClientDownloadHandler clientDownloadHandler = new ClientDownloadHandler();

	/**
	 * 此方法会在连接到服务器后被调用
	 * 
	 * @throws Exception
	 */
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("已连接服务端");
		logger.info("请输入：1（不加密上传），2（加密上传），3（不加密下载），4（加密下载）>>>");
		Scanner scan = new Scanner(System.in);
		String read = scan.nextLine();
		logger.info("输入数据：" + read);
		scan.close();
		processType = Integer.parseInt(read);
		switch (read) {
		case "1":
			clientUploadHandler.requestUpload4001(ctx, null);
			break;
		case "2":
			clientUploadHandler.des3request4009(ctx, null);
			break;
		case "3":
			clientDownloadHandler.requestDownload4002(ctx, null);
			break;
		case "4":
			clientDownloadHandler.des3request4009(ctx, null);
			break;
		default:
			System.out.println("不能识别的输入，关闭连接");
			ctx.close();
			break;
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		logger.info("客户端1：服务端已关闭");
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		com.cp.client.Package pkg = (Package) msg;
		String version = pkg.getVersion();
		if (!"01".equals(version)) {
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_03);
			ctx.writeAndFlush(pkg);
			return;
		}

		String msg_type = pkg.getMsg_type();
		// 先判断流程，然后判断消息类型
		switch (processType) {
		case 1:
			switch (msg_type) {
			case Constant.PRO_RESPONSE:
				clientUploadHandler.uploadResponse4008(ctx, pkg);
				break;
			case Constant.PRO_BREAK_NOTIFY_CMD:
				clientUploadHandler.fileBreakNotify4005(ctx, pkg);
				break;
			default:
				logger.error("服务端收到一个未知类型的报文：{}", pkg.toString());
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_02);
				ctx.writeAndFlush(pkg);
				break;
			}
			break;
		case 2:
			switch (msg_type) {
			case Constant.PRO_RESPONSE:
				clientUploadHandler.uploadResponse4008(ctx, pkg);
				break;
			case Constant.PRO_BREAK_NOTIFY_CMD:
				clientUploadHandler.fileBreakNotify4005(ctx, pkg);
				break;
			case Constant.PRO_RESPONSE_DESKEY:
				clientUploadHandler.des3response4010(ctx, pkg);
				break;
			default:
				logger.error("服务端收到一个未知类型的报文：{}", pkg.toString());
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_02);
				ctx.writeAndFlush(pkg);
				break;
			}
			break;
		case 3:
			switch (msg_type) {
			case Constant.PRO_RESPONSE:
				clientDownloadHandler.response4008(ctx, pkg);
				break;
			case Constant.PRO_FILE_INFO_NOTIFY_CMD:
				clientDownloadHandler.fileInfo4003(ctx, pkg);
				break;
			case Constant.PRO_DATA_CMD:
				clientDownloadHandler.fileData4004(ctx, pkg);
				break;
			case Constant.PRO_FILE_NUM_NOTIFY_CMD:
				clientDownloadHandler.fileNum4006(ctx, pkg);
				break;
			case Constant.PRO_END_CMD:
				clientDownloadHandler.fileEnd4007(ctx, pkg);
				break;
			default:
				logger.error("服务端收到一个未知类型的报文：{}", pkg.toString());
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_02);
				ctx.writeAndFlush(pkg);
				break;
			}
			break;
		case 4:
			switch (msg_type) {
			case Constant.PRO_RESPONSE:
				clientDownloadHandler.response4008(ctx, pkg);
				break;
			case Constant.PRO_FILE_INFO_NOTIFY_CMD:
				clientDownloadHandler.fileInfo4003(ctx, pkg);
				break;
			case Constant.PRO_DATA_CMD:
				clientDownloadHandler.fileData4004(ctx, pkg);
				break;
			case Constant.PRO_FILE_NUM_NOTIFY_CMD:
				clientDownloadHandler.fileNum4006(ctx, pkg);
				break;
			case Constant.PRO_END_CMD:
				clientDownloadHandler.fileEnd4007(ctx, pkg);
				break;
			case Constant.PRO_RESPONSE_DESKEY:
				clientDownloadHandler.des3response4010(ctx, pkg);
				break;
			default:
				logger.error("服务端收到一个未知类型的报文：{}", pkg.toString());
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_02);
				ctx.writeAndFlush(pkg);
				break;
			}
			break;
		default:
			logger.error("未知类型");
			break;
		}

	}

	/**
	 * 捕捉到异常
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		logger.info("客户端：发生异常:" + cause.getMessage());
		ctx.close();
	}
}