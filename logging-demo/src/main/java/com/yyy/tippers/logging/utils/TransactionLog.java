package com.yyy.tippers.logging.utils;


import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shayangzang on 5/3/17.
 */

public class TransactionLog {

    private AtomicInteger txid;
    private int lsn = -1;

    private TransactionEntry head;
    private TransactionEntry tail;
    private TransactionEntry pointer;
    private TransactionEntry curtEntry;

    public TransactionLog(AtomicInteger txid) {
        this.pointer = null; // pointer set to search through the Linkedlist for particular entry
        this.curtEntry = null; // keep track of the last entry
        this.txid = txid;
        this.head = new TransactionEntry(-1, null); // head.next is null
        this.tail = new TransactionEntry(-1, null); // tail.prev is null
//        this.head.setNextEntry(this.tail);
//        this.tail.setPrevEntry(this.head);
    }

    public int append(Object entryObj) {
        lsn++;
        TransactionEntry nextEntry = new TransactionEntry(lsn, entryObj); // nextEntry.next is null and nextEntry.prev is null
        if (curtEntry == null) {
            head.setNextEntry(nextEntry); // assert head.next is null
            nextEntry.setPrevEntry(head); // assert nextEntry.prev is null
        }
        else {
            curtEntry.nullifyNextPointer();     // nullify the next pointer of current entry so that a new link can be made
            curtEntry.setNextEntry(nextEntry);  //      between current entry and the next entry.
            nextEntry.setPrevEntry(curtEntry);  // assert nextEntry.prev is null
            tail.nullifyPrevPointer();          // nullify the prev pointer of the tail so that a new link can be made
                                                //      between next entry and tail
        }
        nextEntry.setNextEntry(tail); // assert nextEntry.next is null
        tail.setPrevEntry(nextEntry); // assert tail.prev is null
        curtEntry = nextEntry;

        return lsn;
    }

    public AtomicInteger getID() {
        return txid;
    }

    public TransactionEntry getFirstEntry() {
        return head.getNextEntry();
    }
    public TransactionEntry getEntryNo(int lsn) {
        pointer = head;
        while (pointer.hasNext()) {
            TransactionEntry curtEntry = pointer.getNextEntry();
            if (curtEntry.getLSN() == lsn) return curtEntry;
            pointer = curtEntry;
        }
        throw new NoSuchElementException(String.format("No entry object found at No.%d in this <TransactionLog>", lsn));
    }


}
