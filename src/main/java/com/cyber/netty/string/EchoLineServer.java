/**
 * 
 */
package com.cyber.netty.string;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * 行解码器
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class EchoLineServer {
	private static final int port = 8080;

	public static void main(String[] args) {
		try {
			new EchoLineServer().start();
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
					ch.pipeline().addLast(new LineBasedFrameDecoder(1024*1024));
					ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
					ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
					ch.pipeline().addLast("myHandler", new EchoServerHandler2());
				}
			});
			ChannelFuture f = b.bind().sync();// 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
			System.out.println(EchoLineServer.class.getName()
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
class EchoServerHandler2 extends ChannelInboundHandlerAdapter {
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		String mm = (String) msg;
		System.out.println("server receive:"+mm);
		ctx.writeAndFlush(mm+"\r\n");
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		cause.printStackTrace();// 捕捉异常信息
		System.out.println("服务端：客户端已关闭");
		ctx.close();// 出现异常时关闭channel
	}
}
