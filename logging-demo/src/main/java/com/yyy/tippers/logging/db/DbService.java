package com.yyy.tippers.logging.db;

import com.gemstone.gemfire.cache.Region;
import com.yyy.tippers.logging.geode.TransactionRepository;
import com.yyy.tippers.logging.utils.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    * batch insert, if error happens during execution, rollback
    * */
    public int saveLogToDb(Transaction tx) {
        Connection conn = DataSourceUtils.getConnection(ds);
        PreparedStatement ps = null;

        int result = 0;

        String sql = "INSERT INTO logTable(lsn, txid, time_stamp, log_type, payload_type, payload, payload_binary) " +
                "VALUES (?,?,?,?,?,?,?)";

        try {
            int count = 0;
            conn.setAutoCommit(false); // turn off autocommit

            ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

            while (tx != null) {

                // for test, create an error
                /*count++;
                if (count == 2) {
                    int temp = 1/0;
                }*/

                String payload_type = tx.getPayload_type();

                ps.setInt(1, tx.getLsn());
                ps.setInt(2, tx.getTxid());
                ps.setInt(3, tx.getTime_stamp());
                ps.setString(4, tx.getLog_type());
                ps.setString(5, payload_type);

                if (payload_type.equals("Binary")) {

                    byte[] payload_bytes = tx.getPayload().getBytes();
                    ByteArrayInputStream byteIS = new ByteArrayInputStream(payload_bytes);

                    ps.setString(6, null);
                    ps.setBinaryStream(7, byteIS, payload_bytes.length);

                }else if (payload_type.equals("XML")){
                    ps.setString(6, tx.getPayload());
                    ps.setBinaryStream(7, null);

                }else if (payload_type.equals("JSON")){
                    // TODO: JSON
                    ps.setString(6, tx.getPayload());
                    ps.setBinaryStream(7, null);

                }else if (payload_type.equals("Plaintxt")){
                    ps.setString(6, tx.getPayload());
                    ps.setBinaryStream(7, null);

                }
                ps.addBatch();
                tx = tx.getPrev();
            }

            ps.executeBatch();
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rollback!!!");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    System.out.println("System error!!!");
                    return -1;
                } finally {
                    DataSourceUtils.releaseConnection(conn, ds);
                }
            }
            return -1;

        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, ds);
            }
        }
        return result;
    }

    public Transaction getTxByLsnInMysql(int lsn) {
        Connection conn = DataSourceUtils.getConnection(ds);

        Transaction tx = null;

        try {
            PreparedStatement ps = conn.prepareStatement("select txid, time_stamp, log_type, payload_type, payload, payload_binary from logTable where lsn=?");

            ps.setInt(1, lsn);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                tx = new Transaction();
                tx.setLsn(lsn);
                tx.setTxid(rs.getInt("txid"));
                tx.setTime_stamp(rs.getInt("time_stamp"));
                tx.setLog_type(rs.getString("log_type"));

                tx.setPayload_type(rs.getString("payload_type"));

                if (tx.getPayload_type().equals("XML")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("JSON")) {
                    // TODO: JSON
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Plaintxt")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Binary")) {
                    InputStream is = rs.getBinaryStream(rs.getString("payload_binary"));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = -1;

                    try {
                        while((i = is.read())!=-1){
                            baos.write(i);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tx.setPayload(baos.toString());
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }
        return tx;
    }

    public List<Transaction> getTxByTxidInMysql(int txid) {
        List<Transaction> txList = new ArrayList<Transaction>();

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("select lsn, time_stamp, log_type, payload_type, payload, payload_binary from logTable where txid=?");

            ps.setInt(1, txid);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setLsn(rs.getInt("lsn"));
                tx.setTxid(txid);
                tx.setTime_stamp(rs.getInt("time_stamp"));
                tx.setLog_type(rs.getString("log_type"));

                tx.setPayload_type(rs.getString("payload_type"));

                if (tx.getPayload_type().equals("XML")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("JSON")) {
                    // TODO: JSON
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Plaintxt")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Binary")) {
                    InputStream is = rs.getBinaryStream(rs.getString("payload_binary"));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = -1;

                    try {
                        while((i = is.read())!=-1){
                            baos.write(i);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tx.setPayload(baos.toString());
                }

                txList.add(tx);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return txList;
    }

    public List<Transaction> getTxByTimeIntervalInMysql(int beginTime, int endTime) {
        List<Transaction> txList = new ArrayList<Transaction>();

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("select lsn, txid, time_stamp, log_type, payload_type, payload, " +
                    "payload_binary from logTable where time_stamp >= ? AND time_stamp <= ?");

            ps.setInt(1, beginTime);
            ps.setInt(2, endTime);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setLsn(rs.getInt("lsn"));
                tx.setTxid(rs.getInt("txid"));
                tx.setTime_stamp(rs.getInt("time_stamp"));
                tx.setLog_type(rs.getString("log_type"));

                tx.setPayload_type(rs.getString("payload_type"));

                if (tx.getPayload_type().equals("XML")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("JSON")) {
                    // TODO: JSON
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Plaintxt")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Binary")) {
                    InputStream is = rs.getBinaryStream(rs.getString("payload_binary"));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = -1;

                    try {
                        while((i = is.read())!=-1){
                            baos.write(i);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tx.setPayload(baos.toString());
                }

                txList.add(tx);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return txList;
    }

    public List<Transaction> getTxByLogTypeInMysql(String log_type) {
        List<Transaction> txList = new ArrayList<Transaction>();

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("select lsn, txid, time_stamp, payload_type, payload, " +
                    "payload_binary from logTable where log_type = ?");

            ps.setString(1, log_type);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setLsn(rs.getInt("lsn"));
                tx.setTxid(rs.getInt("txid"));
                tx.setTime_stamp(rs.getInt("time_stamp"));
                tx.setLog_type(log_type);

                tx.setPayload_type(rs.getString("payload_type"));

                if (tx.getPayload_type().equals("XML")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("JSON")) {
                    // TODO: JSON
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Plaintxt")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Binary")) {
                    InputStream is = rs.getBinaryStream(rs.getString("payload_binary"));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = -1;

                    try {
                        while((i = is.read())!=-1){
                            baos.write(i);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tx.setPayload(baos.toString());
                }

                txList.add(tx);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return txList;
    }

    public List<Transaction> getTxByPayloadInMysql(String queryPara) {
        List<Transaction> txList = new ArrayList<Transaction>();

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("select lsn, txid, time_stamp, log_type, payload_type, payload, " +
                    "payload_binary from logTable where payload LIKE ?");

            //String queryPara = "%" + className + "%<" + attribute + ">" + value + "</" + attribute + ">%";
            ps.setString(1, queryPara);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setLsn(rs.getInt("lsn"));
                tx.setTxid(rs.getInt("txid"));
                tx.setTime_stamp(rs.getInt("time_stamp"));
                tx.setLog_type(rs.getString("log_type"));

                tx.setPayload_type(rs.getString("payload_type"));

                if (tx.getPayload_type().equals("XML")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("JSON")) {
                    // TODO: JSON
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Plaintxt")) {
                    tx.setPayload(rs.getString("payload"));

                }else if (tx.getPayload_type().equals("Binary")) {
                    InputStream is = rs.getBinaryStream(rs.getString("payload_binary"));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = -1;

                    try {
                        while((i = is.read())!=-1){
                            baos.write(i);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tx.setPayload(baos.toString());
                }

                txList.add(tx);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return txList;
    }

    public int deleteLogByLsnInMysql(int lsn) {
        int count = 0;

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE from logTable where lsn = ?");

            ps.setInt(1, lsn);

            count = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return count;
    }

    public int deleteLogsByTxidInMysql(int txid) {
        int count = 0;

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE from logTable where txid = ?");

            ps.setInt(1, txid);

            count = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return count;
    }

    public int deleteLogsByTimeIntervalInMysql(int beginTime, int endTime) {
        int count = 0;

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE from logTable where time_stamp >= ? and time_stamp <= ?");

            ps.setInt(1, beginTime);
            ps.setInt(2, endTime);

            count = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return count;
    }

    public int deleteLogsByLogType(String log_type) {
        int count = 0;

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE from logTable where log_type = ?");

            ps.setString(1, log_type);

            count = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return count;
    }

    public int deleteLogsByPayload(String queryPara) {
        int count = 0;

        Connection conn = DataSourceUtils.getConnection(ds);

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE from logTable where payload LIKE ?");

            ps.setString(1, queryPara);

            count = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DataSourceUtils.releaseConnection(conn, ds);
        }

        return count;
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
