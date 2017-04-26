package com.yyy.tippers.logging.factory;

/**
 * Created by shayangzang on 4/25/17.
 */



public interface HandlerFactory {
    public Handlerable getHandler(String format);
}
