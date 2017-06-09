package com.yyy.tippers.logging;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.yyy.tippers.logging.entity.Payload;
import com.yyy.tippers.logging.utils.Transaction;

import java.io.*;
import java.util.*;


/**
 This implements a demo of how our loggingService can be incorporated in tippers app
 */
public class Demo
{
    private final LoggingService loggingService; // make loggingService a private attribute of the "fake-tipper" application

    /*
      Upon initialization,
        LoggingService instance is automatically injected into the constructor,
        as well as binded with "loggingService" attribute.
    */

    @Inject
    public Demo(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /*
      choiceOfHandlerFor(String format) is a method that showcases dependency injection.
      Specifically, it starts the application with loggingService already initialized and
        takes in a String variable at the RUNTIME (when loggingService is running) and makes it behave differently.
     */

    private void start() {

        /*
        // test writelog and flush
        String path = "/Users/yiranwang/IdeaProjects/gs-accessing-data-gemfire/generateXML/observationXML";
        List<String> results = new ArrayList<String>();


        File[] files = new File(path).listFiles();

        int count = 0;
        String []  xmlLists = null;
        int txid = 0;
        int timestamp = 0;
        String type = "Observation";
        String format = "XML";
        Payload payload = null;
        int lsn = 0;

        for (File file : files) {
            if(count % 5 == 0){
                xmlLists = new String[5];
                xmlLists[count%5] = readFile(file.getAbsolutePath());
                txid = loggingService.newTransaction();
                timestamp = count;
                payload = new Payload();
                payload.setType(format);
                payload.setContent(xmlLists[0]);
                lsn = loggingService.writeLog(txid, timestamp, type, payload);
            }else{
                xmlLists[count % 5] = readFile(file.getAbsolutePath());
                timestamp = count;
                payload.setContent(xmlLists[count % 5]);
                lsn = loggingService.writeLog(txid, timestamp, type, payload);
                if(count % 5 == 4){
//                    every five xmls is a transaction and flush to my sql
                    loggingService.flushLog(lsn);
                }

            }
            count++;

        }


        // test query by txid (same with lsn, log_type, timeinterval) and payload
        List<Transaction> txList = loggingService.queryLogListByTxid(1);
        System.out.println("the number of logs with txid = 1 is: " + txList.size());
        for(int i = 0; i < txList.size(); i++){
            System.out.println(txList.get(i).getPayload());
        }

        List<Transaction> txList2 = loggingService.queryLogListByPayload("observation", "payload", "1de67d8c284c2400bca339080147b4edf0c59283");
        for(int i = 0; i < txList2.size(); i++){
            System.out.println(txList2.get(i).getPayload());
        }

        // test delete
        int count1 = loggingService.deleteLogsByTxid(1);
        System.out.println("the number of deleted logs with txid = 1 is: " + count1);
        */

        //test infra data and each transaction has 5 logs.

        String infraPath = "/Users/yiranwang/IdeaProjects/CS223-loggingService/other/sample_infra.txt";
        String firstTag = "<infra>";
        String newFirstTag = "<?xml version=\"1.0\"?>\n" +
                "<infra xmlns=\"http://www.infradata.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.infradata.com infra.xsd\">\n";
        ArrayList<String> infraList = getXmlList(infraPath, firstTag, newFirstTag);
        long startTime = System.nanoTime();
        testWriteAndFlush(infraList, "Infra", "XML");
        long endTime = System.nanoTime();
        long duration1 = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.

        // test sensor data and each transactio has 5 logs
        String sensorPath = "/Users/yiranwang/IdeaProjects/CS223-loggingService/other/sample_sensor.txt";
        firstTag = "<sensor>";
        newFirstTag = "<?xml version=\"1.0\"?>\n" +
                "<sensor xmlns=\"http://www.sensordata.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sensordata.com sensor.xsd\">\n";

        ArrayList<String> sensorList = getXmlList(sensorPath, firstTag, newFirstTag);
        startTime = System.nanoTime();
        testWriteAndFlush(sensorList, "Sensor", "XML");
        endTime = System.nanoTime();
        long duration2 = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.

        System.out.println("total time to flush all "+ infraList.size()+ " logs is: " + duration1 + " milliseconds");
        System.out.println("total time to flush all "+ sensorList.size()+ " logs is: " + duration2 + " milliseconds");






    }

    // read xml from files
    public ArrayList<String> getXmlList(String path, String firstTag, String newFirstTag){

        BufferedReader br = null;
        String line = "";
        int count = 0;
        ArrayList<String> xmlList = new ArrayList<String>();
        try {

            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(line.length() == 0){
                    continue;
                }
                // use comma as separator
                line = line.replace(firstTag, newFirstTag);
                xmlList.add(line);
                System.out.println(line);
                count++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return xmlList;

    }


    public void testWriteAndFlush(ArrayList<String> xmllist, String type, String format){
        int count = 0;
        int txid = 0;
        int timestamp = 0;
        Payload payload = null;
        int lsn = 0;

        for(String xmlContent : xmllist){
            if(count % 5 == 0){
                txid = loggingService.newTransaction();
                timestamp = count;
                payload = new Payload();
                payload.setType(format);
                payload.setContent(xmlContent);
                lsn = loggingService.writeLog(txid, timestamp, type, payload);
            }else{
                timestamp = count;
                payload.setContent(xmlContent);
                lsn = loggingService.writeLog(txid, timestamp, type, payload);
                if(count % 5 == 4){
//                    every five xmls is a transaction and flush to my sql
                    loggingService.flushLog(lsn);
                }

            }
            count++;


        }


    }

    public String readFile(String filename) {
        String result = "";
        try{
            InputStream is = new FileInputStream(filename);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null){
                sb.append(line);
                line = buf.readLine();
                result = sb.toString();
                //System.out.println("Contents : " + result);
            }
//            System.out.println("Contents : " + result);
            return result;

        }catch(IOException e){

            e.printStackTrace();
        }
        return result;

    }

    /*
      An Injector guice is created based on configurations defined in HandlerGuiceModule.java.
      When you try to get Demo instance out of the Injector guice, it will enable the instance to behave differently, given different input.
      Specifically, the demo instance, coming with "loggingService" attribute, which binds to "HandlerFactory". See details in LoggingService.java
     */

    public static void main( String[] args )
    {
        System.out.println( "Start..." );

        Injector guice = Guice.createInjector(new HandlerGuiceModule());

        Demo demo = guice.getInstance(Demo.class);

        demo.start();

        System.out.println( "End." );

    }
}
