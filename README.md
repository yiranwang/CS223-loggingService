# CS223-loggingService


## Proposal

__Team: YYY__

Yue Ding  20809476  
Shengnan Wang 83682456  
Shayang Zang  36326408  

__Goal:__

This project will implement a logging service accepting logging message in various formats, such as XML, JSON, Plain Text, Binary. We will provide our client with APIs that include “write log into memory”, “delete log” and “flush log onto disk”.

Specifically, we will use:  
1. Maven to manage the entire project.  
2. Google Guice to facilitate the building of dependency injection pattern so that different classes, depending on the format of the incoming logging message, can be dynamically loaded.
3. Apache Geode to manage data in memory.
4. MySQL/PostgreSQL to manage data on disk.

__Procedure Description:__

Client invoke logging handlers through our APIs, corresponding logging message files are first  converted to java beans and then stored in memory. Upon flush, they will go to database.

__Timeline:__

W4: Get ourselves familiar with what each framework/library/package does.  
W5: Start to build the logging service.  
W6: Finish the implementation such that our system can work with at least one payload format.  
W7: Extend it to accept various types of payload format.  


## Tentative APIs

__LastUpdate: 05032017__  

__"loggingService" is a privately attached member of the application.__

Clients initiate the logging of a transaction by calling: __txid = loggingService.newTransaction()__  

Input:  
* none  

Output:  
* txid : AtomicInteger : transaction ID   

_transaction IDs are a series of monotonically increasing numbers unique to each transaction. Most DBMS has SEQUENCE.nextval() that can generate such numbers._  

---

Clients write each log into memory by calling: __loggingService.writeLog(txid, content, format)__  

Input:  
* txid : AtomicInteger : transaction ID   
* content : STRING : logging content
* format : STRING : xml, json, plain text, binary

Current Output:  
* retcode : INT : 0 indicating success. 1 otherwise.

Potential Output:  
* lsn : INT : log sequential number

_maintain logs within the same transaction using doubly linkedlist._

---

Clients query log by calling: __loggingService.queryLog(txid)__  

Input:
* txid : AtomicInteger : transaction ID

Output:
* Specific output format hasn't been decided.

---

Clients flush the logging content from memory to disk by calling: __loggingService.flushLog(txid)__  

Input:
* txid : AtomicInteger : transaction ID

Output:
* retcode : INT : 0 indicating success. 1 otherwise.

---

## Development

### Where to start?

__LastUpdate: 04252017__  
1. How to generate unique transaction ID and log sequential number using DB.sequence.nextval() in java?
2. How to build JavaBeans from XML and XSD schema? How to apply Guice for dependency injection with multiple classes?
3. How to build JavaBeans from content of JSON, pt, bin format?
4. How to implement data transfer from memory to disk?


__LastUpdate: 04262017__    
1. transaction ID comes from on-disk DB and LSN comes from in-memory counter
2. WRITE input TO in-memory database and then FLUSH TO on disk database, do we still need a Transaction class to hold info?


---

### Dependency Injection Layout

__LastUpdate: 04262017__  

__src/../logging/Demo:__  
A "fake" application class that mimics the application behavior specific to calling logging services. A LoggingService instance is privately attached to emit different behaviors given different input in RUNTIME.  

__src/../logging/LoggingService:__  
A "central" class that defines APIs, such as newTransaction(), writeLog(), flushLog(), etc. To behave differently in response to different input in RUNTIME, a LoggingHandlerFactory instance is privately attached.  

__src/../logging/factory/LoggingHandlerFactory:__  
A "factory" class that defines the conditional logic of producing different handlers in RUNTIME so they can emit different behaviors. The mapping of different input to different output (handlers) is privately attached as an attribute here but remotely defined in HandlerGuiceModule.

__src/../logging/HandlerGuiceModule:__
A "configure" class whose instance holds "configuration" in terms of:  
1. Injectable: mapBinder, injecting mapping relations of different input to different handler classes, to LoggingHandlerFactory.
2. bind(HandlerFactory.class).to(LoggingHandlerFactory.class) so it can be compiled with HandlerFactory interface and executed with LoggingHandlerFactory instance at RUNTIME.

__src/../logging/factory/HandlerFactory:__  
An factory interface, defines unspecified factory-level behavior at COMPILE time.  

__src/../logging/factory/Handlerable:__
An handler interface, defines unspecified handler-level behavior at COMPILE time.  

__src/../logging/handlers/HandlerForXML:__
A "handler" class that implemets Handlerable with specific behavior tailored to a certain type of RUNTIME parameter.

__src/../logging/handlers/HandlerForJSON:__  
A "handler" class that implemets Handlerable with specific behavior tailored to a certain type of RUNTIME parameter.  

