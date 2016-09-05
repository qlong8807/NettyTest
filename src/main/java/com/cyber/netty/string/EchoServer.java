package com.cyber.netty.string;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * 未解决粘包拆包问题
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class EchoServer {
	private static final int port = 8080;

	public static void main(String[] args) {
		try {
			new EchoServer().start();
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
					// pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
					ch.pipeline().addLast("myHandler", new EchoServerHandler());
				}
			});
			ChannelFuture f = b.bind().sync();// 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
			System.out.println(EchoServer.class.getName()
					+ " started and listen on " + f.channel().localAddress());
			f.channel().closeFuture().sync();// 应用程序会一直等待，直到channel关闭
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully().sync();// 关闭EventLoopGroup，释放掉所有资源包括创建的线程
		}
	}

}
// 注解@Sharable可以让它在channels间共享
//@Sharable
class EchoServerHandler extends ChannelInboundHandlerAdapter {
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;
		try {
			if (in.isReadable()) { // (1)
				String string = in.toString(Charset.forName("UTF-8"));
				System.out.println("server received data :" + string);
			}
		} finally {
			ReferenceCountUtil.release(msg); // (2)
		}
		//写数据到客户端
		
		ctx.writeAndFlush(Unpooled.copiedBuffer("i received U",Charset.forName("UTF-8")));
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		cause.printStackTrace();// 捕捉异常信息
		System.out.println("服务端：客户端已关闭");
		ctx.close();// 出现异常时关闭channel
	}
}
