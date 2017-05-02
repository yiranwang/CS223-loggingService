1. run maven generated jar in cml
java -cp /target/some.jar com.company.groupid.artifactid.MainClass

2. maven stores dependencies @ ~/.m2/

3. please refer to pom file and see how a dependencies-included jar file can be produced. specifically, > mvn clean compile assembly:single
 
