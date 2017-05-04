package com.yyy.tippers.logging.utils;

/**
 * Created by shayangzang on 5/3/17.
 */
public class TransactionEntry {

    private int lsn;
    private Object entryObject;
    private TransactionEntry next;
    private TransactionEntry prev;

    public TransactionEntry(int lsn, Object entryObject) {
        this.lsn = lsn;
        this.entryObject = entryObject;
        this.next = null;
        this.prev = null;
    }

    public int getLSN() {
        return lsn;
    }
    public Object getEntryObject() {
        return entryObject;
    }

    public void setNextEntry(TransactionEntry nextEntry) {
        if (next != null) throw new IllegalStateException("The current <TransactionEntry> has next entry already.");
        next = nextEntry;
    }
    public TransactionEntry getNextEntry() {
        if (next == null) throw new NullPointerException("This entry doesn't link to any next entry. This is the last entry.");

        return next;
    }

    public void setPrevEntry(TransactionEntry prevEntry) {
        if (prev != null) throw new IllegalStateException("The current <TransactionEntry> has previous entry already.");
        prev = prevEntry;

    }
    public TransactionEntry getPrevEntry() {
        if (prev == null) throw new NullPointerException("This entry doesn't link to any previous entry. This is the first entry.");

        return prev;
    }

    public boolean hasNext() {
        return (next != null);
    }
    public boolean hasPrev() {
        return (prev != null);
    }

    public void nullifyNextPointer() {
        next = null;
    }
    public void nullifyPrevPointer() {
        prev = null;
    }
}
