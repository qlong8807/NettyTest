/**
 * 
 */
package com.cyber.netty.http2;

/**
 * @author zyl
 * @date 2016年11月23日
 * 
 */

public class HelloController {

	public String say(String name,String age){
		System.err.println(name+"---"+age);
		return "U name is "+name+",age:"+age;
	}
	public String say1(){
		return "U name is ";
	}
}
