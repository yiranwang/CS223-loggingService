package com.yyy.tippers.logging.entity;

/**
 * Created by Yue on 6/4/17.
 */
public class Payload {
    private String type; //XML, JSON, Plaintxt, Binary
    private String xmlContent;
    private String jsonContent; // should import Json libarary
    private String txtContent;
    private String binaryContent;

    public Payload() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    public String getTxtContent() {
        return txtContent;
    }

    public void setTxtContent(String txtContent) {
        this.txtContent = txtContent;
    }

    public String getBinaryContent() {
        return binaryContent;
    }

    public void setBinaryContent(String binaryContent) {
        this.binaryContent = binaryContent;
    }
}
