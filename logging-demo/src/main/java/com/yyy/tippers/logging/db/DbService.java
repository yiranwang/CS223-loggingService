package com.yyy.tippers.logging.db;

import com.gemstone.gemfire.cache.Region;
import com.yyy.tippers.logging.geode.TransactionRepository;
import com.yyy.tippers.logging.utils.Transaction;
import com.yyy.tippers.logging.utils.TransactionLog;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Yue on 5/4/17.
 */
public class DbService {
    private DataSource ds; // used for connection to mySQL
    private TransactionRepository transactionRepository;
    private Region<Integer, Transaction> region;

    public DbService() {

        //get datasource for connection with mySQL
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("dbconfig.xml");
        this.ds = (DataSource)applicationContext.getBean("dataSource");


        //get the logRegion and transactionLogRepository from geodeApplication and gfshBean.xml
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();

        context.setConfigLocation("gfshBean.xml");

        context.refresh();
        this.transactionRepository = context.getBean(TransactionRepository.class);
        region = context.getBean(Region.class);
    }

    public DataSource getDs() {
        return ds;
    }

    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Region<Integer, Transaction> getRegion(){
        return region;
    }

    /*
            Get next transaction id from geode and mysql by getting the largest txid so far + 1
        * */
    public synchronized int getNextTxid() {

        // check the transaction is stored in Geode or not:
        boolean isInGeode = true;

        //get largest txid from mysql
        Connection conn = DataSourceUtils.getConnection(ds);

        int largestTxid = 0;

        try {
            PreparedStatement ps = conn.prepareStatement("select MAX(txid) from transactionLog");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                largestTxid = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        //get largest txid from geode

        int largestTxidGeo = 0;
        Transaction a = transactionRepository.findLargestTxid();
        if(a != null){
            largestTxidGeo = a.getTxid();
        }else{
            isInGeode = false;
        }

        largestTxid = Math.max(largestTxid, largestTxidGeo);

        int finalTxid = largestTxid + 1;

        // add the new Transaction to the Geode region
        if(!isInGeode){
            Transaction newTransaction = new Transaction(finalTxid, new TransactionLog(finalTxid));
            region.put(finalTxid, newTransaction);
        }

        return finalTxid;


    }



}
