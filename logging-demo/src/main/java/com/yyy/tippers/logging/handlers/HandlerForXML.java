package com.yyy.tippers.logging.handlers;

import com.javatpoint.Employee;
import com.yyy.tippers.logging.factory.Handlerable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;


public class HandlerForXML implements Handlerable {

    @Override
    public void showType() {
        System.out.println("<XML-Handler>");
        return ;
    }

    @Override
    public void parse(String xmlContent) {

        String firstTagName = parseFirstTag(xmlContent);
        UnmarshallXMLClass u = new UnmarshallXMLClass(firstTagName, xmlContent);
    }

    /**
     *
     * @param xmlContent
     * @return firstTag - the first tag name of xml, which is used to match to the corresponding java class.
     */
    public String parseFirstTag(String xmlContent){
        String firstTag = "";
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
            doc.getDocumentElement().normalize();
            Node rootNode = doc.getDocumentElement();
            firstTag = rootNode.getNodeName().toString();

        }catch(javax.xml.parsers.ParserConfigurationException e1){
            e1.printStackTrace();

        }catch(java.io.UnsupportedEncodingException e2){
            e2.printStackTrace();

        }catch(java.io.IOException e3){
            e3.printStackTrace();
        }catch(org.xml.sax.SAXException e4){
            e4.printStackTrace();
        }

        System.out.println(String.format("<HandlerForXML><parseFirstTag> $firstTag : %s", firstTag));

        return firstTag;

    }



}
