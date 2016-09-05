/**
 * 
 */
package com.cyber.netty.string;

/**
 * @author zyl
 * @date 2016年7月28日
 * 
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

public class EchoClient {
	private final String host;
	private final int port;

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		new EchoClient("127.0.0.1", 8080).start();
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
					ch.pipeline().addLast(new EchoClientHandler());
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
	private class EchoClientHandler extends ChannelInboundHandlerAdapter {
		/**
		 * 此方法会在连接到服务器后被调用
		 * */
		public void channelActive(ChannelHandlerContext ctx) {
			// MessageProtocol mProtocol = new MessageProtocol();
			// mProtocol.setMessageType(2);
			// mProtocol.setContent("abc");
			// String objectToJson = JsonUtil.objectToJson(mProtocol);
			// objectToJson = objectToJson+"\r\n";
			// System.out.println(objectToJson);
			// ctx.writeAndFlush(Unpooled.copiedBuffer(objectToJson,
			// CharsetUtil.UTF_8));

			String s = "hello Netty";
			ctx.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));

			// 拆包问题
			/*
			 * String s = ""; for(int i=0;i<10000;i++){ s+="Netty Name"+i; }
			 * ctx.writeAndFlush(Unpooled.copiedBuffer(s+"\r\n",
			 * CharsetUtil.UTF_8));
			 */
			// 粘包问题
			/*
			 * for(int i=0;i<10000;i++){ String s = "hello netty"+i+"\r\n";
			 * ctx.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8)); }
			 */
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			System.out.println("---" + msg);
			ByteBuf in = (ByteBuf) msg;
			try {
				if (in.isReadable()) { // (1)
					String string = in.toString(CharsetUtil.UTF_8);
					System.out.println("client received data :" + string);
				}
			} finally {
				ReferenceCountUtil.release(msg); // (2)
			}
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
