package com.xml.parse;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.xml.server.entity.HeartBeatEnvelope;
import com.xml.server.entity.XmlUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class Dom4jTest {
    public static void main(String[] args) {
        String xml = generateXmlString();
        parseXml(xml);
        System.out.println(XmlUtil.fromXML(xml, HeartBeatEnvelope.class));
    }
    public static String generateXmlString(){
        Document doc = DocumentHelper.createDocument();
        Element rootEle = doc.addElement("Envelope");
        rootEle.addAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        rootEle.addAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema");
        Element header = rootEle.addElement("Header");
//        header.addAttribute("name", "iOS");
        Element msgName = header.addElement("MessageName");
        msgName.setText("swift");
        Element msgTime = header.addElement("MessageTime");
        msgTime.setText(DateUtil.date().toString(DatePattern.NORM_DATETIME_FORMAT));

        Element body = rootEle.addElement("Body");
//        body.addAttribute("name", "Android");
        Element source = body.addElement("Source");
        source.setText("java");
        Element target = body.addElement("Target");
        target.setText("google");

//        OutputFormat format = OutputFormat.createCompactFormat();
        OutputFormat format = OutputFormat.createPrettyPrint();

//        format.setNewlines(true);
//        format.setIndent("yes");
        format.setEncoding("UTF-8");
        StringWriter writer = new StringWriter();
        XMLWriter output = new XMLWriter(writer, format);
        try {
            output.write(doc);
            writer.close();
            output.close();
            System.out.println(writer.toString());
        }  catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }
    public static void parseXml(String xmlString){
        try {
            Document document = DocumentHelper.parseText(xmlString);
            Element root = document.getRootElement();
            Element msgName = root.element("Header").element("MessageName");
            System.err.println(msgName.getStringValue());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
