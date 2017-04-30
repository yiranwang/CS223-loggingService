package com.yyy.tippers.logging;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

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

    private void start(String content, String format) {

        AtomicInteger txid = loggingService.newTransaction();
        loggingService.writeLog(txid, content, format);
//        loggingService.writeLog(txid, "JSON_content", "JSON");
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
        String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<employee\n" +
                "        xmlns=\"http://www.javatpoint.com\"\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"http://www.javatpoint.com employee.xsd\">\n" +
                "\n" +
                "    <firstname>vimal</firstname>\n" +
                "    <lastname>jaiswal</lastname>\n" +
                "    <email>vimal@javatpoint.com</email>\n" +
                "</employee>";
        demo.start(xmlContent, "XML");
        System.out.println( "End." );

    }
}
