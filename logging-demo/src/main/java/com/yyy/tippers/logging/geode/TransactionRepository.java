package com.yyy.tippers.logging.geode;

/**
 * Created by yiranwang on 5/3/17.
 */

import com.yyy.tippers.logging.utils.Transaction;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer>{

    @Query("select distinct * from /logRegion where txid = $1")
    Collection<Transaction> findByTxid(int txid);

    Transaction findByLsn(int lsn);

    @Query("SELECT DISTINCT * From /logRegion where lsn <= $1 ORDER BY lsn desc limit 1")
    Transaction findByLsnLessThanEqual(int lsn);

    Collection<Transaction> findAll();

//    @Query("select MAX(txid) from /Transaction")
    @Query("SELECT DISTINCT * FROM /logRegion ORDER BY txid desc limit 1")
    Transaction findByLargestTxid();

    @Query("SELECT DISTINCT * FROM /logRegion ORDER BY lsn desc limit 1")
    Transaction findByLargestLsn();

    @Query("select distinct * from /logRegion where time_stamp >= $1 AND time_stamp <= $2")
    Collection<Transaction> findByTimeInterval(int beginTime, int endTime);

    @Query("select distinct * from /logRegion where log_type = $1")
    Collection<Transaction> findByLog_typeIgnoreCase(String log_type);
}
