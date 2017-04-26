package com.yyy.tippers.logging.factory;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class LoggingHandlerFactory implements HandlerFactory {

    private final Map<String, Handlerable> handlerableBinder;

    @Inject
    public LoggingHandlerFactory(Map<String, Handlerable> mapBinder) {
        this.handlerableBinder = mapBinder; // set to be injected in HandlerGuiceModule.java
    }

    @Override
    public Handlerable getHandler(String format) {
        return handlerableBinder.get(format);
    }
}
