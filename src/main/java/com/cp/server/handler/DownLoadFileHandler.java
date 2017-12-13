package com.cp.server.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.Constant;
import com.cp.client.Package;
import com.cp.client.StringUtil;
import com.cp.server.SocketServerLogUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * @author zyl
 * @date 2017年11月28日
 * @desc 客户端上传文件处理流程 1. (客户端)建立连接 2. (客户端)发送文件下载请求(4002) 3. (服务端)验证身份
 *       不通过则发送失败的文件数通知4006报文,响应码非00，并关闭连接，结束 下载; 通过则发送成功的文件数通知报文(4006)。
 *       如果4006报文中需要下发的文件为0,则中心关闭连接，结束下载。 4. (客户端)发送应答报文(4008) 5.
 *       (服务端)发送文件信息通知报文(4003) 6. (客户端)发送断点通知报文(4005) 7. (服务端)发送应答报文(4008) 8.
 *       (服务端)发送数据报文(4004) 9. (客户端)发送应答报文(4008) 10. 重复8，9两步直到文件传输结束 11.
 *       (服务端)发送文件传输结束报文(4007) 12. (客户端)发送应答报文(4008) 13. (服务端)将成功传送的文件移到备份目录
 *       重复5-13，直到所有的文件都传输完成 14. (客户端)关闭Socket连接 15. (客户端)断开拨号连接(有拨号的情况)
 */
public class DownLoadFileHandler {

	private Logger logger = LoggerFactory.getLogger(DownLoadFileHandler.class);

	private int fileNum = 0;// 下发的文件数
	private String fileName;
	private String fileDesc;
	private int fileSize;
	// 文件名集合
	private List<String> fileNameList = new ArrayList<String>();
	// 文件数计数器，发送第几个文件
	private int fileIndex = 0;
	// 已发送的文件大小
	private int fileSendedSize = 0;
	private StringBuilder fileContent = new StringBuilder(1024);
	private ConcurrentMap<String, String> files = new ConcurrentHashMap<String, String>();
	private String institutionCode = "";
	private String localFilePath = "";

	public DownLoadFileHandler(String data_dir) {
		this.localFilePath = data_dir;
	}

	/**
	 * 当前进行到哪一步。1鉴权完毕+文件数通知，2文件信息通知，3发送数据报文，4一个文件发送结束
	 */
	private int step = 0;

