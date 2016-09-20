/**
 * 
 */
package com.cyber.netty.byte2;

/**
 * @author zyl
 * @date 2016年9月19日
 * 
 */
public class Constant {

	public static final int PACKET_MAX_LENGTH = 1023;//Integer.MAX_VALUE;
	public static byte pkBegin = 0x6A;
	public static byte pkEnd = 0x6C;
	public static final byte[] escapeByte = new byte[] { 0x6A, 0x6B, 0x6C, 0x6D };
	public static final byte[][] toEscapeByte = new byte[][] { { 0x6B, 0x01 },
			{ 0x6B, 0x02 }, { 0x6D, 0x01 }, { 0x6D, 0x03 } };
}
