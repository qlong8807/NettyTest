package com.xml.parse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

public class DomTest {
    public static void main(String[] args) {
        try {
            //创建一个 DocumentBuilderFactory 对象
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //创建DocumentBuilder对象
            DocumentBuilder db = dbf.newDocumentBuilder();
            //创建 Document 对象
            Document document = db.newDocument();
//            document.createElementNS("xsd","http://www.w3.org/2001/XMLSchema");
//            document.createElementNS("xsi","http://www.w3.org/2001/XMLSchema-instance");
            //隐藏 XML文件 standalone 属性
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            //创建根节点
            Element school = document.createElement("school");
            school.setAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema");
            school.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
            //创建子节点
            Element student = document.createElement("student");
            //创建student的子节点
            Element name = document.createElement("name");
            Element age = document.createElement("age");
            Element address = document.createElement("address");

            //给 name 节点添加 文本内容
            name.setTextContent("小明");
            age.setTextContent("18");
            address.setTextContent("北京市海定区五道口");

            //把子节点 添加到 student 节点下
            student.appendChild(name);
            student.appendChild(age);
            student.appendChild(address);

            //向 studet 节点添加属性和属性值
            student.setAttribute("id", "1");
            //向 school 添加子节点
            school.appendChild(student);
            //将 根节点(已经包含子节点    )添加到dom树中
            document.appendChild(school);

            //..将 dom树转换为 XML文件
            //创建 TransformerFactory 对象
            TransformerFactory tff = TransformerFactory.newInstance();
            //创建 Transformer 对象
            Transformer tf = tff.newTransformer();
            //设置生成的 XML 文件自动换行
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("encoding", "utf-8");
            //使用 Transformer 的 transform 方法把Dom树转换成  XML 文件
//            tf.transform(new DOMSource(document), new StreamResult(new File("School_01.xml")));
            StringWriter writerStr = new StringWriter();
            Result resultStr = new StreamResult(writerStr);
            tf.transform(new DOMSource(document), resultStr);
            System.out.println(writerStr.getBuffer().toString());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void parseXml(){
        //(1)建立DocumentBuilderFactory，以用取得DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //(2)建立DocumentBuilderFactory取得DocumentBuilder
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        //(3)定义Document接口对象，通过DocumentBuilder类进行DOM树的转换操作
        Document doc = null;
        try {
            doc = builder.parse("src/dom.xml");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList nodeList = doc.getElementsByTagName("linkman");
        for(int i = 0; i < nodeList.getLength(); i++) {//循环输出节点内容
            Element e = (Element)nodeList.item(i);//取出每一元素
            System.out.println("姓名：" + e.getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
            System.out.println("email：" + e.getElementsByTagName("email").item(0).getFirstChild().getNodeValue());
        }
    }
}
