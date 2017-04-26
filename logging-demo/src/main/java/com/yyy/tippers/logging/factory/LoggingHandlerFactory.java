package com.yyy.tippers.logging.factory;

/**
   This concrete LoggingHandlerFactory class implements HandlerFactory interface.
   It takes the injectable mapBinder FROM HandlerGuiceModule to construct itself.
   Constructor() and getHandler() are very well de-coupled.
 */


import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class LoggingHandlerFactory implements HandlerFactory {

    private final Map<String, Handlerable> handlerableBinder;

    /*
      The mapBinder is injected into this constructor FROM HandlerGuiceModule instance. It is pre-configured in HandlerGuiceModule.java
     */

    @Inject
    public LoggingHandlerFactory(Map<String, Handlerable> mapBinder) {
        this.handlerableBinder = mapBinder;
    }

    // This is where you can set your conditional logic.
    // Specifically, produce whichever type of handlers depending on the input.
    // Here no if-clause is needed because the input corresponds nicely with the map key.

    @Override
    public Handlerable getHandler(String format) {
        return handlerableBinder.get(format);
    }
}
