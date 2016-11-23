/**
 * 
 */
package com.cyber.netty.http2;

/**
 * @author zyl
 * @date 2016年11月23日
 * 
 */
public class MethodNotSupportedException extends Exception {

	private static final long serialVersionUID = 1L;
	public MethodNotSupportedException(String msg){
		super(msg);
	}
}
