package com.cp.client.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
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
 * @desc 客户端上传文件处理流程 1. (客户端)建立连接 2. (客户端)发送文件上传请求报文(4001) 3.
 *       (服务端)验证身份，发送应答报文(4008) 4. (客户端)发送文件数通知报文(4006) 5. (服务端)发送应答报文(4008) 6.
 *       (客户端)发送文件信息通知报文(4003) 7. (服务端)发送断点通知报文(4005) 8. (客户端)发送数据报文(4004) 9.
 *       (服务端)发送应答报文(4008) 10. 重复8、9，直至文件传输完成 11. (客户端)发送文件传输结束报文(4007) 12.
 *       (服务端)发送应答报文(4008) 13. 转第6步，开始下一个文件的传输，如无文件则执行第14步 14. (客户端)关闭Socket连接
 *       15. (客户端)断开拨号连接 (有拨号的情况)
 */
public class ClientUploadHandler {
	private static Logger logger = LoggerFactory.getLogger(ClientUploadHandler.class);
	private static String inst_code = "02017910";
	private static String localFilePath = "/Users/apple/Documents/test/frontSocket";
	/**
	 * 文件名集合
	 */
	private List<String> fileNameList = new ArrayList<String>();
	/**
	 * 文件名-文件内容
	 */
	private ConcurrentMap<String, String> files = new ConcurrentHashMap<String, String>();
	private int fileIndex = 0;
	private int fileSendedSize = 0;
	private int fileCurrentSize = 0;
	/**
	 * 1-已发送4001，2-已发送4006，3-已发送4003，4-已发送4004,5-已发送4007
	 */
	private int step = 0;

	public void requestUpload4001(ChannelHandlerContext ctx, Package pkg) {
		if(null == pkg) {
			pkg = new Package();
			pkg.setClient_sync_info("thisisClient");
			pkg.setZip_flag("0");
			pkg.setEncrypt_flag("0");
			pkg.setVersion("01");
		}else {
			pkg.setEncrypt_flag("1");
		}
		pkg.setMsg_type(Constant.PRO_UP_REQUEST_CMD);
		String name = StringUtil.addSpaceForStr_R("", 40);
		String code = StringUtil.addSpaceForStr_R(inst_code, 11);
		String keep = StringUtil.addForStr_R("", "F", 256);
		pkg.setContent(name + code + keep);
		pkg.setMac("1111222233334444");
		ctx.writeAndFlush(pkg);
		step = 1;
	}

	public void fileInfo4003(ChannelHandlerContext ctx, Package pkg) {
		logger.info("发送文件信息通知");
		String temp_fileName = fileNameList.get(fileIndex);
		String fileContent = files.get(temp_fileName);
		fileCurrentSize = fileContent.length();
		fileSendedSize = 0;
		int namelength = temp_fileName.length();
		if (namelength > 50)
			temp_fileName = temp_fileName.substring(0, 50);
		if (namelength < 50)
			temp_fileName = StringUtil.addSpaceForStr_R(temp_fileName, 50);
		String fileDesc = StringUtil.addSpaceForStr_R("filedesc256", 256);
		String fileSizeString = StringUtil.addZeroForStr_L("" + fileCurrentSize, 10);
		String keep = StringUtil.addSpaceForStr_L("", 256);
		pkg.setMsg_type(Constant.PRO_FILE_INFO_NOTIFY_CMD);
		pkg.setContent(temp_fileName + fileDesc + fileSizeString+keep);
		ctx.writeAndFlush(pkg);
		step = 3;
	}

	public void fileData4004(ChannelHandlerContext ctx, Package pkg) {
		int fileNum = fileNameList.size();
		if (fileIndex == fileNum) {
			// 文件发送完毕，关闭链路
			ctx.close();
			return;
		}
		if (0 != fileSendedSize && fileSendedSize == fileCurrentSize) {
			fileIndex++;
			fileEnd4007(ctx, pkg);
			return;
		}
		String fileName = fileNameList.get(fileIndex);
		String fileContent = files.get(fileName);
		int fileSize = fileContent.length();
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
		step = 4;
	}

	public void fileBreakNotify4005(ChannelHandlerContext ctx, Package pkg) {
		// 未实现断点续传，收到4005时直接给服务端发送4004
		fileData4004(ctx, pkg);
	}

	public void fileNum4006(ChannelHandlerContext ctx, Package pkg) {
		Collection<File> listFiles = FileUtils.listFiles(new File(localFilePath + "/upload/"), null, false);
		for (File file : listFiles) {
			if(33 != file.getName().length()) continue;
			fileNameList.add(file.getName());
			String readFileToString;
			try {
				readFileToString = FileUtils.readFileToString(file, Constant.GB2312_STR);
				files.put(file.getName(), readFileToString);
			} catch (IOException e) {
				logger.error("读文件{}发生异常：{}", file.getName(), e.getMessage());
			}
		}
		int size = fileNameList.size();
		String sizeStr = StringUtil.addZeroForStr_L(size + "", 4);
		pkg.setMsg_type(Constant.PRO_FILE_NUM_NOTIFY_CMD);
		pkg.setContent(sizeStr + Constant.ANSWER_CODE_00);
		ctx.writeAndFlush(pkg);
		step = 2;
	}

	public void fileEnd4007(ChannelHandlerContext ctx, Package pkg) {
		pkg.setMsg_type(Constant.PRO_END_CMD);
		pkg.setContent("00000000");
		ctx.writeAndFlush(pkg);
		step = 5;
	}

	public void uploadResponse4008(ChannelHandlerContext ctx, Package pkg) {
		String content = pkg.getContent();
		if (Constant.ANSWER_CODE_00.equals(content)) {
			if (1 == step) {
				fileNum4006(ctx, pkg);
			} else if (2 == step) {
				fileInfo4003(ctx, pkg);
			} else if (3 == step) {
				logger.error("不识别的step");
			} else if (4 == step) {
				fileData4004(ctx, pkg);
			} else if (5 == step) {
				int fileNum = fileNameList.size();
				if (fileIndex == fileNum) {
					logger.info("文件发送完毕，关闭链路");
					ctx.close();
					return;
				}
				fileInfo4003(ctx, pkg);
			} else {
				logger.error("不识别的step");
			}
		} else {
			logger.error("请检查！！！收到非成功响应码：{}", content);
		}
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
		requestUpload4001(ctx, pkg);
	}
}
