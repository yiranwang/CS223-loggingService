package com.yyy.tippers.logging.utils;

import com.yyy.tippers.logging.entity.Payload;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.Region;

/**
 * Created by Yue on 5/4/17.
 */

@Region("logRegion")
public class Transaction {
    @Id
    private int lsn;
    private int txid;
    private int timestamp;
    private String type;
    private Payload payload;

    private Object object;
    private Transaction prev;
    private Transaction next;

    //no used
    private TransactionLog transactionLog;

    @PersistenceConstructor
    public Transaction(int lsn, int txid, int timestamp, String type, Payload payload, Object object) {
        this.lsn = lsn;
        this.txid = txid;
        this.timestamp = timestamp;
        this.type = type;
        this.payload = payload;
        this.object = object;
        this.prev = null;
        this.next = null;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Transaction getPrev() {
        return prev;
    }

    public void setPrev(Transaction prev) {
        this.prev = prev;
    }

    public Transaction getNext() {
        return next;
    }

    public void setNext(Transaction next) {
        this.next = next;
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

    public int getLsn() {
        return lsn;
    }

    public void setLsn(int lsn) {
        this.lsn = lsn;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
