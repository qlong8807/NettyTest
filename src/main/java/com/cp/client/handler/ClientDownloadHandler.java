package com.cp.client.handler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.Package;
import com.cp.client.StringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * @author zyl
 * @date 2017年12月11日
 * @desc 客户端上传文件处理流程 1. (客户端)建立连接 2. (客户端)发送文件下载请求(4002) 3. (服务端)验证身份
 *       不通过则发送失败的文件数通知4006报文,响应码非00，并关闭连接，结束 下载; 通过则发送成功的文件数通知报文(4006)。
 *       如果4006报文中需要下发的文件为0,则中心关闭连接，结束下载。 4. (客户端)发送应答报文(4008) 5.
 *       (服务端)发送文件信息通知报文(4003) 6. (客户端)发送断点通知报文(4005) 7. (服务端)发送应答报文(4008) 8.
 *       (服务端)发送数据报文(4004) 9. (客户端)发送应答报文(4008) 10. 重复8，9两步直到文件传输结束 11.
 *       (服务端)发送文件传输结束报文(4007) 12. (客户端)发送应答报文(4008) 13. (服务端)将成功传送的文件移到备份目录
 *       重复5-13，直到所有的文件都传输完成 14. (客户端)关闭Socket连接 15. (客户端)断开拨号连接(有拨号的情况)
 */
public class ClientDownloadHandler {
	private static Logger logger = LoggerFactory.getLogger(ClientUploadHandler.class);
	private String inst_code = "";
	private String localFilePath = "";

	private int fileNum = 0;
	private int fileSavedNum = 0;
	private String fileName = "";
	private int fileSize = 0;
	private StringBuilder fileContent = new StringBuilder(2000);

	public ClientDownloadHandler(String code,String path) {
		this.inst_code = code;
		this.localFilePath = path;
	}
	public void requestDownload4002(ChannelHandlerContext ctx, Package pkg) {
		if (null == pkg) {
			pkg = new Package();
			pkg.setClient_sync_info("thisisClient");
			pkg.setZip_flag("0");
			pkg.setEncrypt_flag("0");
			pkg.setVersion("01");
		} else {
			pkg.setEncrypt_flag("1");
		}
		pkg.setMsg_type(Constant.PRO_DOWN_REQUEST_CMD);
		String name = StringUtil.addSpaceForStr_R("长安个球", 40);
		String code = StringUtil.addSpaceForStr_R(inst_code, 11);
		String date = StringUtil.addZeroForStr_L("", 8);
		String keep = StringUtil.addForStr_R("", "F", 256);
		pkg.setContent(name + code + date + keep);
		pkg.setMac("1111222233334444");
		ctx.writeAndFlush(pkg);
	}

