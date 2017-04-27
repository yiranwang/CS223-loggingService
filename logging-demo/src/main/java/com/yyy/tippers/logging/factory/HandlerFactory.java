package com.yyy.tippers.logging.factory;

/**
   This is a factory-level interface.
   We want it so that we don't need to specify "what type of factory" I want when LoggingService is instantiated.
 */



public interface HandlerFactory {
    public Handlerable getHandler(String format);
}
