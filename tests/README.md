# Testing TerseDecompress #

TerseDecompress has unit tests defined in the Maven project.

The tests are skipped by default, and the test data is in a submodule repository due to the size of the test data files. This means that TerseDecompress can be built without downloading all the test data.

### To run the TerseDecompress tests: ###

1. Initialize (download) the submodule containing the test data:
```git submodule update --init```
2. Build with unit tests:
```mvn -DskipTests=false clean package```
