package com.reconnect.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(handler);
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