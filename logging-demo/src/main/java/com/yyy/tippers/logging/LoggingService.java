package com.yyy.tippers.logging;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.yyy.tippers.logging.db.DbService;
import com.yyy.tippers.logging.factory.Handlerable;
import com.yyy.tippers.logging.factory.HandlerFactory;
import com.yyy.tippers.logging.utils.Transaction;
import com.yyy.tippers.logging.utils.TransactionEntry;
import com.yyy.tippers.logging.utils.TransactionLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingService {

    private final HandlerFactory handlerFactory; // placeholder for the injected

    // for test-purpose only
    private final Map<AtomicInteger, TransactionLog> transactionManager;

//    private DbService dbService = new DbService();
    private DbService dbService;

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
        this.transactionManager = new HashMap<AtomicInteger, TransactionLog>(); // for test-purpose only
        this.dbService = new DbService();
    }


    public AtomicInteger newTransaction() {
        AtomicInteger txid = dbService.getNextTxid();

        transactionManager.put(txid, new TransactionLog(txid)); // for test-purpose only
        return txid;
    }


    /*
      Here is how we can invoke the handlerFactory to produce handler for us according to some input.
      The condition logic is defined in the concrete class - LoggingHandlerFactory.java
     */

    public void writeLog(AtomicInteger txid, String content, String format) {

        //get transactionLog from geode according to txid
        Transaction tx = dbService.getTransactionRepository().findByTxid(txid);

        TransactionLog txlg = tx.getTransactionLog();

        // with runtime input - format, generate specific and concrete handler.
        Handlerable handler = handlerFactory.getHandler(format);

        // unmarshal the input - content into log entry object
        Object obj = handler.parse(content);

        // put the entryObj into TransactionLog - a doublyLinkedList
        int lsn = txlg.append(obj);

        System.out.println(String.format("<LoggingService><writelog> add an entry (lsn: %d) into <TransactionLog> (txid: %d)", lsn, txid.get()));

        // for test-purpose only
        TransactionLog txlg_test = transactionManager.get(txid);
        int lsn_test = txlg_test.append(obj);
        System.out.println(String.format("TEST: <LoggingService><writelog> add an entry (lsn_test: %d) into <TransactionLog> (txid: %d)", lsn_test, txid.get()));


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
