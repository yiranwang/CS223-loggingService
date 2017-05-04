package com.yyy.tippers.logging.unitester;

import com.yyy.tippers.logging.utils.TransactionEntry;
import com.yyy.tippers.logging.utils.TransactionLog;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shayangzang on 5/3/17.
 */
public class TransactionLogTester {
    public static void main(String[] args) {
        // test constructor
        TransactionLog transaction = new TransactionLog(new AtomicInteger(0));

        // test append()
        int entry0 = transaction.append("FirstEntry");
        int entry1 = transaction.append("SecondEntry");
        int entry2 = transaction.append("ThirdEntry");

        // test getID()
        System.out.println(String.format("<TransactionTest><main> - transaction ID : %d", transaction.getID().get()));

        // test getFirstEntry()
        TransactionEntry firstEntry = transaction.getFirstEntry();
        System.out.println(String.format("<TransactionTest><main> - transaction ID : %s", firstEntry.getEntryObject()));

        // test getNextEntry()
        TransactionEntry secondEntry = firstEntry.getNextEntry();
        System.out.println(String.format("<TransactionTest><main> - transaction ID : %s", secondEntry.getEntryObject()));

        // test getEntryNo()
        TransactionEntry thirdEntry = transaction.getEntryNo(entry2);
        System.out.println(String.format("<TransactionTest><main> - transaction ID : %s", thirdEntry.getEntryObject()));

        // test getPrevEntry()
        TransactionEntry prev = thirdEntry.getPrevEntry();
        System.out.println(String.format("<TransactionTest><main> - transaction ID : %s", prev.getEntryObject()));

    }
}
