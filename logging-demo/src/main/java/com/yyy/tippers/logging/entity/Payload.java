package com.yyy.tippers.logging.entity;

/**
 * Created by Yue on 6/4/17.
 */
public class Payload {
    private String type; //XML, JSON, Plaintxt, Binary
    private String content;

    public Payload() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