	public void fileInfo4003(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		int len = 0;
		try {
			len = content.getBytes(Constant.GB2312_STR).length;
		} catch (UnsupportedEncodingException e) {
			logger.error("解析4003报文出现编码异常:<{}>",e.getMessage());
		}
		if (StringUtils.isNotBlank(content) && len == 572) {
			fileName = content.substring(0, 50).trim();
			// fileDesc = content.substring(50, 306).trim();
			String sizeStr = content.substring(content.length()-266,content.length()-256);
			fileSize = Integer.parseInt(sizeStr);
			// 校验上三项
			if (fileName.length() != 33) {
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_07);
				ctx.writeAndFlush(pkg);
				logger.error("服务端发送的文件名<{}>不合法,断开连接", fileName);
				ctx.close();
			}
			// 文件摘要暂时先不验证-zyl
			if (fileSize < 1) {
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_08);
				ctx.writeAndFlush(pkg);
				logger.error("服务端发送的文件<{}>大小<{}>不合法,断开连接", fileName, fileSize);
				ctx.close();
			}
			// 发送断点通知报文。
			fileBreakNotify4005(ctx, pkg);
		} else {
			logger.error("服务端发送的文件通知报文长度异常。content长度应为572实际为：{}，报文：{}", content.length(), pkg.toString());
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_04);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileData4004(ChannelHandlerContext ctx, Package pkg) {
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
			logger.error("服务端发送的文件内容片段报文长度异常。{}", pkg.toString());
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_08);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileBreakNotify4005(ChannelHandlerContext ctx, Package pkg) {
		// 发送断点通知报文。现在没有实现。所以每次发送的断点长度为0.
		pkg.setMsg_type(Constant.PRO_BREAK_NOTIFY_CMD);
		pkg.setContent("0000000000" + Constant.ANSWER_CODE_00);
		ctx.writeAndFlush(pkg);
	}

	public void fileNum4006(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (StringUtils.isNotBlank(content)) {
			String numstring = content.substring(0, 4).trim();
			int num = Integer.parseInt(numstring);
			String answer_code = content.substring(4).trim();
			if (!Constant.ANSWER_CODE_00.equals(answer_code)) {
				logger.error("客户端<--服务端发送的文件数<{}>报文，响应码<{}>非成功，可能是鉴权不通过", num, answer_code);
				return;
			}
			if (0 == num) {
				logger.error("客户端<--服务端发送的文件数为0，结束下载程序");
				ctx.close();
				return;
			}
			fileNum = num;
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			logger.error("服务端发送的文件数报文异常。{}", pkg.toString());
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_04);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileEnd4007(ChannelHandlerContext ctx, Package pkg) {
		// 一个文件传输结束，生成文件
		int strChinaLength = StringUtil.strChinaLength(fileContent.toString());
		fileSize = fileSize - strChinaLength;
		if (fileSize == fileContent.length()) {
			logger.info("客户端<{}>,文件<{}>接收完成，大小匹配。", ctx.channel().remoteAddress(), fileName);
			try {
				logger.info("写入第<{}>个文件<{}>。", fileSavedNum, localFilePath + "/tmp/" + fileName);
				FileUtils.write(new File(localFilePath + "/tmp/" + fileName), fileContent, Constant.GB2312_STR);
				fileSavedNum++;
				fileContent.delete(0, fileContent.length());
			} catch (IOException e) {
				logger.error("文件" + fileName + "保存时出现异常，客户端主动断开连接。");
				// 保存文件出现异常，断开连接，让服务端再次传送文件
				pkg.setMsg_type(Constant.PRO_RESPONSE);
				pkg.setContent(Constant.ANSWER_CODE_50);
				ctx.writeAndFlush(pkg);
				ctx.close();
			}
			if (fileNum == fileSavedNum) {
				logger.info("所有文件接收完成，文件数：{}，开始从tmp目录转入download目录。", fileNum);
				try {
					Collection<File> listFiles = FileUtils.listFiles(new File(localFilePath + "/tmp/"), null, false);
					for (File file : listFiles) {
						if (33 == file.getName().length()) {
							FileUtils.moveFile(file, new File(localFilePath + "/download/" + file.getName()));
						} else {
							logger.error("文件<{}>名称长度不等于33，不进行转存。", file.getName());
						}
					}
					logger.info("文件全部接收完毕，关闭链路");
				} catch (IOException e) {
					logger.error("目录从<{}>到<{}>转移出错：{}", localFilePath + "/tmp/", localFilePath + "/download/",
							e.getMessage());
				}
			}
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
		} else {
			logger.error("文件<{}>收到4007结束命令，但是文件大小不匹配，文件大小：{},实际接收的大小：{}。", fileName, fileSize, fileContent.length());
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_08);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void response4008(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		logger.info("收到服务端发送的4008报文，内容是：{}",content);
	}

	public void des3request4009(ChannelHandlerContext ctx, Package pkg) {
		pkg = new Package();
		pkg.setClient_sync_info("thisisClient");
		pkg.setZip_flag("0");
		pkg.setEncrypt_flag("0");
		pkg.setVersion("01");
		pkg.setMsg_type(Constant.PRO_REQUEST_DESKEY);
		String name = StringUtil.addSpaceForStr_R("", 40);
		String code = StringUtil.addSpaceForStr_R(inst_code, 11);
		String keep = StringUtil.addZeroForStr_R("", 64);
		pkg.setContent(name + code + keep);
		pkg.setMac("1111222233334444");
		ctx.writeAndFlush(pkg);
	}

	public void des3response4010(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		String crypt = content.substring(0, 24);
		System.err.println("客户端<--服务器获取到的秘钥是：" + crypt);
		Attribute<String> attr2 = ctx.channel().attr(Constant.DES_KEY);
		attr2.set(crypt);
		requestDownload4002(ctx, pkg);
	}
}
