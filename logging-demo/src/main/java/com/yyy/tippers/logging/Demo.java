package com.yyy.tippers.logging;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.yyy.tippers.logging.Constant;
import com.yyy.tippers.logging.utils.GeodeApplication;
import org.springframework.boot.SpringApplication;

import java.util.concurrent.atomic.AtomicInteger;

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


        // Todo : Yue - need to implement this method asap so that multi-transaction behavior can be tested.
        AtomicInteger txid = loggingService.newTransaction();


        String format = "XML";

        loggingService.writeLog(txid, Constant.xmlContent1, format);
        loggingService.writeLog(txid, Constant.xmlContent2, format);
        loggingService.writeLog(txid, Constant.xmlContent3, format);

        loggingService.queryLog(txid); // implement this first
//        loggingService.queryLog(txid, lsn); // this one later

//        loggingService.writeLog(txid, "JSON_content", "JSON"); // for dependency injection testing only

        loggingService.flushLog(txid);
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

        //start a the Geode to store the transaction
        SpringApplication geoApp = new SpringApplication(GeodeApplication.class);
        geoApp.setWebEnvironment(false);
        String input="";
        geoApp.run(input);




        demo.start();
        System.out.println( "End." );

    }
}
