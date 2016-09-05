/**
 * 
 */
package com.cyber.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.Date;

/**
 * @author zyl
 * @date 2016年7月28日
 * 
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private WebSocketServerHandshaker handshaker;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		//传统的http接入
		if(msg instanceof FullHttpRequest){
			handleHttpRequest(ctx,(FullHttpRequest) msg);
		}else if(msg instanceof WebSocketFrame){
			//websocket 接入
			handleWebSocketFrame(ctx,(WebSocketFrame)msg);
		}
	}
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	private void handleHttpRequest(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception{
		//如果http解码失败，则返回http异常
		if(!request.getDecoderResult().isSuccess() || 
				(!"websocket".equals(request.headers().get("Upgrade")))){
			sendHttpResponse(ctx,request,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		//构造握手响应返回
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8989/websocket", null,false);
		handshaker = wsFactory.newHandshaker(request);
		if(handshaker == null){
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		}else {
			handshaker.handshake(ctx.channel(), request);
		}
	}
	private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
		//判断是否关闭链路指令
		if(frame instanceof CloseWebSocketFrame){
			handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
			return;
		}
		//判断是否是ping
		if(frame instanceof PingWebSocketFrame){
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		//本例程只支持文本，不支持二进制消息
		if(!(frame instanceof TextWebSocketFrame)){
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}
		//返回应答消息
		String request = ((TextWebSocketFrame)frame).text();
		System.out.println(ctx.channel() +"----"+request);
		ctx.channel().write(
				new TextWebSocketFrame(request+",欢迎使用netty websocket服务，现在时刻是："+new Date().toString()));
	}
	private static void sendHttpResponse(ChannelHandlerContext ctx,FullHttpRequest request,FullHttpResponse response){
		//返回应答给客户端
		if(response.getStatus().code() != 200){
			ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(),CharsetUtil.UTF_8);
			response.content().writeBytes(buf);
			buf.release();
			setContentLength(response,response.content().readableBytes());
		}
		//如果是非keep-alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(response);
		if(!isKeepAlive(request) || response.getStatus().code() != 200){
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	
	private static void setContentLength(HttpResponse response, long length) {
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, length);
	}

	private static boolean isKeepAlive(FullHttpRequest request) {
		String string = request.headers().get(HttpHeaders.Names.CONNECTION);
		if (null != string && HttpHeaders.Values.KEEP_ALIVE.equals(string)) {
			return true;
		}
		return false;
	}
}
