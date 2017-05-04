package com.yyy.tippers.logging.utils;

/**
 * Created by yiranwang on 5/3/17.
 */

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface TransactionLogRepository extends CrudRepository<TransactionLog, AtomicInteger>{
    TransactionLog findBytxid(AtomicInteger txid);
    List<TransactionLog> findAll();
}
