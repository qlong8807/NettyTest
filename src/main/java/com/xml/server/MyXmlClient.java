/**
 * 
 */
package com.xml.server;

import com.cyber.netty.string.EchoLineClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @author zyl
 * @date 2016年7月28日
 */

public class MyXmlClient {

	private final String host;
	private final int port;

	public MyXmlClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		new MyXmlClient("127.0.0.1", 8088).start("tongfang");
	}

	public void start(String name) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.remoteAddress(new InetSocketAddress(host, port));
			b.handler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
					// 加了StringEncoder编码器就可以直接发送String，要不还得转换为ByteBuffer
					ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
					ch.pipeline().addLast(new MyXmlClientHandler(name));
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
		} catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				group.shutdownGracefully().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Sharable
	private class MyXmlClientHandler extends ChannelInboundHandlerAdapter {
		private String ctxName;
		public MyXmlClientHandler(String name) {
			this.ctxName = name;
		}
		/**
		 * 此方法会在连接到服务器后被调用
		 * */
		public void channelActive(ChannelHandlerContext ctx) {
			MyCtxCache.setCtx(ctxName,ctx);
			String s = "1hello Netty\r\n";
			ctx.writeAndFlush(s);
			System.out.println("sended");
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
			if ("bye".equals(msg.toString())) {
				throw new NullPointerException("收到结束异常");
			}
			MyCtxCache.getCtx(ctxName).writeAndFlush("我收到了："+msg);

		}

		/**
		 * 捕捉到异常
		 * */
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			// cause.printStackTrace();

			System.out.println("异常啦===============================："+cause.getMessage());
			System.out.println("我要重连");
			new MyXmlClient("127.0.0.1", 8088).start("tongfang");
			ctx.close();

//			System.out.println("客户端：服务端已关闭:"+cause.getMessage());
//			ctx.close();
		}
	}
}
