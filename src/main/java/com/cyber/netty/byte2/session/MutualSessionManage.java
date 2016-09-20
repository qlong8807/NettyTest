package com.cyber.netty.byte2.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutualSessionManage {
	// 链路缓存，Key链路ID，Value自定义链路对象
	private static Map<Long, MutualSession> sessionMap = new ConcurrentHashMap<Long, MutualSession>();
	// 节点编号与链路ID映射缓存，Key节点编号，Value链路ID
	private static Map<Long, Long> sessionMapping = new ConcurrentHashMap<Long, Long>();
	private static Logger logger = LoggerFactory.getLogger(MutualSessionManage.class);
	private static MutualSessionManage instance = new MutualSessionManage();
	private MutualSessionManage() {
	}

	public static Map<Long, Long> getSessionMapping() {
		return sessionMapping;
	}

	public final static MutualSessionManage getInstance() {
		return instance;
	}

	/**
	 * 添加一个链路对象
	 * 
	 * @param key：链路ID
	 * @param mutualSession
	 * @return
	 */
	public MutualSession addMutualSession(Long key, MutualSession mutualSession) {
		logger.info("bind mutualSession:"+key+"--->"+mutualSession);
		return sessionMap.put(key, mutualSession);
	}

	/**
	 * 根据链路ID移除一个链路对象
	 * 
	 * @param key
	 * @return
	 */
	public boolean delMutualSession(Long key) {
		MutualSession mutualSession = sessionMap.get(key);
		if (mutualSession != null) {
			if (mutualSession.getChannel() != null)
				mutualSession.getChannel().close();
		}
		logger.info("remove Session:"+key);
		return sessionMap.remove(key) != null;
	}

	/**
	 * 根据链路ID获取一个链路对象
	 * 
	 * @param key
	 * @return
	 */
	public MutualSession getMutualSessionForSessionId(long key) {
		return sessionMap.get(key);
	}

	/**
	 * 获取服务的所有链路列表
	 * 
	 * @return
	 */
	public Map<Long, MutualSession> get() {
		Map<Long, MutualSession> temp = sessionMap;
		return temp;
	}

}
