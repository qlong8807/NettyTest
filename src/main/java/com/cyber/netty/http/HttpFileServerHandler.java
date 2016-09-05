/**
 * 
 */
package com.cyber.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

/**
 * @author zyl
 * @date 2016年7月22日
 * 
 */
public class HttpFileServerHandler extends
		SimpleChannelInboundHandler<FullHttpRequest> {
	private String url;
	private static final Pattern PATTERN_URL = Pattern.compile(".*[<>&\"].*");
	private static final Pattern ALLOWED_FILE_NAME = Pattern
			.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

	public HttpFileServerHandler(String url) {
		this.url = url;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext context,
			FullHttpRequest request) throws Exception {
		if (!request.getDecoderResult().isSuccess()) {
			sendError(context, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		if (request.getMethod() != HttpMethod.GET) {
			sendError(context, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		String uri = request.getUri();
		System.out.println("uri:"+uri);
		String path = sanitizeUri(uri);
		if (null == path) {
			sendError(context, HttpResponseStatus.FORBIDDEN);
			return;
		}
		File file = new File(path);
		if (file.isHidden() || !file.exists()) {
			sendError(context, HttpResponseStatus.NOT_FOUND);
			return;
		}
		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				sendList(context, file);
			} else {
				sendRedirect(context, uri + '/');
			}
			return;
		}
		if (!file.isFile()) {
			sendError(context, HttpResponseStatus.FORBIDDEN);
			return;
		}
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");// 以只读的方式打开文件
		} catch (FileNotFoundException e) {
			sendError(context, HttpResponseStatus.NOT_FOUND);
			return;
		}
		long length = randomAccessFile.length();
		HttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		setContentLength(response, length);
		setContentTypeHeader(response, file);
		if (isKeepAlive(request)) {
			response.headers().set(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
		}
		context.write(response);
		ChannelFuture sendFuture = context.write(new ChunkedFile(
				randomAccessFile, 0, length, 8192), context
				.newProgressivePromise());
		sendFuture.addListener(new ChannelProgressiveFutureListener() {

			@Override
			public void operationComplete(ChannelProgressiveFuture future)
					throws Exception {
				System.out.println("Transfer completer...");
			}

			@Override
			public void operationProgressed(ChannelProgressiveFuture future,
					long progress, long total) throws Exception {
				if (total < 0) {
					System.err.println("Transfer progress:" + progress);
				} else {
					System.err.println("Transfer progress:" + progress + "/"
							+ total);
				}
			}
		});
		ChannelFuture lastFuture = context
				.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!isKeepAlive(request)) {
			lastFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private boolean isKeepAlive(FullHttpRequest request) {
		String string = request.headers().get(HttpHeaders.Names.CONNECTION);
		if (null != string && HttpHeaders.Values.KEEP_ALIVE.equals(string)) {
			return true;
		}
		return false;
	}

	private String sanitizeUri(String uri) {
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				throw new Error();
			}
		}
		if (!uri.startsWith(url))
			return null;
		if (!uri.startsWith("/"))
			return null;
		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.')
				|| uri.contains('.' + File.separator) || uri.startsWith(".")
				|| uri.endsWith(".") || PATTERN_URL.matcher(uri).matches()) {
			return null;
		}
		return System.getProperty("user.dir") + File.separator + uri;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		System.out.println("complete................");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (ctx.channel().isActive()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static void sendList(ChannelHandlerContext context, File dir) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				"text/html;charset=UTF-8");
		StringBuilder builder = new StringBuilder();
		String dirPath = dir.getPath();
		builder.append("<!DOCTYPE html>\r\n");
		builder.append("<html><head><title>");
		builder.append(dirPath);
		builder.append("目录：");
		builder.append("</title></head><body>\r\n");
		builder.append("<h3>");
		builder.append(dirPath).append("目录:");
		builder.append("</h3>\r\n");
		builder.append("<ul>");
		builder.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		for (File f : dir.listFiles()) {
			if (f.isHidden() || !f.canRead()) {
				continue;
			}
			String name = f.getName();
			if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
				continue;
			}
			builder.append("<li>链接:<a href=\"");
			builder.append(name);
			builder.append("\">");
			builder.append(name);
			builder.append("</a></li>\r\n");
		}
		builder.append("</ul></body></html>\r\n");
		ByteBuf buffer = Unpooled.copiedBuffer(builder, CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		context.writeAndFlush(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	private static void sendRedirect(ChannelHandlerContext context, String uri) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
		response.headers().set(HttpHeaders.Names.LOCATION, uri);
		context.writeAndFlush(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	private static void sendError(ChannelHandlerContext context,
			HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure:"
						+ status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				"text/plain;charset=UTF-8");
		context.writeAndFlush(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mime = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				mime.getContentType(file.getPath()));
	}

	private static void setContentLength(HttpResponse response, long length) {
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, length);
	}
}
