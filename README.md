# rsuite-export-generator Version 1.0

Code to generate arbitrarily-large fake export data sets to test import scale for the RSuite 3.7 importer.

Generates a arbitrarily-large set of randomly-generated XML and (eventually) non-XML MOs with random but realistic content (using randomly-selected words from the linux master English words.txt file) and a set of randomly-generated containers with references to the generated MOs.

## Running

To run the generator do:

```
java -jar export-generator-1.0.jar {path to generation.properties file}
```

The generation.properties file defines the details for the generation:

```
outdir=/Users/ekimber/temp/export/
browseWidth=10
browseDepth=5
maxContainerChildren=100
maxXmlMOs=10000
maxBinaryMOs=10000
maxVersions=5
```

Each numeric parameter defines the limit on that aspect. Actual values are randomly generated between some appropriate minimum and the maximum. 

Currently the generation only produces DITA topics and no non-XML MOs but it has been architected to make it relatively easy to extend for other file types or non-DITA output.

In this version the XML details are in the DITA code but it probably makes more sense to use an XSLT-based template-driven approach to generate the XML content, making it easier to customize for specific kinds of data.

## Developing:

To make the jar from the command line:

```
mvn clean compile assembly:single
```

