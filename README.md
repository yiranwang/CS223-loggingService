# CS223-loggingService


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
