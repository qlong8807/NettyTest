package com.reconnect.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ReconnectClient {
    private EventLoopGroup loop = new NioEventLoopGroup();

    public static void main(String[] args) {
        new ReconnectClient().run();
    }

    public void createBootstrap(EventLoopGroup eventLoop) {
        Bootstrap bootstrap = new Bootstrap();
        final MyInboundHandler handler = new MyInboundHandler(this);
        bootstrap.group(eventLoop);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(handler);
                //在Netty中提供了一个IdleStateHandler类用于心跳检测,
                // 在处理数据的handler中增加userEventTriggered用来接收心跳检测结果,event.state()的状态分别对应上面三个参数的时间设置，当满足某个时间的条件时会触发事件。
                ch.pipeline().addLast("ping", new IdleStateHandler(60, 20, 60 * 10, TimeUnit.SECONDS));
            }
        });
        bootstrap.remoteAddress("localhost", 10001);
        bootstrap.connect().addListener(new ConnectionListener(this));
    }

    public void run() {
        createBootstrap(loop);
    }
}

/**
 * 实现ChannelFutureListener 用来启动时监测是否连接成功，不成功的话重试：
 */
class ConnectionListener implements ChannelFutureListener {
    private ReconnectClient client;

    ConnectionListener(ReconnectClient client) {
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            System.out.println("Reconnect");
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    client.createBootstrap(loop);
                }
            }, 1L, TimeUnit.SECONDS);
        } else {
            System.err.println("连接成功");
        }
    }
}

/**
 * 同样在ChannelHandler监测连接是否断掉，断掉的话也要重连：
 */
class MyInboundHandler extends ChannelInboundHandlerAdapter {
    private ReconnectClient client;

    MyInboundHandler(ReconnectClient client) {
        this.client = client;
    }

    /**
     * 配合上面的addLast使用，指定3种时间
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx,evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                System.out.println("长期没收到服务器推送数据");
                //可以选择重新连接
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                System.out.println("长期未向服务器发送数据");
                //发送心跳包
                ctx.writeAndFlush("");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                System.out.println("长期没有读写操作");
            }
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                System.err.println("inactive reconnect");
                client.createBootstrap(eventLoop);
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }
}