package com.cyber.netty.byte2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.cyber.netty.byte2.session.MutualSession;
import com.cyber.netty.byte2.session.MutualSessionManage;
import com.cyber.netty.bytes.Convert;

public class BytesClient2 {
	private volatile Bootstrap bootstrap;
	private volatile EventLoopGroup workerGroup;
    private final String remoteHost;
    private final int remotePort;

	public BytesClient2(String host, int port) {
		this.remoteHost = host;
		this.remotePort = port;
	}

	public static void main(String[] args) throws Exception {
		new BytesClient2("127.0.0.1", 8080).start();
	}

	public void start(){
		bootstrap = new Bootstrap();
		workerGroup = new NioEventLoopGroup();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addFirst(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        super.channelInactive(ctx);
                        ctx.channel().eventLoop().schedule(new Runnable() {
							@Override
							public void run() {
								System.out.println("重新连接1。。。");
								doConnect();
							}
						},1L,TimeUnit.SECONDS);
                    }
                });
				ch.pipeline().addLast(new MyByteEncoder2());
				ch.pipeline().addLast(new MyByteDecoder2());
				ch.pipeline().addLast(new MyClientHandler());
			}
		});
		doConnect();
	}
	private void doConnect() {
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(remoteHost, remotePort));
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    System.out.println("Started Tcp Client: " + getServerInfo());
                } else {
                    System.out.println("Started Tcp Client Failed: " + getServerInfo());
                    f.channel().eventLoop().schedule(new Runnable() {
						@Override
						public void run() {
							System.out.println("重新连接2。。。");
							doConnect();
						}
					},1L,TimeUnit.SECONDS);
                }
            }
        });
    }
	public void close() {
        workerGroup.shutdownGracefully();
        System.out.println("Stopped Tcp Client: " + getServerInfo());
    }
	private String getServerInfo() {
        return String.format("RemoteHost=%s RemotePort=%d",
                remoteHost,
                remotePort);
    }
	
	
	@Sharable
	private class MyClientHandler extends ChannelInboundHandlerAdapter {

		/**
		 * 此方法会在连接到服务器后被调用
		 * 
		 * @throws Exception
		 * */
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			int hashCode = ctx.channel().remoteAddress().hashCode();
			MutualSession mutualSession = new MutualSession();
			mutualSession.setId(ctx.hashCode());
			mutualSession.setChannel(ctx);
			mutualSession
					.setFirstConnectTime(System.currentTimeMillis() / 1000);
			MutualSessionManage.getInstance().addMutualSession((long) hashCode,
					mutualSession);

			MessageProtocol2 mp = new MessageProtocol2();
			mp.setMessageType(2);
			String sss = "";
			for (int i = 0; i < 1000; i++) {
				sss += "这是客户端的消息" + i;
			}
			mp.setContent(sss);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(baos);
			oo.writeObject(mp);
			byte[] byteArray = baos.toByteArray();
			Packet rp = new Packet();
			rp.setVersion(1);
			rp.setCommand(5);
			rp.setUniqueMark(10001);
			rp.setSerialNumber(5555);
			rp.setContent(byteArray);
			System.err.println("客户端发送byte:"
					+ Convert.bytesToHexString(rp.getContent()));
			ctx.writeAndFlush(rp);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			System.out.println("客户端：服务端已关闭");
//			super.channelInactive(ctx);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			Packet p = (Packet) msg;
			System.err.println("客户端收到server:" + p.toString());
			ObjectInputStream oi = new ObjectInputStream(
					new ByteArrayInputStream(p.getContent()));
			MessageProtocol2 mp = (MessageProtocol2) oi.readObject();
			System.out.println("server mp:" + mp.toString());
		}

		/**
		 * 捕捉到异常
		 * */
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			// cause.printStackTrace();
			System.out.println("客户端：发生异常:" + cause.getMessage());
//			ctx.close();
		}
	}
}
