package com.yyy.tippers.logging.handlers;

import com.javatpoint.Employee;
import com.yyy.tippers.logging.factory.Handlerable;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by shayangzang on 4/25/17.
 */

public class HandlerForJSON implements Handlerable {
    private String className = "";

    @Override
    public void showType(String className) {
        this.className = className;
        System.out.println("<JSON-Handler>");

    }
    public Object parse(String content){
        ObjectMapper mapper = new ObjectMapper();

        /**
         * Read object from file
         */

        if(className.toLowerCase().equals("employee")) {
            Employee value = null;
            try {
                value = mapper.readValue(content, Employee.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return  value;

        }

        return null;
        
    }




}
