package com.yyy.tippers.logging.geode;

/**
 * Created by yiranwang on 5/3/17.
 */

import com.yyy.tippers.logging.utils.Transaction;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, AtomicInteger>{

    Transaction findByTxid(AtomicInteger txid);

    List<Transaction> findAll();

//    @Query("select MAX(txid) from /Transaction")
    @Query("SELECT DISTINCT * FROM /logRegion ORDER BY txid desc limit 1")
    Transaction findLargestTxid();
}
