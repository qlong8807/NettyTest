package com.cp.server.handler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.Package;
import com.cp.server.SocketServerLogUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * @author zyl
 * @date 2017年11月28日
 * @desc 客户端上传文件处理流程 1. (客户端)建立连接 2. (客户端)发送文件上传请求报文(4001) 3.
 *       (服务端)验证身份，发送应答报文(4008) 4. (客户端)发送文件数通知报文(4006) 5. (服务端)发送应答报文(4008) 6.
 *       (客户端)发送文件信息通知报文(4003) 7. (服务端)发送断点通知报文(4005) 8. (客户端)发送数据报文(4004) 9.
 *       (服务端)发送应答报文(4008) 10. 重复8、9，直至文件传输完成 11. (客户端)发送文件传输结束报文(4007) 12.
 *       (服务端)发送应答报文(4008) 13. 转第6步，开始下一个文件的传输，如无文件则执行第14步 14. (客户端)关闭Socket连接
 *       15. (客户端)断开拨号连接 (有拨号的情况)
 */
public class UpLoadFileHandler {

	private Logger logger = LoggerFactory.getLogger(UpLoadFileHandler.class);


	private int fileNum = 0;// 上传的文件数
	private int fileSavedNum = 0;// 已存入临时目录的文件数
	private String fileName;
	// private String fileDesc;
	private int fileSize;
	private String institutionCode = "";
	private StringBuilder fileContent = new StringBuilder(1024);
	private String localFilePath = "";

	public UpLoadFileHandler(String data_dir) {
		this.localFilePath = data_dir;
	}

