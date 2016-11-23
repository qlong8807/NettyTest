/**
 * 
 */
package com.cyber.netty.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import javax.activation.MimetypesFileTypeMap;

/**
 * @author zyl
 * @date 2016年7月22日
 * 
 */
public class HttpServerHandler extends
		SimpleChannelInboundHandler<FullHttpRequest> {
	private String url;
	private static final Pattern PATTERN_URL = Pattern.compile(".*[<>&\"].*");
	private static final Pattern ALLOWED_FILE_NAME = Pattern
			.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	private ClassPool pool = ClassPool.getDefault();

	public HttpServerHandler(String url) {
		this.url = url;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext context,
			FullHttpRequest request) throws Exception {
		if (!request.getDecoderResult().isSuccess()) {
			sendError(context, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		// 只接收post请求
		if (request.getMethod() != HttpMethod.POST) {
			sendError(context, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		String uri = request.getUri();
		System.out.println("请求的uri:" + uri);
		//将GET, POST所有请求参数转换成Map对象
		Map<String, String> parmMap = RequestParser.parse(request);
		System.out.println(parmMap);
		System.out.println("------------------------------");
		String methodName = "say";

		Class<?> clazz = Class.forName("com.cyber.netty.http2.HelloController");
		Object newInstance = clazz.newInstance();
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.getName().equals(methodName)) {
				int parameterCount = method.getParameterCount();
				System.out.println("参数个数为：" + parameterCount);
				Class<?>[] parameterTypes = method.getParameterTypes();
				for (Class<?> clas : parameterTypes) {
					String parameterName = clas.getName();
					System.out.println("参数名称:" + parameterName);
				}
				System.out.println("*****************************");
				CtClass cc = pool.get(clazz.getName());
				CtMethod cm = cc.getDeclaredMethod(methodName);
				// 使用javaassist的反射方法获取方法的参数名
				MethodInfo methodInfo = cm.getMethodInfo();
				CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
				LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
						.getAttribute(LocalVariableAttribute.tag);
				if (attr == null) {
					// exception
				}
				// paramNames即参数名
				String[] paramNames = new String[cm.getParameterTypes().length];
				int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
				for (int i = 0; i < paramNames.length; i++)
					paramNames[i] = attr.variableName(i + pos);
				Object[] args = new Object[paramNames.length];
				for (int i = 0; i < paramNames.length; i++) {
					args[i] = parmMap.get(paramNames[i]);
				}
				
				Object invoke = method.invoke(newInstance, args);
				sendMessage(context, (String) invoke);
			}
		}

	}

	/**
	 * 发送字符串消息
	 * 
	 * @param context
	 * @param result
	 */
	private void sendMessage(ChannelHandlerContext context, String result) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				"text/html;charset=UTF-8");
		ByteBuf buffer = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		context.writeAndFlush(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 判断当前request是否为keepalive
	 * 
	 * @param request
	 * @return
	 */
	private boolean isKeepAlive(FullHttpRequest request) {
		String string = request.headers().get(HttpHeaders.Names.CONNECTION);
		if (null != string && HttpHeaders.Values.KEEP_ALIVE.equals(string)) {
			return true;
		}
		return false;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		System.out.println("complete................");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		System.err.println("发生异常。。。");
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static void sendRedirect(ChannelHandlerContext context, String uri) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
		response.headers().set(HttpHeaders.Names.LOCATION, uri);
		context.writeAndFlush(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 发送错误信息
	 * 
	 * @param context
	 * @param status
	 */
	private static void sendError(ChannelHandlerContext context,
			HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure:"
						+ status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				"text/plain;charset=UTF-8");
		context.writeAndFlush(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 设置response的Content-Type
	 * 
	 * @param response
	 * @param file
	 */
	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mime = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				mime.getContentType(file.getPath()));
	}

	/**
	 * 设置response的Content-Length
	 * 
	 * @param response
	 * @param length
	 */
	private static void setContentLength(HttpResponse response, long length) {
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, length);
	}
}
