package com.yyy.tippers.logging.handlers;

/**
 * Created by shayangzang on 4/25/17.
 */

import com.yyy.tippers.logging.factory.Handlerable;

public class HandlerForXML implements Handlerable {

    @Override
    public void showType() {
        System.out.println("<XML-Handler>");
        return ;
    }

}
