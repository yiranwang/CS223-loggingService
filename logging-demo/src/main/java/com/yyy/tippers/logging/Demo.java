package com.yyy.tippers.logging;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.yyy.tippers.logging.entity.Payload;


/**
 This implements a demo of how our loggingService can be incorporated in tippers app
 */
public class Demo
{
    private final LoggingService loggingService; // make loggingService a private attribute of the "fake-tipper" application

    /*
      Upon initialization,
        LoggingService instance is automatically injected into the constructor,
        as well as binded with "loggingService" attribute.
    */

    @Inject
    public Demo(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /*
      choiceOfHandlerFor(String format) is a method that showcases dependency injection.
      Specifically, it starts the application with loggingService already initialized and
        takes in a String variable at the RUNTIME (when loggingService is running) and makes it behave differently.
     */

    private void start() {

        //get next Txid
        // test first transaction, containing 3 logs
        int txid = loggingService.newTransaction();

        String format = "XML";

        int timestamp = 1;
        String type = "Database Log";

        Payload payload = new Payload();
        payload.setType("XML");

        /* using payload() allows for the segmentation of the logic of unmarshaling the XMLString and the logic of writing the log */

        int lsn;

        payload.setContent(Constant.xmlContent1);
        lsn = loggingService.writeLog(txid, timestamp++, type, payload); // type is not necessary but we are lazy.

        payload.setContent(Constant.xmlContent2);
        lsn = loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setContent(Constant.xmlContent3);
        lsn = loggingService.writeLog(txid, timestamp++, type, payload);

        loggingService.flushLog(lsn);

        // test the second transaction, containing 3 logs
        txid = loggingService.newTransaction();

        payload.setContent(Constant.xmlContent1);
        lsn = loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setContent(Constant.xmlContent2);
        lsn = loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setContent(Constant.xmlContent3);
        lsn = loggingService.writeLog(txid, timestamp++, type, payload);

        //List<Transaction> txls = loggingService.queryLogListByPayload("employee", "firstname", "vimal");
        //nt count = loggingService.deleteLogsByPayload("employee", "firstname", "vimal");

        loggingService.flushLog(lsn);

    }

    /*
      An Injector guice is created based on configurations defined in HandlerGuiceModule.java.
      When you try to get Demo instance out of the Injector guice, it will enable the instance to behave differently, given different input.
      Specifically, the demo instance, coming with "loggingService" attribute, which binds to "HandlerFactory". See details in LoggingService.java
     */

    public static void main( String[] args )
    {
        System.out.println( "Start..." );

        Injector guice = Guice.createInjector(new HandlerGuiceModule());

        Demo demo = guice.getInstance(Demo.class);

        demo.start();

        System.out.println( "End." );

    }
}
