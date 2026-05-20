# TerseDecompress

TerseDecompress is a tool to decompress files that have been compressed on an IBM Mainframe using the TERSE / AMATERSE program (on IBM z/OS or IBM z/VM).

It provides both a Java implementation and a C++ port, allowing for decompression on virtually any platform.

## Purpose & Benefit

IBM Mainframe files compressed with TERSE often need to be analyzed or processed on other platforms. TerseDecompress allows you to decompress these files on any workstation, laptop, or server that supports Java or C++, eliminating the need for mainframe access for this specific task.

## Features

- Supports both PACK (standard) and SPACK (enhanced) compression formats.
- Supports fixed-length and variable-length records.
- Text mode with EBCDIC to ASCII conversion.
- Binary mode for lossless decompression of non-text data.
- Optional GZIP compression of the output.
- Available as a CLI tool and as a library for both Java and C++.

---

## Version History

**Version 5.0.1: July 2023 -- September 2025**

- Updated pom.xml to have Maven compile using Java 21
- Allow user to specify only `<input file>` for text files. Resulting `<output file>` will be `<input file>.txt`
- Added C++ port
- Added commit id to the version

**Version 5: March 2021**

- Support for variable length binary records. Variable length records processed in binary mode will be prefixed with a 4 byte field in the same format as the IBM RDW i.e. 2 byte record length field (including RDW length, big-endian) followed by 2 bytes of zeros.

---

## Installation

### Java

**Prerequisites:**
- [OpenJDK](https://openjdk.org/) (Version 21 or later)
- [Apache Maven](https://maven.apache.org/)

**Building:**
1. Navigate to the project root directory (containing `pom.xml`).
2. Run:
   ```bash
   mvn clean package
   ```
3. The compiled JAR will be located at `target/tersedecompress.jar`.

### C++

**Prerequisites:**
- An open-source C++ compiler (e.g., [GCC](https://gcc.gnu.org/) or [Clang](https://clang.llvm.org/)) supporting C++17.
- [GNU Make](https://www.gnu.org/software/make/)
- [zlib](https://zlib.net/) development headers.

**Building:**
1. Navigate to the `cpp/` directory.
2. Run:
   ```bash
   make
   ```
3. The executable will be located at `cpp/src/tersedecompress`.
4. A static library `libtersedecompress.a` is also produced in `cpp/src/`.

---

## Unit Tests

The project contains unit tests to verify that the decompression is correct.

Due to the size of the test data, it is stored in a separate git repository and referenced via a submodule. The test data is not required to build tersedecompress, unless you want to run the unit tests.

The test data can be found here:
[https://github.com/openmainframeproject/tersedecompress-testdata](https://github.com/openmainframeproject/tersedecompress-testdata)

Descriptions of the data are here:
[https://github.com/openmainframeproject/tersedecompress-testdata/tree/master/tests](https://github.com/openmainframeproject/tersedecompress-testdata/tree/master/tests)

### Building with Unit Tests

1. Initialize (download) the submodule containing the test data:
   ```bash
   git submodule update --init
   ```
2. Build with unit tests:
   ```bash
   mvn -DskipTests=false clean package
   ```

---

## CLI Usage Reference

### Java CLI Synopsis

```bash
java -jar tersedecompress.jar [options] <input_file> [output_file]
```

**Options:**
- `-b`: Enable binary mode. No EBCDIC -> ASCII conversion is performed.
- `-z`: Enable GZIP compression for the output file.
- `-h`, `--help`: Show help message.
- `-v`, `--version`: Show version information.

**Arguments:**
- `<input_file>`: Path to the tersed input file (required).
- `[output_file]`: Path to the output file (optional).
  - In text mode (default), defaults to `<input_file>.txt`.
  - In binary mode (`-b`), defaults to `<input_file>.bin`.
  - If `-z` is used, `.gz` is appended.

### C++ CLI Synopsis

```bash
./tersedecompress [options] <input_file> [output_file]
```

**Options:**
- `-b`: Enable binary mode. No EBCDIC -> ASCII conversion is performed.
- `-z`: Enable GZIP compression for the output file.
- `-h`: Show help message.

**Arguments:**
- `<input_file>`: Path to the tersed input file (required).
- `[output_file]`: Path to the output file (optional). Defaults are similar to the Java version.

### Examples

**Decompress a text file:**
```bash
java -jar tersedecompress.jar mydata.tersed
```

**Decompress a binary file to a specific location:**
```bash
./tersedecompress -b mydata.tersed mydata.bin
```

**Decompress and GZIP the output:**
```bash
java -jar tersedecompress.jar -z mydata.tersed
```

---

## API Reference

### Java API

The primary entry point is `org.openmainframeproject.tersedecompress.TerseDecompresser`.

```java
import org.openmainframeproject.tersedecompress.TerseDecompresser;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

try (InputStream in = new FileInputStream("input.tersed");
     OutputStream out = new FileOutputStream("output.txt");
     TerseDecompresser decompresser = TerseDecompresser.create(in, out, true)) { // true for text mode
    decompresser.decode();
} catch (Exception e) {
    // Handle IOException or other decompression errors
    e.printStackTrace();
}
```

- **Entry Point:** `TerseDecompresser.create(InputStream, OutputStream, boolean textMode)`
- **Error Behavior:** Throws `IOException` if the input is malformed, the header is invalid, or if I/O errors occur.

### C++ API

The primary interface is defined in `cpp/src/TerseDecompresser.h`.

```cpp
#include "TerseDecompresser.h"
#include <fstream>
#include <iostream>

try {
    std::ifstream in("input.tersed", std::ios::binary);
    std::ofstream out("output.txt", std::ios::binary);

    DecompresserOptions options;
    options.textMode = true;

    auto decompresser = TerseDecompresser::create(in, out, options);
    decompresser->decode();
} catch (const std::exception &e) {
    // Handle decompression errors
    std::cerr << "Error: " << e.what() << std::endl;
}
```

- **Headers:** Include `TerseDecompresser.h`.
- **Main Class:** `TerseDecompresser`.
- **Usage:** Use `TerseDecompresser::create` to get a `unique_ptr` to a decompresser instance, then call `decode()`.
- **Error Behavior:** Throws `std::runtime_error` or other `std::exception` subclasses on failure (e.g., invalid header, unsupported format, or I/O errors).

---

## Security Guidance

- **Untrusted Input:** Treat Terse files from untrusted sources with caution. While basic validation is performed, malformed files could potentially lead to high resource usage.
- **Resource Usage:** Decompression can be CPU and memory intensive. Ensure the environment has sufficient resources for the expected file sizes.
- **Binary Mode:** Always use binary mode (`-b`) for non-text data (e.g., load modules, database backups) to avoid data corruption during EBCDIC to ASCII conversion.
- **Privileges:** Run with the minimum necessary privileges. The tool only requires read access to the input and write access to the output destination.

---

## Development & Support

- **Continuous Development:** Development happens continuously in this public repository. Commits and PRs occur between formal releases. Release tags represent stable snapshots.
- **Reporting Issues:** Please report bugs or request features via [GitHub Issues](https://github.com/openmainframeproject/tersedecompress/issues).
- **Getting Help:** See [SUPPORT.md](SUPPORT.md) for more details.
- **Release Process:** See [RELEASE.md](RELEASE.md) for information on versioning and release cadence.
- **Contributing:** See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## FLOSS Build Tooling

This project is built using only Free/Libre and Open Source Software (FLOSS) tools:
- **Java:** OpenJDK and Apache Maven.
- **C++:** Open-source compilers (GCC, Clang) and GNU Make.

---

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

