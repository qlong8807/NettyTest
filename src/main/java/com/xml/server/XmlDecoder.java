package com.xml.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class XmlDecoder extends ByteToMessageDecoder {
    private static Logger logger = LoggerFactory.getLogger(XmlDecoder.class);
    private byte[] lastBytes;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
//        System.err.println("enter decoder");
        int len = byteBuf.readableBytes();
        if (len < 4) return;
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        lastBytes = bytesAppend(lastBytes,bytes);
        processPackage(out);
    }

    private void processPackage(List<Object> out) {
        while (true) {
            String temp = new String(lastBytes, CharsetUtil.UTF_8);
            int parseLength = 0;
            int index = temp.indexOf("</Envelope>");//这里为XML结束标签
            if (index != -1) {
                while (StringUtils.isNotBlank(temp) && !temp.startsWith("<?xml")) {//判断下一条消息是否以<?xml开头，如果不是则移除一个字符串
                    String charFirst = temp.substring(0, 1);
                    logger.error("移除多余的字符：[{}],原字符串：[{}]",charFirst,temp);
                    parseLength = parseLength + charFirst.getBytes(CharsetUtil.UTF_8).length;
                    temp = temp.substring(1);
                }
                if (StringUtils.isNotBlank(temp)) {
                    index = temp.indexOf("</Envelope>");
                    String subTemp = temp.substring(0, index + 11);//11为结束标签所占字节位数
                    parseLength = parseLength + subTemp.getBytes(CharsetUtil.UTF_8).length;
                    if (subTemp.startsWith("<?xml") && subTemp.endsWith("</Envelope>")) {
                        out.add(subTemp);
                    }else{
                        logger.error("不是一个合法的XML：[{}].",subTemp);
                    }

                    byte[] tmp = new byte[lastBytes.length-parseLength];
                    System.arraycopy(lastBytes,parseLength,tmp,0,lastBytes.length-parseLength);
                    lastBytes = tmp;
                }else {
                    break;
                }
            }else{
                break;
            }
        }

    }

    private byte[] bytesAppend(byte[] b1, byte[] b2) {
        if (b1 == null || b1.length < 1)
            return b2;
        if (b2 == null || b2.length < 1)
            return b1;
        byte[] r = new byte[b1.length + b2.length];
        System.arraycopy(b1,0,r,0,b1.length);
        System.arraycopy(b2,0,r,b1.length,b2.length);
        return r;
    }
}
