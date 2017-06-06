package com.yyy.tippers.logging;

/**
 * Created by shayangzang on 4/25/17.
 */


import com.google.inject.Inject;
import com.yyy.tippers.logging.db.DbService;
import com.yyy.tippers.logging.entity.Payload;
import com.yyy.tippers.logging.factory.HandlerFactory;
import com.yyy.tippers.logging.factory.Handlerable;
import com.yyy.tippers.logging.geode.TransactionRepository;
import com.yyy.tippers.logging.utils.Transaction;
import com.yyy.tippers.logging.utils.TransactionEntry;
import com.yyy.tippers.logging.utils.TransactionLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingService {

    private final HandlerFactory handlerFactory; // placeholder for the injected

    private DbService dbService; // private DbService dbService = new DbService();

    /*
      This constructor binds an HandlerFactory instance with the LoggingService
            HandlerFactory was an interface, implemented by "LoggingHandlerFactory" class, which is set to bind "HandlerFactory" in HandlerGuiceModule.java
            Therefore, we can instantiate the "interface" directly here. But it is actually a LoggingHandlerFactory instance.
     */
    @Inject
    public LoggingService(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
        this.dbService = new DbService();
    }


    public int newTransaction() {
        int txid = dbService.getNextTxid(); // retrieve transaction id from db
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
            // with runtime input - format, generate specific and concrete handler.
            Handlerable handler = handlerFactory.getHandler("JSON");

            // unmarshal the input - content into log entry object
            object = handler.parse(payload.getXmlContent());

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

    public int writeLog(int txid, int timestamp, String type, Payload payload) {

        // get object based on payload type
        Object object = parseObject(payload);

        //get the latest transaction from geode according to largest lsn defined in /geode/TransactionRepository.java
        Transaction prevTx = dbService.getTransactionRepository().findByLargestLsn();

        Transaction tx = null;

        /*
          txid and lsn are two independently maintained integers, monotonically increasing.
          txid helps to group the logs within the same transaction together.
          lsn keeps count of how many logs have been written both in Geode as well as in MySQL.
          Therefore, we first look Geode for the current maximum of lsn, and then look into MySQL it is not found in Geode.
        */
        int lsn = 0;

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

        //.save() comes with Geode; insert current transaction to geode region.
        dbService.getTransactionRepository().save(tx);

        System.out.println(String.format("<LoggingService><writelog> add an entry (lsn: %d) into <TransactionLog> (txid: %d)", lsn, txid));

        // int lsn_test = txlg_test.append(object);
        // System.out.println(String.format("TEST: <LoggingService><writelog> add an entry (lsn_test: %d) into <TransactionLog> (txid: %d)", lsn_test, txid));

        return lsn;

    }

    public Transaction queryLogByLsn(int lsn) {

        Transaction tx = null;

        // first search from geode
        tx = dbService.getTransactionRepository().findByLsn(lsn);

        // if not in geode, search in mysql
        if (tx == null) {
            tx = dbService.getTxByLsnInMysql(lsn);
        }

        return tx;
    }

    public List<Transaction> queryLogListByTxid(int txid) {
        List<Transaction> txList = new ArrayList<Transaction>();

        //frist search geode
        txList.addAll(dbService.getTransactionRepository().findByTxid(txid));

        //second search mysql
        txList.addAll(dbService.getTxByTxidInMysql(txid));

        return txList;
    }

    public List<Transaction> queryLogListByTimeInterval(int beginTime, int endTime) {
        List<Transaction> txList = new ArrayList<Transaction>();

        //first search geode
        txList.addAll(dbService.getTransactionRepository().findByTimeInterval(beginTime, endTime));

        //second search mysql
        txList.addAll(dbService.getTxByTimeIntervalInMysql(beginTime, endTime));

        return txList;
    }

    public List<Transaction> queryLogListByLogType(String log_Type) {
        List<Transaction> txList = new ArrayList<Transaction>();

        //first search geode
        txList.addAll(dbService.getTransactionRepository().findByLog_typeIgnoreCase(log_Type));

        //second search mysql
        txList.addAll(dbService.getTxByLogTypeInMysql(log_Type));

        return txList;
    }

    public int deleteLogByLsn(int lsn) {
        int count = 0;

        //first delete in geode
        TransactionRepository transactionRepository = dbService.getTransactionRepository();

        Transaction tx = transactionRepository.findByLsn(lsn);
        if (tx != null) {
            transactionRepository.delete(tx);

            Transaction prevTx = tx.getPrev();
            Transaction nextTx = tx.getNext();

            if (prevTx != null) {
                prevTx.setNext(nextTx);
            }

            if (nextTx != null) {
                nextTx.setPrev(prevTx);
            }
            return 1;
        }

        //delete in mysql
        count = dbService.deleteLogByLsnInMysql(lsn);

        return count;
    }

    public int deleteLogsByTxid(int txid) {
        int count = 0;

        //delete in geode
        TransactionRepository transactionRepository = dbService.getTransactionRepository();

        List<Transaction> transactions = (List<Transaction>)transactionRepository.findByTxid(txid);

        for (Transaction tx : transactions) {
            transactionRepository.delete(tx);

            Transaction prevTx = tx.getPrev();
            Transaction nextTx = tx.getNext();

            if (prevTx != null) {
                prevTx.setNext(nextTx);
            }

            if (nextTx != null) {
                nextTx.setPrev(prevTx);
            }
        }
        count += transactions.size();

        //delete in mysql
        count += dbService.deleteLogsByTxidInMysql(txid);

        return count;
    }

    public int deleteLogsByTimeInterval(int beginTime, int endTime) {
        int count = 0;

        //delete in geode
        TransactionRepository transactionRepository = dbService.getTransactionRepository();

        List<Transaction> transactions = (List<Transaction>)transactionRepository.findByTimeInterval(beginTime, endTime);

        for (Transaction tx : transactions) {
            transactionRepository.delete(tx);

            Transaction prevTx = tx.getPrev();
            Transaction nextTx = tx.getNext();

            if (prevTx != null) {
                prevTx.setNext(nextTx);
            }

            if (nextTx != null) {
                nextTx.setPrev(prevTx);
            }
        }
        count += transactions.size();

        //delete in mysql
        count += dbService.deleteLogsByTimeIntervalInMysql(beginTime, endTime);

        return count;
    }

    public int deleteLogsByLogType(String log_type) {
        int count = 0;

        //delete in geode
        TransactionRepository transactionRepository = dbService.getTransactionRepository();

        List<Transaction> transactions = (List<Transaction>)transactionRepository.findByLog_typeIgnoreCase(log_type);

        for (Transaction tx : transactions) {
            transactionRepository.delete(tx);

            Transaction prevTx = tx.getPrev();
            Transaction nextTx = tx.getNext();

            if (prevTx != null) {
                prevTx.setNext(nextTx);
            }

            if (nextTx != null) {
                nextTx.setPrev(prevTx);
            }
        }
        count += transactions.size();

        //delete in mysql
        count += dbService.deleteLogsByLogType(log_type);

        return count;
    }

    /*
       flushes the log upto given LSN to the disk, atomically

    */
    public synchronized int flushLog(int lsn) {

        //List<Transaction> deletedLogs = (List<Transaction>)dbService.getTransactionRepository().findByLsnLessThanEqual(lsn);
        Transaction tx = dbService.getTransactionRepository().findByLsnLessThanEqual(lsn);

        Transaction curTx = tx;

        while (curTx != null) {

            //save curTx to external database
            dbService.saveLogToDb(curTx);

            //remove curTx from geode
            dbService.getTransactionRepository().delete(curTx);

            curTx = curTx.getPrev();
        }
        if (tx != null) {
            Transaction nextTx = tx.getNext();
            if (nextTx != null) {
                nextTx.setPrev(null);
            }
        }

        System.out.println("flush successful!");
        return 0;
    }
}
