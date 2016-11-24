/**
 * 
 */
package com.cyber.netty.http2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyber.netty.http2.annotation.AnnotationUtil;

/**
 * @author zyl
 * @date 2016年7月22日
 * 
 */
public class HttpServer2 {
	private static Logger logger = LoggerFactory.getLogger(HttpServer2.class);

	public void run(final String webName, int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.localAddress(new InetSocketAddress(port))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel arg0)
								throws Exception {
							arg0.pipeline().addLast("http-decoder",
									new HttpRequestDecoder());
							arg0.pipeline().addLast("http-encoder",
									new HttpResponseEncoder());
							arg0.pipeline().addLast("http-aggregator",
									new HttpObjectAggregator(65535));
							arg0.pipeline().addLast("http-chunk",
									new ChunkedWriteHandler());
							arg0.pipeline().addLast("http-file-handler",
									new HttpServerHandler(webName));
						}
					});
			ChannelFuture future = b.bind().sync();
			InetAddress addr = InetAddress.getLocalHost();
			String ip=addr.getHostAddress().toString();
			logger.info("服务器启动，地址是：http://"+ip+":"+port+"/"+webName);
			future.channel().closeFuture().sync();
		}catch(Exception e){ 
			e.printStackTrace();
		}finally {
			bossGroup.shutdownGracefully().sync();
			workerGroup.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) {
		try {
			AnnotationUtil.initMap("com.cyber.netty.http2");
			new HttpServer2().run("src", 8989);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
