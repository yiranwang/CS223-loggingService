package com.yyy.tippers.logging.handlers;

import com.javatpoint.Employee;
import com.shiporder.data.Shiporder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Created by yiranwang on 4/30/17.
 */
public class UnmarshallXMLClass {
    String firstTag;
    String xmlContent;

    public UnmarshallXMLClass(String firstTag, String xmlContent){
        this.firstTag = firstTag;
        this.xmlContent = xmlContent;
        getObject();
    }

    /**
        This method is to unmarshall xml to different class. To match the exact class, hard code
        use the if else block. Here, just take two classes as an example.
        After get the java object, we could store it into Apache-Geode
     */
    public void getObject(){
        try{
            StringReader reader = new StringReader(xmlContent);
            JAXBContext jaxbContext;
            Unmarshaller jaxbUnmarshaller;
            if(firstTag.equals("employee")){
                jaxbContext = JAXBContext.newInstance(Employee.class);
                jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Employee employee = (Employee) jaxbUnmarshaller.unmarshal(reader);

            }else if(firstTag.equals("shiporder")){
                jaxbContext = JAXBContext.newInstance(Shiporder.class);
                jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Shiporder shiporder = (Shiporder) jaxbUnmarshaller.unmarshal(reader);
            }



        }catch (javax.xml.bind.JAXBException e){
            e.printStackTrace();
        }




    }


}
