package com.yyy.tippers.logging;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.yyy.tippers.logging.factory.Handlerable;
import com.yyy.tippers.logging.factory.HandlerFactory;
import com.yyy.tippers.logging.utils.TransactionEntry;
import com.yyy.tippers.logging.utils.TransactionLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingService {

    private final HandlerFactory handlerFactory; // placeholder for the injected

    private final Map<AtomicInteger, TransactionLog> transactionManager; // in memory mapping txid -> transaction


    /*
      This constructor
      1. binds an HandlerFactory instance with the LoggingService
            HandlerFactory was an interface, implemented by "LoggingHandlerFactory" class, which is set to bind "HandlerFactory" in HandlerGuiceModule.java
            Therefore, we can instantiate the "interface" directly here. But it is actually a LoggingHandlerFactory instance.
      2. init transactionManager
     */

    @Inject
    public LoggingService(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        this.transactionManager = new HashMap<AtomicInteger, TransactionLog>();
    }


    /*
      Todo: YueDing -> Retrieve transaction ID from local DB, code resides in db package.
     */

    public AtomicInteger newTransaction() {
        AtomicInteger txid = new AtomicInteger(0);
        // txid = db.SomeDataBaseClass.nextEntryIndex() // it should always be unique
        transactionManager.put(txid, new TransactionLog(txid));
        return txid;
    }


    /*
      Here is how we can invoke the handlerFactory to produce handler for us according to some input.
      The condition logic is defined in the concrete class - LoggingHandlerFactory.java
     */

    public void writeLog(AtomicInteger txid, String content, String format) {
        // with runtime input - txid, retrieve the transactionLog from transactionManager
        TransactionLog txlg = transactionManager.get(txid);

        // with runtime input - format, generate specific and concrete handler.
        Handlerable handler = handlerFactory.getHandler(format);

        // unmarshal the input - content into log entry object
        Object obj = handler.parse(content);

        // put the entryObj into TransactionLog - a doublyLinkedList
        int lsn = txlg.append(obj);

        System.out.println(String.format("<LoggingService><writelog> add an entry (lsn: %d) into <TransactionLog> (txid: %d)", lsn, txid.get()));


        //  Todo: Shengnan, Yue -> Store TransactionLog obj in Geode.

    }

    // this is just for demo purpose!
    // Not sure where the output goes, we need to discuss the scope of the query method.
    public void queryLog(AtomicInteger txid) {
        TransactionEntry entry = transactionManager.get(txid).getFirstEntry();
        while (entry.hasNext()) {
            System.out.println(entry.getEntryObject());
            entry = entry.getNextEntry();
        }
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
