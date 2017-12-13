/**
 * @Title:公用常量
 * @Copyright: Copyright (c) 201006
 * @Company: M
 
 * @version 1.0
 */
package com.cp.client;

import java.util.HashMap;
import java.util.Map;

import io.netty.util.AttributeKey;

public class Constant {
	public static final String USER_SESSION_KEY = "USER_SESSION_KEY";
	public static final String LOGID_SESSION_KEY = "LOGID_SESSION_KEY";
	public static final String FUNCID = "FUNCSID";// 功能编号

	/*
	 * 远程访问主机
	 */
	public static final String REQUEST_HOST = "REQUEST_HOST";
	/*
	 * 远程访问主机端口
	 */
	public static final String REQUEST_PORT = "REQUEST_PORT";
	/*
	 * 本地主机IP
	 */
	public static final String LOCAL_HOST = "LOCAL_HOST";
	/*
	 * 本地主机端口
	 */
	public static final String LOCAL_PORT = "LOCAL_PORT";
	/*
	 * 登录账号
	 */
	public static final String ACCOUNT = "account";
	/*
	 * 当前用户所在域
	 */
	public static final String USER_DOMAIN = "USER_DOMAIN";
	/*
	 * 用户组ID  key 用于session存取用户组ID
	 */
	public static final String GROUP_ID = "GROUP_ID";
	/**
	 * 全局配置表，配置CAS单点连接
	 */
	public static final String CAS_URL = "CAS_URL";
	
	public static final String TEMP4AUSER = "admin";
	
	public static final String USER_SESSION_ID = "USER_SESSION_ID";
	public static final String Radware_Address = "";// 服务地址
	public static final String Radware_UserName = ""; // 负载服务用户名
	public static final String Radware_Password = "";// 负载服务用户密码

	public static int PROCESS_LEVEL = 0;// 虚拟机创建进度
	
	public static final String CENTER_CENTER_CODE = "02017910";//省平台机构号
	
	public static final String GBK_ENCODE_FORMATE = "GBK";
	
	public static final String NUMBER_FORMATE = "ISO-8859-1";
	
	public static final String GB2312_STR = "GB2312";
	
	public static final String UTF8_STR = "UTF-8";
	
	public static final String ZERO_STRING = "zero";
	
	public static final String SPACE_STRING = "space";
	
	public static final String SOCKET_IP = "socketIp";
	
	public static final String SOCKET_PORT = "socketPort";
	
	public static final String LOCAL_FILEPATH = "localfilepath";
	/*************************************************************报文部分********************************************/
 	
	public static final String YYYY_MM_DD_STRING = "yyyyMMdd";
	
    public static final String STRING_00000001 = "00000001";
 	
 	public static final String STRING_001 = "001";
 	
 	public static final String  STRING_3123 = "3123";
 	
 	public static final String STRING_3011 = "3011";
 	
 	public static final String  STRING_0033 = "0033";
	
	public static final String STRING_000 = "000";
	
	public static final String  STRING_8000 = "8000";
	
	public static final String  PROD_STRING = "PROD";
	
	public static final String  ZERO_NUMBER =  "0" ;
	
	public static final String  STRING_01 = "01"; 
	
	public static final String CHAR_STRING = "\r\n";	
	/*
	 * 16进制字符数组
	 */
	public static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
	
	public static final Map<String,String> CODE_MAP = new HashMap<>();
	
	static{
		CODE_MAP.put("00", "成功");
		CODE_MAP.put("02", "无效的消息类型码");
		CODE_MAP.put("03", "无效的消息版本");
		CODE_MAP.put("04", "无效的报文长度");
		CODE_MAP.put("05", "MAC错");
		CODE_MAP.put("06", "文件摘要验证失败");
		CODE_MAP.put("07", "文件名不合法");
		CODE_MAP.put("08", "文件大小或内容不合法");
		CODE_MAP.put("09", "无效文件断点");
		CODE_MAP.put("50", "系统错误");
		
	}
	public final static String ANSWER_CODE_00 = "00";
	public final static String ANSWER_CODE_02 = "02";
	public final static String ANSWER_CODE_03 = "03";
	public final static String ANSWER_CODE_04 = "04";
	public final static String ANSWER_CODE_05 = "05";
	public final static String ANSWER_CODE_06 = "06";
	public final static String ANSWER_CODE_07 = "07";
	public final static String ANSWER_CODE_08 = "08";
	public final static String ANSWER_CODE_09 = "09";
	public final static String ANSWER_CODE_50 = "50";
	
