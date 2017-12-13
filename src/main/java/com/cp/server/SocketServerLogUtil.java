package com.cp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.cp.business.domain.SocketServerLog;
//import com.cp.business.service.SocketServerLogService;
//import com.cp.clearmsg.util.DateUtil;
//import com.cp.util.SpringContextUtil;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zyl
 * @date 2017年12月4日
 * @desc 异步保存Log对象到数据库
 */
public class SocketServerLogUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SocketServerLogUtil.class);
	
//	private static LinkedBlockingQueue<SocketServerLog> queue = new LinkedBlockingQueue<SocketServerLog>();
//
//	private static SocketServerLogService socketServerLogService = (SocketServerLogService) SpringContextUtil
//			.getBean("socketServerLogService");
	
	public static void insertLog(String level,String msg_type,ChannelHandlerContext ctx,String content,String institution) {
//		SocketServerLog log = new SocketServerLog();
//		log.setErr_level(level);
//		log.setMsg_type(msg_type);
//		log.setClient_ip(ctx.channel().remoteAddress().toString());
//		log.setInstitution(institution);
//		if(200 < content.length()) content = content.substring(0, 200);
//		log.setContent(content);
//		log.setLog_time(DateUtil.getDateNowFormat(DateUtil.FORMAT1));
//		queue.add(log);
	}
	
	public static void saveLog() {
//		logger.info("开始循环查询队列中是否有SocketServerLog对象");
//		while(true) {
//			if(queue.isEmpty()) {
//				try {
//					Thread.sleep(10*1000);
//				} catch (InterruptedException e) {
//				}
//			}else{
//				SocketServerLog log = null;
//				try {
//					log = queue.take();
//					socketServerLogService.insertByObj(log);
//				} catch (InterruptedException e) {
//					logger.error("从队列中获取Log对象<{}>并插入数据库出错：{}",null==log?"null":log.toString(),e.getMessage());
//				}
//			}
//		}
	}
	
}