	public void fileDownRequest4002(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		String name = content.substring(0, 40).trim();// 机构简称
		String code = content.substring(40, 51).trim();// 机构代码
		String clearDate = content.substring(51, 64).trim();
		// TODO
		// 判断是否验证通过
		boolean flag = true;
		if (flag) {
			Attribute<String> attr = ctx.channel().attr(Constant.NETTY_IS_LOGIN);
			attr.set("islogin");
			Attribute<String> attr3 = ctx.channel().attr(Constant.INST_CODE);
			attr3.set(code);
//			Attribute<String> attr2 = ctx.channel().attr(Constant.DES_KEY);
//			attr2.set(key);
			// 鉴权通过，获取文件数和文件列表
			Collection<File> listFiles = FileUtils
					.listFiles(new File(localFilePath + "/" + institutionCode + "/download/"), null, false);
			for (File file : listFiles) {
				fileName = file.getName();
				if (33 != fileName.length())
					continue;
				fileNameList.add(fileName);
				try {
					String readFileToString = FileUtils.readFileToString(file, Constant.GB2312_STR);
					files.put(fileName, readFileToString);
				} catch (IOException e) {
					logger.error("读文件<{}>发生异常:", fileName, e.getMessage());
					SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx,
							"读文件" + fileName + "发生异常:" + e.getMessage(), institutionCode);
					pkg.setMsg_type(Constant.PRO_RESPONSE);
					pkg.setContent(Constant.ANSWER_CODE_50);
					ctx.writeAndFlush(pkg);
					ctx.close();
					return;
				}
			}
			fileNum = files.size();
			String numString = StringUtil.addZeroForStr_L("" + fileNum, 4);
			pkg.setMsg_type(Constant.PRO_FILE_NUM_NOTIFY_CMD);
			pkg.setContent(numString + Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
			if (0 == fileNum) {
				ctx.close();
			}
			step = 2;
		} else {
			// 鉴权不通过
			logger.error("鉴权不通过，客户端<{}>,机构代码<{}>。", ctx.channel().remoteAddress(), institutionCode);
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "下载鉴权不通过", institutionCode);
			pkg.setMsg_type(Constant.PRO_FILE_NUM_NOTIFY_CMD);
			pkg.setContent("0000" + Constant.ANSWER_CODE_50);
			ctx.writeAndFlush(pkg);
			ctx.close();
		}
	}

	public void fileBreakNotify4005(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (StringUtils.isNotBlank(content) && content.length() == 12) {
			String receiveSize = content.substring(0, 10).trim();
			String responCode = content.substring(10, 12).trim();
			logger.info("服务端接收到客户端<{}>，的断点通知，已接收大小：<{}>，响应码：<{}>。", ctx.channel().remoteAddress(), receiveSize,
					responCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_00);
			ctx.writeAndFlush(pkg);
			// 发送完应答，开始发送数据报文
			fileName = fileNameList.get(fileIndex);
			fileContent.delete(0, fileContent.length());
			fileContent.append(files.get(fileName));
			fileSize = fileContent.length();
			int end_flag = 0;
			int temp_end = fileSendedSize + 2000;
			if (temp_end > fileSize) {
				temp_end = fileSize;
				end_flag = 1;
			}
			String temp_content = fileContent.substring(fileSendedSize, temp_end);
			pkg.setMsg_type(Constant.PRO_DATA_CMD);
			pkg.setContent(end_flag + temp_content);
			ctx.writeAndFlush(pkg);
			fileSendedSize = temp_end;
			step = 3;
		} else {
			SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx, "无效的报文长度", institutionCode);
			pkg.setMsg_type(Constant.PRO_RESPONSE);
			pkg.setContent(Constant.ANSWER_CODE_04);
			ctx.writeAndFlush(pkg);
		}
	}

	public void clientResponse4008(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (Constant.ANSWER_CODE_00.equals(content)) {
			if (2 == step) {
				if (0 != fileIndex) {
					// 一个文件已经发送完成,将文件转移至备份目录
					logger.info("正在将文件<{}>转移至备份目录", fileName);
					try {
						FileUtils.moveFile(new File(localFilePath + "/" + institutionCode + "/download/" + fileName),
								new File(localFilePath + "/" + institutionCode + "/downbak/" + fileName));
					} catch (IOException e) {
						logger.error("将文件<{}>转移至备份目录发生异常:{}", fileName, e.getMessage());
						SocketServerLogUtil.insertLog(Constant.ERROR, pkg.getMsg_type(), ctx,
								"将文件" + fileName + "转移至备份目录发生异常:" + e.getMessage(), institutionCode);
						pkg.setMsg_type(Constant.PRO_RESPONSE);
						pkg.setContent(Constant.ANSWER_CODE_50);
						ctx.writeAndFlush(pkg);
						ctx.close();
						return;
					}
					logger.info("将文件<{}>转移至备份目录完成", fileName);
					SocketServerLogUtil.insertLog(Constant.INFO, pkg.getMsg_type(), ctx, "将文件" + fileName + "转移至备份目录完成",
							institutionCode);
				}
				if (fileIndex == (fileNum)) {
					logger.info("客户端<{}>的文件全部发送完毕，关闭链路", ctx.channel().remoteAddress() + "-" + institutionCode);
					SocketServerLogUtil.insertLog(Constant.INFO, pkg.getMsg_type(), ctx, "文件全部发送完毕，关闭链路",
							institutionCode);
					// 判断文件是否已全部发送完成,如果发送完成则关闭链路
					ctx.close();
				} else {
					logger.info("发送文件信息通知");
					String temp_fileName = fileNameList.get(fileIndex);
					fileContent.delete(0, fileContent.length());
					fileContent.append(files.get(temp_fileName));
					fileSize = fileContent.length();
					int namelength = temp_fileName.length();
					if (namelength > 50)
						temp_fileName = temp_fileName.substring(0, 50);
					if (namelength < 50)
						temp_fileName = StringUtil.addSpaceForStr_R(temp_fileName, 50);
					fileDesc = StringUtil.addSpaceForStr_R(fileDesc, 256);
					String fileSizeString = StringUtil.addZeroForStr_L("" + fileSize, 10);
					String keep = StringUtil.addSpaceForStr_L("", 256);
					pkg.setMsg_type(Constant.PRO_FILE_INFO_NOTIFY_CMD);
					pkg.setContent(temp_fileName + fileDesc + fileSizeString + keep);
					ctx.writeAndFlush(pkg);
					step = 3;
				}
			} else if (3 == step) {
				if (fileSendedSize == fileSize) {
					// 一个文件已发完，发送文件结束4007
					fileIndex++;
					step = 2;
					fileSendedSize = 0;
					pkg.setMsg_type(Constant.PRO_END_CMD);
					pkg.setContent("00000000");
					ctx.writeAndFlush(pkg);
					return;
				}
				int end_flag = 0;
				int temp_end = fileSendedSize + 4000;
				if (temp_end > fileSize) {
					temp_end = fileSize;
					end_flag = 1;
				}
				String temp_content = fileContent.substring(fileSendedSize, temp_end);
				pkg.setMsg_type(Constant.PRO_DATA_CMD);
				pkg.setContent(end_flag + temp_content);
				ctx.writeAndFlush(pkg);
				fileSendedSize = temp_end;
			}
		} else {
			logger.error("请检查！！！客户端<{}>，发送的非成功应答码为：<{}>，当前进行到的步骤：<{}>（2文件信息通知，3发送数据报文），当前下发文件名<{}>,已发送长度<{}>",
					ctx.channel().remoteAddress(), content, step, fileName, fileSendedSize);
			SocketServerLogUtil
					.insertLog(
							Constant.ERROR, pkg.getMsg_type(), ctx, "客户端发送的非成功应答码为：" + content + "，当前进行到的步骤：" + step
									+ "（2文件信息通知，3发送数据报文），当前下发文件名" + fileName + ",已发送长度" + fileSendedSize,
							institutionCode);
		}
	}
}
