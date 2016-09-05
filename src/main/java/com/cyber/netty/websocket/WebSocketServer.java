/**
 * 
 */
package com.cyber.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author zyl
 * @date 2016年7月22日
 * 
 */
public class WebSocketServer {

	public void run(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel arg0)
								throws Exception {
							arg0.pipeline().addLast("http-codec",
									new HttpServerCodec());
							arg0.pipeline().addLast("http-aggregator",
									new HttpObjectAggregator(65535));
							arg0.pipeline().addLast("http-chunk",
									new ChunkedWriteHandler());
							arg0.pipeline().addLast("http-file-handler",
									new WebSocketServerHandler());
						}
					});
			System.out.println("请打开浏览器访问：http://localhost:" + port);
			b.bind(port).sync().channel().closeFuture().sync();
			System.out.println("=======");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully().sync();
			workerGroup.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) {
		try {
			new WebSocketServer().run(8989);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
