package com.yyy.tippers.logging.db;

import com.gemstone.gemfire.cache.Region;
import com.yyy.tippers.logging.entity.Payload;
import com.yyy.tippers.logging.geode.TransactionRepository;
import com.yyy.tippers.logging.utils.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            // use employeeLog table as an example
            PreparedStatement ps = conn.prepareStatement("select MAX(txid) from logTable");

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
        Transaction a = transactionRepository.findByLargestTxid();
        if(a != null){
            largestTxidGeo = a.getTxid();
        }else{
            isInGeode = false;
        }

        largestTxid = Math.max(largestTxid, largestTxidGeo);

        int finalTxid = largestTxid + 1;

        /*// add the new Transaction to the Geode region
        if(!isInGeode){
            Transaction newTransaction = new Transaction(finalTxid, new TransactionLog(finalTxid));
            region.put(finalTxid, newTransaction);
        }*/

        return finalTxid;

    }

    /*
        Get largest lsn from mysql
    * */
    public synchronized int getLargetLsnInMysql() {
        Connection conn = DataSourceUtils.getConnection(ds);

        int largestLsn = 0;

        try {
            // use employeeLog table as an example
            PreparedStatement ps = conn.prepareStatement("select MAX(lsn) from logTable");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                largestLsn = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }
        return largestLsn;
    }

    /*
        Get next lsn from geode and mysql by getting the largest lsn so far + 1
    * */
    public synchronized int getNextLsn() {

        // check the transaction is stored in Geode or not:
        boolean isInGeode = true;

        //get largest lsn from mysql
        Connection conn = DataSourceUtils.getConnection(ds);

        int largestLsn = 0;

        try {
            // use employeeLog table as an example
            PreparedStatement ps = conn.prepareStatement("select MAX(lsn) from logTable");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                largestLsn = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        //get largest lsn from geode

        int largestLsnGeo = 0;
        Transaction a = transactionRepository.findByLargestLsn();
        if(a != null){
            largestLsnGeo = a.getLsn();
        }else{
            isInGeode = false;
        }

        largestLsn = Math.max(largestLsn, largestLsnGeo);

        int finalLsn = largestLsn + 1;

        /*// add the new Transaction to the Geode region
        if(!isInGeode){
            Transaction newTransaction = new Transaction(finalTxid, new TransactionLog(finalTxid));
            region.put(finalTxid, newTransaction);
        }*/

        return finalLsn;

    }

    /*
    * attributes in logtable: lsn, txid, time_stamp, log_type(varchar), payload(varchar), payload_binary(BLOB)
    * */
    public void saveLogToDb(Transaction tx) {
        Connection conn = DataSourceUtils.getConnection(ds);
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement("INSERT INTO logTable(lsn, txid, time_stamp, log_type, payload, payload_binary) " +
                    "VALUES (?,?,?,?,?,?)");

            ps.setInt(1, tx.getLsn());
            ps.setInt(2, tx.getTxid());
            ps.setInt(3, tx.getTimestamp());
            ps.setString(4, tx.getType());

            Payload payload = tx.getPayload();

            if (payload.getType().equals("Binary")) {

                byte[] payload_bytes = payload.getBinaryContent().getBytes();
                ByteArrayInputStream byteIS = new ByteArrayInputStream(payload_bytes);

                ps.setString(5, null);
                ps.setBinaryStream(6, byteIS, payload_bytes.length);

            }else if (payload.getType().equals("XML")){
                ps.setString(5, payload.getXmlContent());
                ps.setBinaryStream(6, null);

            }else if (payload.getType().equals("JSON")){
                // TODO: JSON
                ps.setString(5, payload.getJsonContent());
                ps.setBinaryStream(6, null);

            }else if (payload.getType().equals("Plaintxt")){
                ps.setString(5, payload.getTxtContent());
                ps.setBinaryStream(6, null);

            }

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }
    }

    /*
        save doubleLinkedList log to external database
    * *//*
    public void saveEmployeeLogToDb(TransactionEntry curEntry, int txid) {

        Connection conn = DataSourceUtils.getConnection(ds);
        PreparedStatement ps = null;

        while (curEntry != null) {
            int lsn = curEntry.getLSN();

            TransactionEntry prevEntry = curEntry.getPrevEntry();

            String oldFirstName = null, oldLastName = null, oldEmail = null;

            // if the curEntry is not the firstEntry, get the old value of its fields
            if (prevEntry != null) {

                Employee prevEmp = (Employee)prevEntry.getEntryObject();
                oldFirstName = prevEmp.getFirstName();
                oldLastName = prevEmp.getLastName();
                oldEmail = prevEmp.getEmail();
            }

            Employee curEmp = (Employee)curEntry.getEntryObject();
            String newFirstName = curEmp.getFirstName();
            String newLastName = curEmp.getLastName();
            String newEmail = curEmp.getEmail();

            try {
                ps = conn.prepareStatement("INSERT INTO employeeLog(lsn, txid, newFirstName, oldFirstName, " +
                        "newLastName, oldLastName, newEmail, oldEmail) VALUES (?,?,?,?,?,?,?,?)");

                ps.setInt(1, lsn);
                ps.setInt(2, txid);
                ps.setString(3, newFirstName);
                ps.setString(4, oldFirstName);
                ps.setString(5, newLastName);
                ps.setString(6, oldLastName);
                ps.setString(7, newEmail);
                ps.setString(8, oldEmail);

                int count = ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DataSourceUtils.releaseConnection(conn, ds);
            }
        }
    }*/

}