...


### DB

Yue Ding

### Handlers
Unmarshall XML to java class. Here, we use two xsds and two xmls(src/main/reources): one is emplyee and the other is shiporder.  
1. First delete the target/generated-sources/xjc folder. You will generate it later.  
2. in pom.xml, there is plugin of jaxb to generate the java class from the XSD(schema). Right click pom.xml-->maven-->Reimport. Then Right click pom.xml --> maven--> generate the sources and update folders. After these steps, in target/generated-sources/xjc, you will see the class folders: one is about the employee class, the other is about the shiporder class.  
3. Unmarshall the xml file using the generated data.  

### Geode Implementation Details fix:
shengnan的更新没有问题，改动的地方有：  
1. 全部的AtomicInteger 改成 Int，大概因为Geode不支持AtomicInteger查询，但既然整个method加了synchronize，就没有什么问题。
2. TransactionRepository里面findLargestTxid这个方法的执行细节改了。因为之前的方法Geode不支持。
3. 在DBService里面，完善了getNextTxid这个method的执行细节。如果Geode里面没有当前transaction，那么就创造一个空的transaction with txid being the max(mysql_largestID, geode_largestID).


### Transaction Object Interface

### Table Creation in Mysql
SQL (in schema: CS223-loggingService):
(1)Create Schema:
CREATE SCHEMA 'new_schema' ;
-----------------------------------------
(2)Create Table:
CREATE TABLE 'CS223-loggingService'.'logTable' (
  'lsn' INT NOT NULL,
  'txid' INT NOT NULL,
  'time_stamp' INT NULL,
  'log_type' VARCHAR(45) NULL,
  'payload_type' VARCHAR(45) NULL,
  'payload' LONGTEXT NULL,
  'payload_binary' BLOB NULL,
  PRIMARY KEY ('lsn'));



__LastUpdate: 05032017__  

``` java
// The following classes are defined in src/../loggingService/utils/

public class TransactionLog {}

TransactionLog transaction = new TransactionLog(AtomicInteger txid);
int lsn = transaction.append(Object entryObj);
AtomicInteger txid = transaction.getID();
TransactionEntry firstEntry = transaction.getFirstEntry();
TransactionEntry entry = transaction.getEntryNo(int lsn);

public class TransactionEntry {}

TransactionEntry nextEntry = entry.getNextEntry();
TransactionEntry prevEntry = entry.getPrevEntry();
Object logObj = entry.getEntryObject();

```

## Prepare

__LastUpdate: 04212017__  

### Concept

1. [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) __is a design pattern whose core principle is to separate behavior from dependency resolution.__ _For five-year-olds: When you go and get things out of the refrigerator for yourself, you can cause problems. You might leave the door open, you might get something Mommy or Daddy doesn't want you to have. You might even be looking for something we don't even have or which has expired.
What you should be doing is stating a need, "I need something to drink with lunch," and then we will make sure you have something when you sit down to eat._  


2. [JavaBeans](http://stackoverflow.com/questions/3295496/what-is-a-javabean-exactly?answertab=votes#tab-top) denotes a standard way to encapsulate info in a class. In this project, payload of different format, along with XSD, will be parsed (possibly by JAXB) into JavaBeans and then stored in a in-memory database.

3. [Payload](https://en.wikipedia.org/wiki/Payload_(computing)) is the actual intended message of the entire log message, which might also include the following info:
+ log level (fail, success)
+ transaction ID
+ client ip
+ timestamp
+ headers/metadata


### Framework

1. [Google Guice](https://en.wikipedia.org/wiki/Google_Guice) is a generic framework for dependency injection using Java annotations.

2. [Apache Geode](http://geode.apache.org/) is a in-memory database.

3. Java Reflection allows you to build your applications without necessarily compiling all the external dependencies into source code so that dependency injection can work with dynamic class loading properly.  
_A use-case is application frameworks and containers which typically use Class.forName(...) under the hood to dynamically load the classes for application-specific beans, servlets, and so on._  

4. [JAXB](https://en.wikipedia.org/wiki/Java_Architecture_for_XML_Binding) is a Java framework that allows mapping from XML to Java classes.  

5. Maven: project manager.

6. MySQL/PostgreSQL: on disk db.

### Tutorials

1. [Generate JAXB Java classes from XSD with maven-jaxb2-plugin AND Spring OXM JAXB Example](https://www.youtube.com/watch?v=0D-P2LzLJYQ)

2. [Injecting Single Interface Implementation with Google Guice](https://www.youtube.com/watch?v=wNclLOTxQjk&list=PLKiN3faYVq89TjVuba-F62_nKBpcFdOWz&index=2)
