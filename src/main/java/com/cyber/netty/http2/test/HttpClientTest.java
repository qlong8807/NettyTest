package com.cyber.netty.http2.test;

import java.util.HashMap;
import java.util.Map;


public class HttpClientTest {

	public static void main(String[] args) throws Exception {

		/* Post Request */
		Map<String,Object> dataMap = new HashMap<String,Object>();
		dataMap.put("username", "Nick Huang");
		dataMap.put("name", "Nick Huang");
		dataMap.put("blog", "IT");
		dataMap.put("age", "11");
//		System.out.println(new HttpRequestor().doPost("http://localhost:8080/OneHttpServer/", dataMap));
//		System.out.println(new HttpRequestor().doPost("http://chushou.tv/chushou/login.htm", dataMap));

		/* Get Request */
//		System.out.println(new HttpRequestor()
//				.doGet("http://localhost:8080/OneHttpServer/"));
		
//		System.out.println(new HttpRequestor().doGet("http://localhost:8989/src/main/java/com/cyber?names=whf&abc=123"));
		System.out.println(new HttpRequestor().doPost("http://localhost:8989/src/hello/say/", dataMap));
	}
}
