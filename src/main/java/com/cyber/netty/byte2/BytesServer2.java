package com.cyber.netty.byte2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyber.netty.byte2.session.MutualSession;
import com.cyber.netty.byte2.session.MutualSessionManage;

/**
 * 使用消息头+消息体。解决了拆包/粘包问题。
 * @author zyl
 * @date 2016年9月2日
 * 
 */
public class BytesServer2 {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final int port = 8080;
	private volatile ServerBootstrap bootstrap;
	private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;

	public static void main(String[] args) {
		new BytesServer2().start();
	}
	public void start() {
		bootstrap = new ServerBootstrap();// 引导辅助程序
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		bootstrap.group(bossGroup,workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);// 设置nio类型的channel
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {// 有连接到达时会创建一个channel
			protected void initChannel(SocketChannel ch) throws Exception {
//					ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
				ch.pipeline().addLast(new MyByteEncoder2());
				ch.pipeline().addLast(new MyByteDecoder2());
				ch.pipeline().addLast(new MyServerHandler());
			}
		});
		doBind();
	}
	private void doBind(){
		try {
			bootstrap.bind(port).sync().addListener(new ChannelFutureListener() {
			    @Override
			    public void operationComplete(ChannelFuture f) throws Exception {
			        if (f.isSuccess()) {
			            System.out.println("Started Tcp Server: " + port);
			        } else {
			            System.out.println("Started Tcp Server Failed: " + port);
			            f.channel().eventLoop().schedule(new Runnable() {
							@Override
							public void run() {
								System.out.println("重新绑定端口。。。");
								doBind();
							}
						},1L,TimeUnit.SECONDS);
			        }
			    }
			});
		} catch (Exception e) {
			logger.info(e.getMessage());
			try {
				TimeUnit.SECONDS.sleep(3);
				doBind();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	private void close(){
		bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        System.out.println("Stopped Tcp Server: " + port);
	}

	private class MyServerHandler extends ChannelInboundHandlerAdapter {
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			Packet p = (Packet) msg;
			MessageProtocol2 mp = null;
			try {
				ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(p.getContent()));
				mp = (MessageProtocol2) oi.readObject();
				System.out.println("消息是："+mp.getContent().length());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			mp.setContent("这是服务端的回应");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oo = new ObjectOutputStream(baos);
				oo.writeObject(mp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] byteArray = baos.toByteArray();
			Packet rp = new Packet();
			rp.setVersion(1);
			rp.setCommand(2);
			rp.setUniqueMark(8888);
			rp.setSerialNumber(123);
			rp.setContent(byteArray);
			System.err.println("服务端发送："+rp.toString());
			System.err.println("服务端发送："+mp.toString());
			ctx.writeAndFlush(rp);
		}
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			int hashCode = ctx.channel().remoteAddress().hashCode();
			MutualSession mutualSession = new MutualSession();
			mutualSession.setId(ctx.hashCode());
			mutualSession.setChannel(ctx);
			mutualSession.setFirstConnectTime(System.currentTimeMillis()/1000);
			MutualSessionManage.getInstance().addMutualSession((long)hashCode, mutualSession);
		}

		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		cause.printStackTrace();// 捕捉异常信息
			System.out.println("服务端：客户端已关闭");
			ctx.close();// 出现异常时关闭channel
		}
	}
}
