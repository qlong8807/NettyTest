/**
 * 
 */
package com.cyber.netty.http2;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * netty的request请求参数解析器, 支持GET, POST
 */
public class RequestParser {
	/**
	 * 解析请求参数
	 * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
	 * @throws BaseCheckedException
	 * @throws IOException
	 */
	public static Map<String, String> parse(FullHttpRequest fullReq) throws MethodNotSupportedException,
			IOException {
		HttpMethod method = fullReq.getMethod();
		Map<String, String> parmMap = new HashMap<>();
		if (HttpMethod.GET == method) {
			// 是GET请求
			QueryStringDecoder decoder = new QueryStringDecoder(
					fullReq.getUri());
			Set<Entry<String, List<String>>> entrySet = decoder.parameters()
					.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				// entry.getValue()是一个List, 只取第一个元素
				parmMap.put(entry.getKey(), entry.getValue().get(0));
			}
		} else if (HttpMethod.POST == method) {
			// 是POST请求
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
			decoder.offer(fullReq);

			List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

			for (InterfaceHttpData parm : parmList) {

				Attribute data = (Attribute) parm;
				parmMap.put(data.getName(), data.getValue());
			}

		} else {
			// 不支持其它方法
			throw new MethodNotSupportedException(""); // 这是个自定义的异常, 可删掉这一行
		}

		return parmMap;
	}
}
