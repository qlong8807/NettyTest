package com.xml.server.entity;

import ch.qos.logback.core.encoder.EncoderBase;
import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import sun.applet.Main;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

public class XmlUtil {

    public static String toXML(Object obj) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// //编码格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// 是否格式化生成的xml串
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);// 是否省略xm头声明信息

//            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
//                @Override
//                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//                    if (namespaceUri.equals("http://www.lzrabbit.cn")) return "abc";
//                    if (namespaceUri.contains("http://www.cnblogs.com")) return "blog";
//                    return suggestion;
//                }
//            });

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromXML(String xml, Class<T> valueType) {
        try {
            JAXBContext context = JAXBContext.newInstance(valueType);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public static void main(String[] args) {
        HeartBeatHeader header = new HeartBeatHeader();
        header.setMessageName("心跳");
        header.setMessageTime(new Date());
        HeartBeatBody body = new HeartBeatBody();
        body.setSource("SIS");
        body.setTarget("SMS");
        HeartBeatEnvelope envelope = new HeartBeatEnvelope();
        envelope.setHeader(header);
        envelope.setBody(body);
        String xmlString = XmlUtil.toXML(envelope);
        System.out.println(xmlString);
        System.out.println(XmlUtil.fromXML(xmlString,HeartBeatEnvelope.class));
    }
}