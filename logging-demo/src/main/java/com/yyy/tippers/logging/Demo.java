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

        payload.setXmlContent(Constant.xmlContent1);
        loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setXmlContent(Constant.xmlContent2);
        loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setXmlContent(Constant.xmlContent3);
        loggingService.writeLog(txid, timestamp++, type, payload);

//        loggingService.queryLog(txid, lsn); // this one later

//        loggingService.writeLog(txid, "JSON_content", "JSON"); // for dependency injection testing only

        loggingService.flushLog(3);

        //int testTx = loggingService.deleteLogsByLogType("Database Log");

        // test the second transaction, containing 3 logs
        txid = loggingService.newTransaction();

        payload.setXmlContent(Constant.xmlContent1);
        loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setXmlContent(Constant.xmlContent2);
        loggingService.writeLog(txid, timestamp++, type, payload);

        payload.setXmlContent(Constant.xmlContent3);
        loggingService.writeLog(txid, timestamp++, type, payload);

        //int testTx = loggingService.deleteLogsByLogType("Database Log");

        loggingService.flushLog(6);


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

        /**
         * Naively start a the Geode to store the transaction.
         * Yue config it during constructing db.DbService() in LoggingService.java.
         */

//        SpringApplication geoApp = new SpringApplication(GeodeApplication.class);
//        geoApp.setWebEnvironment(false);
//        String input="";
//        geoApp.run(input);

        demo.start();
        System.out.println( "End." );

    }
}
