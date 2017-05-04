package com.yyy.tippers.logging.factory;

/*
  This Handlerable interface is defined so that we don't need to specify "what type of handler" we want in COMPILE time.
 */
public interface Handlerable {
    void showType();
    Object parse(String content);
}
