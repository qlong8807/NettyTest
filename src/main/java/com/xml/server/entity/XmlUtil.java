package com.xml.server.entity;

import ch.qos.logback.core.encoder.EncoderBase;
import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import sun.applet.Main;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        header.setMessageTime(new Date().toString());
        HeartBeatBody body = new HeartBeatBody();
        body.setSource("SIS");
        body.setTarget("SMS");
        HeartBeatEnvelope envelope = new HeartBeatEnvelope();
        envelope.setHeader(header);
        envelope.setBody(body);
        String xmlString = XmlUtil.toXML(envelope);
        System.out.println(xmlString);
        xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <Header>\n" +
                "        <MessageName>心跳11</MessageName>\n" +
                "        <MessageTime>2019-09-08T13:43:58.500+08:00</MessageTime>\n" +
                "    </Header>\n" +
                "    <Body>\n" +
                "        <Source>SIS1</Source>\n" +
                "        <Target>SMS1</Target>\n" +
                "    </Body>\n" +
                "</Envelope>";
        System.out.println(XmlUtil.fromXML(xmlString,HeartBeatEnvelope.class));


//        Map map = new HashMap();
//        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//        map.put("xsd", "http://www.w3.org/2001/XMLSchema");
//        SAXReader saxReader = new SAXReader(new DocumentFactory());
//        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
//        SAXWriter writer = new SAXWriter();
    }
}