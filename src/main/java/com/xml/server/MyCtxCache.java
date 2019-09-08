package com.xml.server;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyCtxCache {
    private static Map<String, ChannelHandlerContext> ctxs = new ConcurrentHashMap<>();

    public static ChannelHandlerContext getCtx(String name) {
        return ctxs.get(name);
    }

    public static void setCtx(String name, ChannelHandlerContext ctx) {
        ctxs.put(name, ctx);
    }
}
