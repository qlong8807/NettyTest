package com.cp.client;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {

	/*
	 * 16进制字符数组
	 */
	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	public static int getBegin(int begin, String sss) {
		if (begin == 0) {
			return 0;
		}
		char[] c = sss.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
			if (len == begin) {
				return i;
			}
		}
		return len;
	}

	public static int getEnd(int end, String sss) {
		if (end == 0) {
			return 0;
		}
		char[] c = sss.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
			if (len == end) {
				return i;
			}
		}

		return len;
	}

	public static int length(String s) {
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}

	/*
	 * 偏移位置（一个中文一个汉字），长度（1一个汉字两个），源文件， （最后优化，最优，适合截图记录）
	 */
	public static String subChString(int offset, int length, String s) {
		if (s == null || length == 0)
			return "";
		char[] c = s.toCharArray();
		int len = 0;
		int i = offset;
		for (i = offset; i < s.length(); i++) {
			len++;
			if (!isLetter(c[i])) {
				len++;
			}
			if (len == length - offset) {
				break;// 此时的i就是substring的截止
			}
		}
		return s.substring(offset, i + 1);
	}

	/*
	 * 测试
	 */
	public static String subChs(int begin, int end, String s) {
		if (s == null || end - begin == 0)
			return "";
		char[] c = s.toCharArray();
		char[] r = new char[end - begin];
		for (int i = begin; i < end - begin; i++) {
			if (!isLetter(c[i])) {
				continue;
			} else {
				r[i] = c[i];
			}
		}
		System.out.println(r.length);
		return new String(r);
	}

	/*
	 * 获取中文字段个数
	 */
	public static int getChNum(String s) {
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++) {
			if (!isLetter(c[i])) {
				len++;
			}
		}
		return len;
	}

	public static boolean isLetter(char c) {
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	public static boolean isChinens(char c) throws UnsupportedEncodingException {
		if (String.valueOf(c).getBytes("GB2312").length > 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 字符串截取(字符串可包含中文) 截取从指定开始位置和指定结束位置的字符串
	 * 
	 * @param source
	 *            源字符串
	 * @param start
	 *            截取的起始位置
	 * @param end
	 *            截取结束位置
	 * @return
	 */
	public static String subString(String source, int start, int end) {
		try {
			source = new String(source.getBytes("GBK"), "ISO-8859-1");
			return new String(source.substring(start, end).getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 字符串截取(字符串可包含中文) 截取从指定位置开始的指定长度的字符串
	 * 
	 * @param source
	 *            源字符串
	 * @param start
	 *            截取的起始位置
	 * @param end
	 *            截取长度
	 * @return
	 */
	public static String subStringSpecifiedLength(String source, int start, int count) {
		try {
			source = new String(source.getBytes("GBK"), "ISO-8859-1");
			return new String(source.substring(start, start + count).getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 字节数组转十六进制字符串
	 * 
	 * @param ba
	 *            原数据
	 * @param offset
	 *            开始位移
	 * @param length
	 *            长度
	 * @return 16进制字符串
	 */
	public final static String ba2HexString(byte[] ba, int offset, int length) {
		char[] buf = new char[length << 1];
		for (int i = 0, j = 0, k; i < length;) {
			k = ba[offset + i++];
			buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
			buf[j++] = HEX_DIGITS[k & 0x0F];
		}
		return new String(buf);
	}

	/**
	 * 16进制字符串转字节数组
	 * 
	 * @param s
	 *            16进制字符串
	 * @return 字节数组
	 */
	public final static byte[] hexString2Ba(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		int limit = s.length();
		byte[] result = new byte[((limit + 1) / 2)];
		int i = 0, j = 0;
		if ((limit % 2) == 1) {
			result[j++] = (byte) char2Byte(s.charAt(i++));
		}
		while (i < limit) {
			result[j] = (byte) (char2Byte(s.charAt(i++)) << 4);
			result[j++] |= (byte) char2Byte(s.charAt(i++));
		}
		return result;
	}

	/*
	 * 字符转字节
	 * 
	 * @param c
	 * 
	 * @return
	 */
	private static byte char2Byte(char c) {
		if (c >= '0' && c <= '9') {
			return (byte) (c - '0');
		} else if (c >= 'A' && c <= 'F') {
			return (byte) (c - 'A' + 10);
		} else if (c >= 'a' && c <= 'f') {
			return (byte) (c - 'a' + 10);
		} else
			throw new IllegalArgumentException("Invalid hexadecimal digit: " + c);
	}

	// 优化前的做个备份
	public static String subBufString(int begin, int end, String source) {
		String ret = "";
		if (end > length(source)) {
			end = length(source);
		} else if (end - begin == 0) {
			return ret;
		}
		try {
			byte[] bytes = source.getBytes("GB2312");
			String ss = ba2HexString(bytes, begin, end - begin);
			byte[] b1 = hexString2Ba(ss);
			ret = new String(b1, "GB2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;

	}

	/*
	 * 将一个长传的某些位置的长度替换为其他串 原串：source 替换串：repStr 位置数组： position
	 */
	public static String replace(String source, String repStr, int[] position) {
		int repLength = repStr.length();
		int srcLength = length(source);
		for (int i = 0; i < position.length; i++) {
			source = subString(0, position[i], source) + repStr + subString(position[i] + repLength, srcLength, source);
		}
		return source;
	}
	


	/**
	 * 
	 * @Title: mkString 
	 * @Description: 讲null 或者""转换为"" @param @param str 
	 *@return String 返回类型 @throws
	 */
	public static String mkString(final String str) {
		return StringUtils.trimToEmpty(str);
	}

	/**
	 * 
	 * @Title: isBlank 
	 * @Description: 判断该字符串是否为""，如果为""返回true，否则返货false 
	 * @param @param
	 * @return boolean 返回类型 @throws
	 */
	public static boolean isBlank(final String str) {
		return StringUtils.isBlank(mkString(str));
	}

	/**
	 * 
	 * @Title: dateNow 
	 * @Description: 获取当前时间 
	 * @param @param formate日期格式化
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String dateNow(String formate) {
		SimpleDateFormat dateformate = new SimpleDateFormat(formate);
		return dateformate.format(new Date());
	}

	/**
	 * 
	 * @Title: beanCopy @Description: bean 拷贝 @param @param dist 目标对象 @param @param
	 *         org 原始对象 @return void 返回类型 @throws
	 */
	public static void beanCopy(Object dest, Object orig) {
		try {
			BeanUtils.copyProperties(dest, orig);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: beanToMap @Description: 将bean转换为map集合 @param @param bean @param @return
	 *         设定文件 @return Map<String,String> 返回类型 @throws
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> beanToMap(Object bean) {
		try {
			return PropertyUtils.describe(bean);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @Title: map2Bean @Description: 将map转换为bean @param @param map @param @param
	 *         class1 @param @return 设定文件 @return T 返回类型 @throws
	 */
	public static <T> T map2Bean(Map<String, String> map, Class<T> class1) {
		T bean = null;
		try {
			bean = class1.newInstance();
			BeanUtils.populate(bean, map);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return bean;
	}

	/**
	 * 字符串不足位数 右补空格
	 * 
	 * @param str
	 * @param strLength
	 */
	public static String addSpaceForStr_R(String str, int strLength) {
		int strLen = strLength(str);
		if (strLen < strLength) {
			if (StringUtils.isBlank(str)) {
				str = "";
			}
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				sb.append(str).append(" ");// 空格
				// sb.append(" ").append(str);//左补
				str = sb.toString();
				strLen += 1;
			}
		}
		return str;
	}

	/**
	 * 字符串不足位数 右补0
	 * 
	 * @param str
	 * @param strLength
	 */
	public static String addZeroForStr_R(String str, int strLength) {
		int strLen = strLength(str);
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				if (StringUtils.isNotBlank(str)) {
					sb.append(str).append(Constant.ZERO_NUMBER);// 右补
				} else {
					sb.append(Constant.ZERO_NUMBER);
				}

				// sb.append(" ").append(str);//左补
				str = sb.toString();
				strLen += 1;
			}
		}
		return str;
	}

	/**
	 * 字符串不足位数 左补空格
	 * 
	 * @param str
	 * @param strLength
	 */
	public static String addSpaceForStr_L(String str, int strLength) {
		int strLen = strLength(str);
		if (strLen < strLength) {
			if (StringUtils.isBlank(str)) {
				str = "";
			}
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				// sb.append(str).append(" ");//空格
				sb.append(" ").append(str);// 左补
				str = sb.toString();
				strLen += 1;
			}
		}
		return str;
	}

	/**
	 * 字符串不足位数 左补0
	 * 
	 * @param str
	 * @param strLength
	 */
	public static String addZeroForStr_L(String str, int strLength) {
		int strLen = strLength(str);
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				// sb.append(str).append("0");//右补
				if (StringUtils.isNotBlank(str)) {
					sb.append(Constant.ZERO_NUMBER).append(str);// 左补
				} else {
					sb.append(Constant.ZERO_NUMBER);
				}
				str = sb.toString();
				strLen += 1;
			}
		}
		return str;
	}

	/**
	 * 字符串不足位数 左补指定字符
	 * 
	 * @param str
	 * @param strLength
	 */
	public static String addForStr_L(String str, String s, int strLength) {
		int strLen = strLength(str);
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				sb.append(s).append(str);// 左补
				str = sb.toString();
				strLen += 1;
			}
		}
		return str;
	}

	/**
	 * 获取字符串长度(字符串中可包含中文)
	 * 
	 * @param str
	 * @return
	 */
	private static int strLength(String str) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		if (StringUtils.isNotBlank(str)) {
			for (int i = 0; i < str.length(); i++) {
				/* 获取一个字符 */
				String temp = str.substring(i, i + 1);
				/* 判断是否为中文字符 */
				if (temp.matches(chinese)) {
					/* 中文字符长度为2 */
					valueLength += 2;
				} else {
					/* 其他字符长度为1 */
					valueLength += 1;
				}
			}
		}

		return valueLength;
	}
	/**
	 * 判断有几个汉字
	 * @param str
	 * @return
	 */
	public static int strChinaLength(String str) {
		int len = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		if (StringUtils.isNotBlank(str)) {
			for (int i = 0; i < str.length(); i++) {
				/* 获取一个字符 */
				String temp = str.substring(i, i + 1);
				/* 判断是否为中文字符 */
				if (temp.matches(chinese)) {
					/* 中文字符长度为2 */
					len++;
				} else {
					/* 其他字符长度为1 */
				}
			}
		}
		return len;
	}

	/**
	 * 字符串不足位数 右补指定字符
	 * 
	 * @param str
	 * @param strLength
	 */
	public static String addForStr_R(String str, String s, int strLength) {
		int strLen = strLength(str);
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				sb.append(str).append(s);// 右补
				str = sb.toString();
				strLen += 1;
			}
		}
		return str;
	}

	/**
	 * 补齐不足长度
	 * @param length长度
	 * @param number数字
	 * @return
	 */
	public static String lpad(int number, int length) {
		String f = "%0" + length + "d";
		return String.format(f, number);
	}

	public static String addValueToLength_R(String source, String type,
			int length) {
		String ret = "";
		if (source == null || "".equals(source)) {
			if (Constant.ZERO_STRING.equals(type)) {
				ret = addZeroForStr_R(ret, length);
			} else if (Constant.SPACE_STRING.equals(type)) {
				ret = addSpaceForStr_R(ret, length);
			}
		} else {
			if (Constant.ZERO_STRING.equals(type)) {
				ret = addZeroForStr_R(source, length);
			} else if (Constant.SPACE_STRING.equals(type)) {
				ret = addSpaceForStr_R(source, length);
			}
		}
		return ret;
	}

	public static String addValueToLength_L(String source, String type,
			int length) {
		String ret = "";
		if (source == null || "".equals(source)) {
			if (Constant.ZERO_STRING.equals(type)) {
				ret = addZeroForStr_L(ret, length);
			} else if (Constant.SPACE_STRING.equals(type)) {
				ret = addSpaceForStr_L(ret, length);
			}
		} else {
			if (Constant.ZERO_STRING.equals(type)) {
				ret = addZeroForStr_L(source, length);
			} else if (Constant.SPACE_STRING.equals(type)) {
				ret = addSpaceForStr_L(source, length);
			}
		}
		return ret;
	}

	// 优化后的
	public static String subString(int begin, int end, String source) {
		String ret = "";
		if (end > length(source))
			end = length(source);
		try {
			byte[] bytes = source.getBytes(Constant.GB2312_STR);
			byte[] str = new byte[end - begin];// 要截取的字节码
			int index = 0;
			for (int i = begin; i < end; i++) {
				str[index++] = bytes[i];// 获取100-130的字节码
			}
			ret = new String(str, Constant.GB2312_STR);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 字符串截取(字符串可包含中文) 截取从指定开始位置和指定结束位置的字符串
	 * 
	 * @param source
	 *            源字符串
	 * @param start
	 *            截取的起始位置
	 * @param end
	 *            截取结束位置
	 * @return
	 */
	public static String subStr(String source, int start, int end) {
		try {
			source = new String(source.getBytes(Constant.GBK_ENCODE_FORMATE), Constant.NUMBER_FORMATE);
			return new String(source.substring(start, end).getBytes(
					Constant.NUMBER_FORMATE), Constant.GBK_ENCODE_FORMATE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 转换source的编码格式
	 * @param source
	 * @param originCharset
	 * @param destCharset
	 * @return
	 */
	public static String charsetChange(String source,String originCharset,String destCharset) {
		try {
			byte[] bytes = source.getBytes(originCharset);
			String result = new String(bytes,destCharset);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String s1 = "2015010311453080   苏H05600            17路                                                        010001340000000121600001340000000121600000000000000000000";
		System.out.println(StringUtil.addForStr_R("A", "F", 8));
	}

}
