/**
 * 
 */
package com.cyber.netty.protobuf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;

import com.cyber.netty.protobuf.bean.VrMessageProto;
import com.cyber.netty.protobuf.bean.VrMessageProto.VrMessage;

/**
 * @author zyl
 * @date 2016年9月1日
 * 
 */
public class ProtoClient {
	public static void main(String[] args) {
		try {
			new ProtoClient().connect("127.0.0.1", 9999);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void connect(String host,int port) throws Exception{
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.remoteAddress(new InetSocketAddress(host, port));
			b.handler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(VrMessageProto.VrMessage.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
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
}
class MyClientHandler extends ChannelInboundHandlerAdapter {
	/**
	 * 此方法会在连接到服务器后被调用
	 * */
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("client send...");
		VrMessage.Builder vm = VrMessage.newBuilder();
		String s = "";
		for(int i=0;i<10000;i++){
			s += "iamclient-"+i+"=";
			vm.setContent(s);
			ctx.writeAndFlush(vm.build());
		}
		System.out.println("client sended");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		VrMessageProto.VrMessage message = (VrMessage) msg;
		String content = message.getContent();
		System.out.println(content);
	}

	/**
	 * 捕捉到异常
	 * */
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		cause.printStackTrace();
		System.out.println("客户端：服务端链路已关闭");
		ctx.close();
	}
}