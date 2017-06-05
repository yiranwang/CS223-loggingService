package com.yyy.tippers.logging.geode;

/**
 * Created by yiranwang on 5/3/17.
 */

import com.yyy.tippers.logging.utils.Transaction;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer>{

    Transaction findByTxid(int txid);

    Transaction findByLsn(int lsn);

    List<Transaction> findAll();

//    @Query("select MAX(txid) from /Transaction")
    @Query("SELECT DISTINCT * FROM /logRegion ORDER BY txid desc limit 1")
    Transaction findByLargestTxid();

    @Query("SELECT DISTINCT * FROM /logRegion ORDER BY lsn desc limit 1")
    Transaction findByLargestLsn();
}
