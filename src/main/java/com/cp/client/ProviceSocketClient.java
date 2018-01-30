package com.cp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cp.client.coder.ProviceClientDecoder;
import com.cp.client.coder.ProviceClientEncoder;
import com.cp.client.handler.MyClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * socket 上传客户端客户端 调用socket上传客户端客户端，
 * 必须判断文件名集合和报文信息集合有数据才能调用，否则不能调用。
 * 
 * @author Administrator
 *
 */
public class ProviceSocketClient {

	private Logger logger = LoggerFactory.getLogger(ProviceSocketClient.class);

	private volatile Bootstrap bootstrap;
	private volatile EventLoopGroup workerGroup;
    private final String remoteHost;
    private final int remotePort;

	public ProviceSocketClient(String host, int port) {
		this.remoteHost = host;
		this.remotePort = port;
	}

	public static void main(String[] args) throws Exception {
//		String inst_code = "02017910";
		String inst_code = "12088010";
		String localFilePath = "/Users/apple/Documents/test/frontSocket";
//		new ProviceSocketClient("192.168.0.216", 18088).start();11078060
		new ProviceSocketClient("124.114.131.58", 20011).start(inst_code,localFilePath);
//		new ProviceSocketClient("127.0.0.1", 18088).start(inst_code,localFilePath);
	}

	public void start(final String code,final String path){
		bootstrap = new Bootstrap();
		workerGroup = new NioEventLoopGroup();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ProviceClientDecoder());
				ch.pipeline().addLast(new ProviceClientEncoder());
				ch.pipeline().addLast(new MyClientHandler(code,path));
			}
		});
		doConnect();
	}
	private void doConnect() {
        ChannelFuture future = null;
		try {
			future = bootstrap.connect(remoteHost, remotePort).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			logger.error("客户端连接socket服务器报错：" + e);
			e.printStackTrace();
		} finally {
			if (null != workerGroup) {
				try {
					// 关闭引导器并释放资源，包括线程池
					workerGroup.shutdownGracefully().sync();
				} catch (InterruptedException e) {
					logger.error("客户端关闭socket报错："+e);
					e.printStackTrace();
				}
			}
		}
    }
	public void close() {
        workerGroup.shutdownGracefully();
        logger.info("Stopped Tcp Client: " + getServerInfo());
    }
	private String getServerInfo() {
        return String.format("RemoteHost=%s RemotePort=%d",
                remoteHost,
                remotePort);
    }
	
}
