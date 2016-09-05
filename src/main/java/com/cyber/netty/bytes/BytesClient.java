/**
 * 
 */
package com.cyber.netty.bytes;

/**
 * @author zyl
 * @date 2016年7月28日
 * 
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class BytesClient {
	private final String host;
	private final int port;

	public BytesClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		new BytesClient("127.0.0.1", 8080).start();
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.remoteAddress(new InetSocketAddress(host, port));
			b.handler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new MyByteEncoder());
					ch.pipeline().addLast(new MyByteDecoder());
					ch.pipeline().addLast(new MyClientHandler());
				}
			});
			ChannelFuture f = b.connect().sync();
			f.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future)
						throws Exception {
					if (future.isSuccess()) {
						System.out.println("client connected");
					} else {
						System.out.println("server attemp failed");
						future.cause().printStackTrace();
					}
				}
			});
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully().sync();
		}
	}

	@Sharable
	private class MyClientHandler extends ChannelInboundHandlerAdapter {
		/**
		 * 此方法会在连接到服务器后被调用
		 * */
		public void channelActive(ChannelHandlerContext ctx) {
			MessageProtocol mProtocol = new MessageProtocol();
			mProtocol.setMessageType(2);
			String s = "hello netty"; 
//			for(int i=0;i<10000;i++){ s+="Netty Name"+i; }
			mProtocol.setContent(s);
			ctx.writeAndFlush(mProtocol);
			

			// String s = "hello Netty";
			// ctx.writeAndFlush(s);

		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
//			String s = (String) msg;
//			System.out.println("server:" + s);
			MessageProtocol mp = (MessageProtocol) msg;
			System.out.println(mp.toString());
//			ctx.writeAndFlush(mp);
		}

		/**
		 * 捕捉到异常
		 * */
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			// cause.printStackTrace();
			System.out.println("客户端：服务端已关闭");
			ctx.close();
		}
	}
}
