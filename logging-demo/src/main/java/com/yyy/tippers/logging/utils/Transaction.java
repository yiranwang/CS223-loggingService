package com.yyy.tippers.logging.utils;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.Region;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Yue on 5/4/17.
 */

@Region("logRegion")
public class Transaction {
    @Id
    private int txid;
    private TransactionLog transactionLog;

    @PersistenceConstructor
    public Transaction(int txid, TransactionLog transactionLog) {
        this.txid = txid;
        this.transactionLog = transactionLog;
    }

    public int getTxid() {
        return txid;
    }

    public void setTxid(int txid) {
        this.txid = txid;
    }

    public TransactionLog getTransactionLog() {
        return transactionLog;
    }

    public void setTransactionLog(TransactionLog transactionLog) {
        this.transactionLog = transactionLog;
    }
}