	//部里电子钱包日表名
	public final static String BU_BUEWALLET_TABLE_NAME = "BU_BU_EWALLET";
	//部里电子现金日表名
	public final static String BU_BUECASH_TABLE_NAME = "BU_BU_ECASH";
	//省里电子钱包日表名
	public final static String BUECASH_TABLE_NAME = "BU_ECASH";
	//省里电子现金日表名
	public final static String BUEWALLET_TABLE_NAME = "BU_EWALLET";
	//差错类型- 0006-异常交易验证
	public final static String MISTAKE_TYPE_EXCEPTION = "0006";
	//差错类型- 0002-贷记调整；
	public final static String MISTAKE_TYPE_ADJUST = "0002";
	
	
	//卡交易信息当日表表名
	public final static String CARD_TRADE_TABLE_NAME = "CARD_TRADE_RECORDS";
	//交通部日切时间
	public final static String BU_CUT_TIME = " 08:30:00";
	//消费交易类型代码
	//8451 普通消费,8460 复合交易开始 ,8461 复合交易结束 ,8462 延时复合交易开始 ,8463 延时复合交易结束 ,8465 补票 ,8470锁卡，8471解锁
	public final static String CONSUME_TRADE_CODE = "8451,8460,8461,8462,8463,8464,8465,8470,8471";
	//锁卡
	public final static String LOCK_CARD_TRADE_CODE = "8470";
	//消费类修改卡账户的数据
	public final static String UPDATE_CARD_TRADE_CODE = "8451,8461,8463,8465,8470";
	//充值售卡交易类型代码
	//2062 充资交易 ,2063 售卡交易 
	public final static String RECHARGE_TRADE_CODE = "2062,2063";
	public final static String RECHARGE_CODE = "2062";
	public final static String SELLCARD_CODE = "2063";
	//退卡退资换卡请求交易类型代码
	//3450 退卡请求 ,3452 退卡完成 ,3454 退资请求 ,3456 退资完成 ,3458 换卡请求 ,3460 换卡完成 
	public final static String RETURN_CARD_REQUEST_TRADE_CODE = "3450,3454,3458";
	//退卡退资换卡完成交易类型代码
	public final static String RETURN_CARD_FINIST_TRADE_CODE = "3452,3456,3460";
	public final static String RETURN_CARD_TRADE_CODE = "3452";//退卡完成
	public final static String RETURN_MONEY_TRADE_CODE = "3456";//退资完成
	public final static String CHANGE_CARD_TRADE_CODE = "3460";//换卡完成
	//交通部socket上传数据类型，根据类型去文件存放目录表里面查ip，端口
	public final static int BU_UPLOAD_STROE_DATA_TYPE = 1;
	//交通部socket下发数据类型，根据类型去文件存放目录表里面查ip，端口
	public final static int BU_DOWNLOAD_STROE_DATA_TYPE = 3;
	//本地文件存放目录
	public final static int LOCAL_STROE_DATA_TYPE = 2;
	//FTP存放目录
	public final static int FTP_STROE_DATA_TYPE = 4;
	//部文件上传目录
	public final static int FTP_UPLOAD_DATA_TYPE = 5;
	
	//总账类型： 10：充值总账，11：售卡总账，12：消费确认总账，13：消费调整总账，
	//14：退卡总账，15：换卡总账，16：退资总账
	public final static String RECHARGE_LEDGER = "10";
	public final static String SELLCARD_LEDGER = "11";
	public final static String CONSUME_CONFIRM = "12";
	public final static String CONSUME_ADJUST = "13";
	public final static String RETURNCARD_LEDGER = "14";
	public final static String CHANGECARD_LEDGER = "15";
	public final static String RETURNMONEY_LEDGER = "16";
	
	//=====省平台服务端
	/**
	 * 应答报文
	 */
	public final static String PRO_RESPONSE = "4008";
	/**
	 * 文件上传请求报文
	 */
	public final static String PRO_UP_REQUEST_CMD = "4001";
	/**
	 * 文件数通知报文
	 */
	public final static String PRO_FILE_NUM_NOTIFY_CMD = "4006";
	/**
	 * 文件信息通知报文
	 */
	public final static String PRO_FILE_INFO_NOTIFY_CMD = "4003";
	/**
	 * 数据报文
	 */
	public final static String PRO_DATA_CMD = "4004";
	/**
	 * 传输结束报文
	 */
	public final static String PRO_END_CMD = "4007";
	/**
	 * 文件下载请求
	 */
	public final static String PRO_DOWN_REQUEST_CMD = "4002";
	/**
	 * 断点通知报文
	 */
	public final static String PRO_BREAK_NOTIFY_CMD = "4005";
	/**
	 * 请求3des加解密的key
	 */
	public final static String PRO_REQUEST_DESKEY = "4009";
	/**
	 * 返回3des加解密的key
	 */
	public final static String PRO_RESPONSE_DESKEY = "4010";
	
	/**
	 * 是否登录
	 */
	public static final AttributeKey<String> NETTY_IS_LOGIN = AttributeKey.valueOf("netty.channel.islogin");
	/**
	 * 秘钥
	 */
	public static final AttributeKey<String> DES_KEY = AttributeKey.valueOf("netty.channel.3deskey");
	/**
	 * 机构代码
	 */
	public static final AttributeKey<String> INST_CODE = AttributeKey.valueOf("netty.channel.code");
	
	public final static String DEBUG="DEBUG";
	public final static String INFO="INFO";
	public final static String ERROR="ERROR";
}