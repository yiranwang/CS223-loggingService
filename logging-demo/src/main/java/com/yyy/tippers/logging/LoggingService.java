package com.yyy.tippers.logging;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.yyy.tippers.logging.db.DbService;
import com.yyy.tippers.logging.entity.Payload;
import com.yyy.tippers.logging.factory.Handlerable;
import com.yyy.tippers.logging.factory.HandlerFactory;
import com.yyy.tippers.logging.utils.Transaction;
import com.yyy.tippers.logging.utils.TransactionEntry;
import com.yyy.tippers.logging.utils.TransactionLog;

import java.util.HashMap;
import java.util.Map;

public class LoggingService {

    private final HandlerFactory handlerFactory; // placeholder for the injected

    // for test-purpose only
    private final Map<Integer, TransactionLog> transactionManager;

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
        this.transactionManager = new HashMap<Integer, TransactionLog>(); // for test-purpose only
        this.dbService = new DbService();
    }


    public int newTransaction() {
        int txid = dbService.getNextTxid();

        transactionManager.put(txid, new TransactionLog(txid)); // for test-purpose only
        return txid;
    }

    public int getNextLsn() {
        return dbService.getNextLsn();
    }

    public Object parseObject(Payload payload) {

        Object object = null;

        if (payload.getType().equals("XML")) {
            // with runtime input - format, generate specific and concrete handler.
            Handlerable handler = handlerFactory.getHandler("XML");

            // unmarshal the input - content into log entry object
            object = handler.parse(payload.getXmlContent());

        }else if (payload.getType().equals("JSON")) {
            // TODO: JSON

        }else if (payload.getType().equals("Plaintxt")) {
            object = payload.getTxtContent();

        }else if (payload.getType().equals("Binary")) {
            byte[] payload_bytes = payload.getBinaryContent().getBytes();
            object = payload_bytes;
        }

        return object;
    }


    /*
      Here is how we can invoke the handlerFactory to produce handler for us according to some input.
      The condition logic is defined in the concrete class - LoggingHandlerFactory.java
     */

    public void writeLog(int txid, int timestamp, String type, Payload payload) {

        /*//get transactionLog from geode according to txid
        Transaction tx = dbService.getTransactionRepository().findByTxid(txid);*/

        //get transactionLog from geode according to largest lsn
        Transaction prevTx = dbService.getTransactionRepository().findByLargestLsn();

        Transaction tx = null;

        int lsn = 0;

        // get object based on payload type
        Object object = parseObject(payload);

        // if no transaction in geod, we get the lsn from the largest Lsn number in mysql + 1.
        if(prevTx == null){
            lsn = dbService.getLargetLsnInMysql() + 1;

        // else we get lsn from prevTx + 1;
        }else {
            lsn = prevTx.getLsn() + 1;
        }

        tx = new Transaction(lsn, txid, timestamp, type, payload, object);

        //set prev transaction's next pointer and current transaction's prev pointer
        tx.setPrev(prevTx);

        if (prevTx != null) {
            prevTx.setNext(tx);
        }

        //save current transaction to geode region
        dbService.getTransactionRepository().save(tx);

        System.out.println(String.format("<LoggingService><writelog> add an entry (lsn: %d) into <TransactionLog> (txid: %d)", lsn, txid));

        // for test-purpose only
        TransactionLog txlg_test = transactionManager.get(txid);
        int lsn_test = txlg_test.append(object);
        System.out.println(String.format("TEST: <LoggingService><writelog> add an entry (lsn_test: %d) into <TransactionLog> (txid: %d)", lsn_test, txid));


    }

    // this is just for demo purpose!
    // Not sure where the output goes, we need to discuss the scope of the query method.
    public void queryLog(int txid) {
        TransactionEntry entry = transactionManager.get(txid).getFirstEntry();
        while (entry.hasNext()) {
            System.out.println(entry.getEntryObject());
            entry = entry.getNextEntry();
        }
    }

    public int flushLog(int lsn) {
        /*
          Although, we are not sure about in-mem DB APIs yet. I will update this before next Monday.
          Todo: YueDing -> Build connection between in-mem DB and local DB, code resides in db package.
         */

        Transaction tx = dbService.getTransactionRepository().findByLsn(lsn);

        Transaction curTx = tx;

        while (curTx != null) {

            //save curTx to external database
            dbService.saveLogToDb(curTx);

            //remove curTx from geode
            dbService.getTransactionRepository().delete(curTx);

            curTx = curTx.getPrev();
        }



        System.out.println("flush successful!");
        return 0;
    }
}
