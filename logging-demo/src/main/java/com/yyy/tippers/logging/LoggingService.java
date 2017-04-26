package com.yyy.tippers.logging;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.yyy.tippers.logging.factory.Handlerable;
import com.yyy.tippers.logging.factory.HandlerFactory;

public class LoggingService {

    private final HandlerFactory handlerFactory;

    @Inject
    public LoggingService(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory; // initializing interface factory when LoggingService is constructed.
    }

    public String choiceOfHandlerFor(String format) {
        Handlerable handler = handlerFactory.getHandler(format); // with runtime input, make handlerFactory concrete.
        String choiceOfHandler = handler.handlerType();
        return choiceOfHandler;
    }
}
