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
    private int time_stamp;
    private String log_type;
    private Payload payload;

    private Object object;
    private Transaction prev;
    private Transaction next;

    //no used
    private TransactionLog transactionLog;

    @PersistenceConstructor
    public Transaction(int lsn, int txid, int time_stamp, String log_type, Payload payload, Object object) {
        this.lsn = lsn;
        this.txid = txid;
        this.time_stamp = time_stamp;
        this.log_type = log_type;
        this.payload = payload;
        this.object = object;
        this.prev = null;
        this.next = null;
    }

    public Transaction() {
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

    public int getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(int time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getLog_type() {
        return log_type;
    }

    public void setLog_type(String log_type) {
        this.log_type = log_type;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
