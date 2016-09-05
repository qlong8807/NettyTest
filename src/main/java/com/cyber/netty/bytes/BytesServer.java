package com.cyber.netty.bytes;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * 使用消息头+消息体。解决了拆包/粘包问题。
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class BytesServer {
	private static final int port = 8080;

	public static void main(String[] args) {
		try {
			new BytesServer().start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void start() throws InterruptedException {
		ServerBootstrap b = new ServerBootstrap();// 引导辅助程序
		EventLoopGroup group = new NioEventLoopGroup();// 通过nio方式来接收连接和处理连接
		try {
			b.group(group);
			b.channel(NioServerSocketChannel.class);// 设置nio类型的channel
			b.localAddress(new InetSocketAddress(port));// 设置监听端口
			b.childHandler(new ChannelInitializer<SocketChannel>() {// 有连接到达时会创建一个channel
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
					ch.pipeline().addLast(new MyByteEncoder());
					ch.pipeline().addLast(new MyByteDecoder());
					ch.pipeline().addLast("myHandler", new MyServerHandler());
				}
			});
			ChannelFuture f = b.bind().sync();// 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
			System.out.println(BytesServer.class.getName()
					+ " started and listen on " + f.channel().localAddress());
			f.channel().closeFuture().sync();// 应用程序会一直等待，直到channel关闭
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully().sync();// 关闭EventLoopGroup，释放掉所有资源包括创建的线程
		}
	}

	private class MyServerHandler extends ChannelInboundHandlerAdapter {
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
//			String s = (String) msg;
//			System.out.println("server:"+s);
//			ctx.writeAndFlush("i received..");
			MessageProtocol mp = (MessageProtocol) msg;
			System.out.println(mp.toString());
			ctx.writeAndFlush(mp);
		}
		
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		cause.printStackTrace();// 捕捉异常信息
			System.out.println("服务端：客户端已关闭");
			ctx.close();// 出现异常时关闭channel
		}
	}
}
