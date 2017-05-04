package com.yyy.tippers.logging.handlers;

import com.yyy.tippers.logging.factory.Handlerable;

/**
 * Created by shayangzang on 4/25/17.
 */

public class HandlerForJSON implements Handlerable {

    @Override
    public void showType() {
        System.out.println("<JSON-Handler>");

    }
    public Object parse(String content){
        return 1;
    }
}
