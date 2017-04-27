package com.yyy.tippers.logging;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.yyy.tippers.logging.factory.Handlerable;
import com.yyy.tippers.logging.factory.HandlerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LoggingService {

    private final HandlerFactory handlerFactory;


    /*
      This constructor binds an handlerFactory instance with the LooggingService
      HandlerFactory was an interface, implemented by "LoggingHandlerFactory" class, which is set to bind "HandlerFactory" in HandlerGuiceModule.java
        Therefore, we can instantiate the "interface" directly here. But it is actually a LoggingHandlerFactory instance.
     */

    @Inject
    public LoggingService(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }


    /*
      Todo: YueDing -> Retrieve transaction ID from local DB, code resides in db package.
     */

    public AtomicInteger newTransaction() {
        AtomicInteger txid = new AtomicInteger(0);
        // txid = db.SomeDataBaseClass.nextEntryIndex()
        return txid;
    }


    /*
      Here is how we can invoke the handlerFactory to produce handler for us according to some input.
      The condition logic is defined in the concrete class - LoggingHandlerFactory.java
     */

    public int writeLog(AtomicInteger txid, String content, String format) {

        // with runtime input, make handlerFactory concrete.
        // Now different handlers are available
        Handlerable handler = handlerFactory.getHandler(format);
        handler.parse(content);

        // To test dependency injection
        handler.showType();

        /*
          Todo: Shengnan, Shayang -> Implement HandlerForXML first, code resides in handlers package. Specifically: passing in txid, content and format, parse content and store them in in-mem DB.
         */


        return 0; // signal success.
    }


    public int flushLog(AtomicInteger txid) {
        /*
          Although, we are not sure about in-mem DB APIs yet. I will update this before next Monday.
          Todo: YueDing -> Build connection between in-mem DB and local DB, code resides in db package.
         */

        System.out.println("flush successful!");
        return 0;
    }
}
