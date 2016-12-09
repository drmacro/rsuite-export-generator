# rsuite-export-generator
Code to generate arbitrarily-large fake export data sets to test import scale

To make the jar from the command line:

```
mvn clean compile assembly:single
```

To run the generator locally during develop do:

```
java -jar target/export-generator-1.0-SNAPSHOT-jar-with-dependencies.jar src/main/java/templates/generation.properties
```