	public void validateClient4001(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		String name = content.substring(0, 40).trim();// 机构简称
		String code = content.substring(40, 51).trim();// 机构代码
		institutionCode = code;
		// 判断是否验证通过
		boolean flag = true;
		if (flag) {
			Attribute<String> attr = ctx.channel().attr(Constant.NETTY_IS_LOGIN);
			attr.set("islogin");
			Attribute<String> attr3 = ctx.channel().attr(Constant.INST_CODE);
			attr3.set(code);
//			Attribute<String> attr2 = ctx.channel().attr(Constant.DES_KEY);
//			logger.info("秘钥是：{}", queryObjById.getDesedeKey());
//			attr2.set(queryObjById.getDesedeKey());
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			// 鉴权不通过
			logger.error("鉴权不通过，客户端<{}>,机构代码<{}>。", ctx.channel().remoteAddress(), institutionCode);
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "上传鉴权不通过", institutionCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_50);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileNumNotify4006(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (StringUtils.isNotBlank(content)) {
			String numstring = content.substring(0, 4).trim();
			int num = Integer.parseInt(numstring);
			fileNum = num;
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			logger.error("客户端<{}>,发送的文件数报文异常。{}", ctx.channel().remoteAddress(), pkg.toString());
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "客户端发送的文件数报文异常", institutionCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_04);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileInfoNotify4003(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (StringUtils.isNotBlank(content) && content.length() == 572) {
			fileName = content.substring(0, 50).trim();
			// fileDesc = content.substring(50, 306).trim();
			fileSize = Integer.parseInt(content.substring(306, 316));
			// 校验上三项
			if (fileName.length() != 33) {
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_07);
				ctx.writeAndFlush(pkg);
				logger.error("客户端<{}>发送的文件名<{}>不合法,断开连接", ctx.channel().remoteAddress(), fileName);
				ctx.close();
			}
			// 文件摘要暂时先不验证-zyl
			if (fileSize < 1) {
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_08);
				ctx.writeAndFlush(pkg);
				logger.error("客户端<{}>发送的文件<{}>大小<{}>不合法,断开连接", ctx.channel().remoteAddress(), fileName, fileSize);
				ctx.close();
			}
			pkg.setMsg_type(Constant.PRO_BREAK_NOTIFY_CMD);
			// 发送断点通知报文。现在没有实现。所以每次发送的断点长度为0.
			pkg.setContent("0000000000" + Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			logger.error("客户端<{}>,发送的文件通知报文长度异常。content长度应为572实际为：{}，报文：{}", ctx.channel().remoteAddress(),
					content.length(), pkg.toString());
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx,
					"客户端发送的文件通知报文长度异常。content长度应为572实际为：" + content.length(), institutionCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_04);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileUpData4004(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (StringUtils.isNotBlank(content) && content.length() > 1) {
			String flag = content.substring(0, 1);
			String fileContentSeg = content.substring(1, content.length());
			if ("1".equals(flag)) {
				// 这是本文件的最后一个报文
				fileContent.append(fileContentSeg);
			} else if ("0".equals(flag)) {
				// 还有后续报文
				fileContent.append(fileContentSeg);
			}
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			logger.error("客户端<{}>,发送的文件内容片段报文长度异常。{}", ctx.channel().remoteAddress(), pkg.toString());
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "发送的文件内容片段报文异常", institutionCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_08);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileEnd4007(ChannelHandlerContext ctx, Package pkg) {
		// 一个文件传输结束，生成文件
		if (fileSize == fileContent.length()) {
			logger.info("客户端<{}>,文件<{}>接收完成，大小匹配。", ctx.channel().remoteAddress(), fileName);
			SocketServerLogUtil.insertLog(Constant.INFO, pkg.getMsg_type(), ctx, "文件" + fileName + "接收完成，大小匹配。",
					institutionCode);
			try {
				logger.info("写入第<{}>个文件<{}>。", fileSavedNum + 1,
						localFilePath + "/" + institutionCode + "/tmp/" + fileName);
				FileUtils.write(new File(localFilePath + "/" + institutionCode + "/tmp/" + fileName), fileContent,
						Constant.GB2312_STR);
				fileSavedNum++;
				fileContent.delete(0, fileContent.length());
			} catch (IOException e) {
				SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx,
						"文件" + fileName + "保存时出现异常，服务端主动断开连接。", institutionCode);
				// 保存文件出现异常，断开连接，让客户端再次传送文件
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_50);
				ctx.writeAndFlush(pkg);
				ctx.close();
			}
			if (fileNum == fileSavedNum) {
				logger.info("客户端<{}>,所有文件接收完成，文件数：{}，开始从tmp目录转入upload目录。", ctx.channel().remoteAddress(), fileNum);
				SocketServerLogUtil.insertLog(Constant.INFO, pkg.getMsg_type(), ctx,
						"所有文件接收完成，文件数：" + fileNum + "，开始从临时目录转入upload目录。", institutionCode);
				try {
					Collection<File> listFiles = FileUtils
							.listFiles(new File(localFilePath + "/" + institutionCode + "/tmp/"), null, false);
					for (File file : listFiles) {
						if (33 == file.getName().length()) {
							FileUtils.moveFile(file,
									new File(localFilePath + "/" + institutionCode + "/upload/" + file.getName()));
						} else {
							logger.error("文件<{}>名称长度不等于33，不进行转存。", file.getName());
						}
					}
					logger.info("客户端<{}>的文件全部接收完毕，关闭链路", ctx.channel().remoteAddress() + "-" + institutionCode);
					SocketServerLogUtil.insertLog(Constant.INFO, pkg.getMsg_type(), ctx, "文件全部接收完毕，关闭链路",
							institutionCode);
				} catch (IOException e) {
					logger.error("目录从<{}>到<{}>转移出错：{}", localFilePath + "/" + institutionCode + "/tmp/",
							localFilePath + "/" + institutionCode + "/upload/", e.getMessage());
					SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx,
							"从tmp目录到upload目录转移出错：" + e.getMessage(), institutionCode);
				}
			}
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			logger.error("客户端<{}>,文件<{}>收到4007结束命令，但是文件大小不匹配，文件大小：{},实际接收的大小：{}。", ctx.channel().remoteAddress(),
					fileName, fileSize, fileContent.length());
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx,
					"文件" + fileName + "收到4007结束命令，但是文件大小不匹配，文件大小：" + fileSize + ",实际接收的大小：" + fileContent.length(),
					institutionCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_08);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}
}
