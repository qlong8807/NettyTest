package com.cp.server;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.server.coder.ProviceServerDecoder;
import com.cp.server.coder.ProviceServerEncoder;
import com.cp.server.handler.ProviceHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * @author zyl
 * @date 2017年11月29日
 * @desc
 */
public class ProviceServer {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int port;
	private volatile ServerBootstrap bootstrap;
	private volatile EventLoopGroup bossGroup;
	private volatile EventLoopGroup workerGroup;

	public void start(int port, final String data_dir) {
		this.port = port;
		bootstrap = new ServerBootstrap();// 引导辅助程序
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);// 设置nio类型的channel
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {// 有连接到达时会创建一个channel
			protected void initChannel(SocketChannel ch) throws Exception {
				// ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
				ch.pipeline().addLast(new ProviceServerEncoder());
				ch.pipeline().addLast(new ProviceServerDecoder());
				ch.pipeline().addLast(new ProviceHandler(data_dir));
				ch.pipeline().addLast(new ReadTimeoutHandler(30));//30秒没有读写完成则关闭链路
				ch.pipeline().addLast(new WriteTimeoutHandler(30));
			}
		});
		doBind();
	}

	private void doBind() {
		try {
			bootstrap.bind(port).sync().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess()) {
						logger.info("Started Tcp Server: " + port);
					} else {
						logger.error("Started Tcp Server Failed: " + port);
						f.channel().eventLoop().schedule(new Runnable() {
							@Override
							public void run() {
								logger.error("绑定端口<{}>失败，正在重新绑定端口。。。", port);
								doBind();
							}
						}, 1L, TimeUnit.SECONDS);
					}
				}
			});
		} catch (Exception e) {
			logger.error("绑定端口<{}>失败，正在重新绑定端口，信息：{}", port, e.getMessage());
			try {
				TimeUnit.SECONDS.sleep(3);
				doBind();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void close() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		logger.info("Stopped Tcp Server: " + port);
	}
}
