/**
 * 
 */
package com.cyber.netty.protobuf.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.cyber.netty.protobuf.bean.VrMessageProto;
import com.cyber.netty.protobuf.bean.VrMessageProto.VrMessage;

/**
 * @author zyl
 * @date 2016年9月1日
 * 
 */
public class ProtoServer {
	public static void main(String[] args) {
		try {
			new ProtoServer().bind(9999);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void bind(int port) throws Exception{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap bootstrap = new ServerBootstrap();
		try {
			bootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 100)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
						ch.pipeline().addLast(new ProtobufDecoder(VrMessageProto.VrMessage.getDefaultInstance()));
						ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
						ch.pipeline().addLast(new ProtobufEncoder());
						ch.pipeline().addLast(new MyServerHandler());
					}
				});
			ChannelFuture f = bootstrap.bind(port).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
class MyServerHandler extends ChannelInboundHandlerAdapter{
	int count = 0;
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		count++;
		System.out.println("服务端：收到一条消息");
		VrMessageProto.VrMessage message = (VrMessage) msg;
		String content = message.getContent();
		System.out.println(content.length()+"---count:"+count);
		
		VrMessage.Builder vm = VrMessage.newBuilder();
		vm.setContent("server received cli");
		ctx.writeAndFlush(vm);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
//		cause.printStackTrace();
		System.out.println("远程主机强迫关闭了一个现有的连接。");
		ctx.close();//发生异常，关闭链路
	}
}