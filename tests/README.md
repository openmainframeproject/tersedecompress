# Testing TerseDecompress #

TerseDecompress has unit tests defined in the Maven project.

The test data is in a submodule repository due to the size of the test data files. The tests run
when the submodule is checked out and are skipped when it isn't, so TerseDecompress can be built
without downloading the test data.

### To run the TerseDecompress tests: ###

1. Initialize (download) the submodule containing the test data:
```git submodule update --init```
2. Build with unit tests:
```mvn clean package```

To make a missing submodule fail the build instead of skipping the tests, add `-DskipTests=false`.
