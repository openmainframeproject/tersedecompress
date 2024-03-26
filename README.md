# tersedecompress

TerseDecompress is a java program that can be used to decompress files
which have been previously compressed on an IBM Mainframe
using the TERSE / AMATERSE program (on IBM z/OS or IBM z/VM).

## Purpose & benefit ##

As java programs can virtually run on any platform / operating system
with a JVM, this java decompression program can be very useful if you
don't have access to an IBM mainframe but need to analyze or process a
file which had been compressed on a IBM mainframe using TERSE on IBM z/OS.
With this java version of TerseDecompress, you can decompress those tersed
IBM Mainframe files on any workstation or laptop etc. that supports Java.

## Updates ##

**Version 6: March 2024**

- Added support to decompress then recompress into a gzip file if output-file name ends in ".gz"
- Updated pom.xml to have Maven compile using Java 17, and shade the jar.

**Version 5: March 2021**

- Support for variable length binary records. Variable length records processed in binary mode will be prefixed with a 4 byte field in the same format as the IBM RDW i.e. 2 byte record length field (including RDW length, big-endian) followed by 2 bytes of zeros.

## How to run it ##

For execution, TerseDecompress needs a JVM runtime environment.

Usage:

```java -jar tersedecompress-6.0.0.jar [-b] tersed-file output-file```

Default mode is text mode, which will attempt EBCDIC -> ASCII conversion.

The **-b** flag turns on binary mode, no conversion will be attempted.

**Recommendation:** Use binary mode if possible. EBCDIC->ASCII conversion is not a lossless process, unless the data strictly contains only characters that are common to both code pages used for the translation, **and** the original data does not contain line ending characters.  

## How to build it ##

To build tersedecompress you need the Java JDK and Apache Maven.

In the project directory (the directory containing **pom.xml**):

```mvn clean package```

## Unit Tests ##

The project contains unit tests to verify that the decompression is correct.

Due to the size of the test data, it is stored in a separate git repository and referenced via a submodule. The test data is not required to build tersedecompress, unless you want to run the unit tests.

The test data can be found here:
[https://github.com/openmainframeproject/tersedecompress-testdata](https://github.com/openmainframeproject/tersedecompress-testdata)

Descriptions of the data are here:
[https://github.com/openmainframeproject/tersedecompress-testdata/tree/master/tests](https://github.com/openmainframeproject/tersedecompress-testdata/tree/master/tests)


### Building with Unit Tests ###

1. Initialize (download) the submodule containing the test data:
```git submodule update --init```
2. Build with unit tests:
```mvn -DskipTests=false clean package```

## How to report problems and get support/help ##

If you have a problem / need help with TerseDecompress.java, please create a GitHub issue.

There is also a Slack channel: [https://slack.openmainframeproject.org](https://slack.openmainframeproject.org) channel #tersedecompress

