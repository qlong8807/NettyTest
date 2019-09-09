/**
 *
 */
package com.xml.server;

import com.cyber.netty.string.EchoLineClient;
import com.xml.client.MyXmlClient;
import com.xml.parse.Dom4jTest;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author zyl
 * @date 2016年7月28日
 */

public class MyXmlServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int port = 8088;
    private volatile ServerBootstrap bootstrap;
    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        new MyXmlServer().start("tongfang");
    }

    public void start(String name) {
        bootstrap = new ServerBootstrap();// 引导辅助程序
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);// 设置nio类型的channel
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {// 有连接到达时会创建一个channel
            protected void initChannel(SocketChannel ch) throws Exception {
//					ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                // 加了StringEncoder编码器就可以直接发送String，要不还得转换为ByteBuffer
                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                ch.pipeline().addLast(new XmlDecoder());
                ch.pipeline().addLast(new MyXMLServerHandler(name));
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
                        System.out.println("Started Tcp Server: " + port);
                    } else {
                        System.out.println("Started Tcp Server Failed: " + port);
                        f.channel().eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("重新绑定端口。。。");
                                doBind();
                            }
                        }, 1L, TimeUnit.SECONDS);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("",e);
            try {
                TimeUnit.SECONDS.sleep(3);
                doBind();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Sharable
    private class MyXMLServerHandler extends ChannelInboundHandlerAdapter {
        private String ctxName;

        public MyXMLServerHandler(String name) {
            System.out.println("handler init");
            this.ctxName = name;
        }

        /**
         * 此方法会在连接到服务器后被调用
         * */
        public void channelActive(ChannelHandlerContext ctx) {
//            MyCtxCache.setCtx(ctxName, ctx);
//            String s = Dom4jTest.generateXmlString();
//            ctx.writeAndFlush(s);
//            System.out.println("sended");

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
                throws Exception {
            System.out.println("------------收到-------------");
            System.out.println(msg.toString());
            System.err.println("=========================");
            if ("bye".equals(msg.toString())) {
                throw new NullPointerException("收到结束异常");
            }
//			MyCtxCache.getCtx(ctxName).writeAndFlush("我收到了："+msg);
            ctx.writeAndFlush(Unpooled.copiedBuffer("14:13:10 收到数据：<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><Header><MessageName>swift</MessageName><MessageTime>2019-09-09 14:13:10</MessageTime></Header><Body><Source>java</Source><Target>google</Target></Body></Envelope>", CharsetUtil.UTF_8));
        }

        /**
         * 捕捉到异常
         * */
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();

            System.out.println("异常啦===============================：" + cause.getMessage());
            System.out.println("我要重连");
            new MyXmlClient("127.0.0.1", 8088).start("tongfang");
            ctx.close();

//			System.out.println("客户端：服务端已关闭:"+cause.getMessage());
//			ctx.close();
        }
    }
}
