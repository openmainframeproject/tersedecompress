# TerseDecompress

TerseDecompress decompresses files that were compressed on an IBM mainframe
with the TERSE / AMATERSE program (IBM z/OS or IBM z/VM). It lets you read
tersed mainframe files on any workstation, without access to a mainframe.

TerseDecompress is an [Open Mainframe Project](https://openmainframeproject.org/)
project. Two implementations are maintained in this repository:

| Implementation | Location | Runs on | Executable |
|---|---|---|---|
| Java | [`src/`](src/) | any platform with Java 21 or later | `tersedecompress.jar` |
| C++ | [`cpp/`](cpp/) | Linux, macOS, z/OS UNIX | `tersedecompress++` |

Both decode the same formats and produce the same output.

## Supported input

- PACK and SPACK compressed files
- Fixed (F/FB) and variable (V/VB) record formats
- Sequential data sets and PDS/PDSE
- Native binary mode files (4-byte header)

## Install

Each [GitHub release](https://github.com/openmainframeproject/tersedecompress/releases)
includes a prebuilt `tersedecompress.jar`, and C++ binaries for some platforms.
To build from source, see [Build](#build).

## Usage

### Java

```sh
java -jar tersedecompress.jar [-b] [-z] <input file> [<output file>]
```

### C++

```sh
tersedecompress++ [-b] [-z] <input file> [<output file>]
```

### Options

| Option | Java | C++ | Meaning |
|---|---|---|---|
| `-b` | yes | yes | Binary mode: no EBCDIC to ASCII conversion |
| `-z` | yes | yes | Write the output gzip-compressed |
| `-h` | yes (also `--help`) | yes | Print help |
| `-v`, `--version` | yes | no | Print the version and commit |

**Text mode** is the default. Each record is converted from EBCDIC to ASCII
and ends with a newline.

**Binary mode** (`-b`) writes the bytes unchanged. Variable-length records
are prefixed with a 4-byte RDW, the same format IBM uses: a 2-byte big-endian
length that includes the RDW itself, followed by 2 zero bytes.

**Use binary mode if you can.** EBCDIC to ASCII conversion is lossless only
when the data uses characters common to both code pages and contains no
line-ending characters.

### Default output file name

If you leave out `<output file>`:

| Mode | Java | C++ |
|---|---|---|
| Text | `<input file>.txt` | `<input file>.txt` |
| Binary | `<input file>.bin` | error: an output file is required |

With `-z`, `.gz` is appended (for example, `<input file>.txt.gz`).

### Examples

```sh
# Text file, output to MY.DATA.TRS.txt
java -jar tersedecompress.jar MY.DATA.TRS

# Binary data, keep the original bytes and RDWs
java -jar tersedecompress.jar -b MY.LOAD.TRS my.load.bin

# Text output, gzip-compressed
tersedecompress++ -z MY.DATA.TRS my.data.txt.gz
```

## Build

### Java

Requirements: JDK 21 or later and Apache Maven.

```sh
mvn clean package
```

The jar is written to `target/tersedecompress.jar`. It has no runtime
dependencies.

### C++

Requirements: a C++17 compiler, GNU make and zlib. The tests also need
GoogleTest.

On Linux and macOS the Makefile uses `clang++`. On Debian or Ubuntu:

```sh
sudo apt-get install clang make zlib1g-dev libgtest-dev
cd cpp
make
```

The binary is written to `cpp/bin/tersedecompress++`.

If GoogleTest is not installed system-wide, the Makefile also looks in
`$(PREFIX)/include` and `$(PREFIX)/lib` (`PREFIX` defaults to `$HOME/local`).

On z/OS, build from z/OS UNIX with IBM Open XL C/C++ (`ibm-clang++`). The
Makefile selects the z/OS settings automatically and links the system zlib
(`/lib/libzz.a`).

## Test

Both test suites use a test corpus kept in a separate repository,
[tersedecompress-testdata](https://github.com/openmainframeproject/tersedecompress-testdata),
and referenced as the `test-data` submodule. You don't need it to build,
only to run the tests. Descriptions of the data are in its
[`tests`](https://github.com/openmainframeproject/tersedecompress-testdata/tree/master/tests)
folder.

```sh
git submodule update --init
```

Java: `mvn clean package` runs the tests when `test-data` is checked out,
and skips them when it isn't. To make a missing submodule fail the build
instead, add `-DskipTests=false`.

```sh
mvn clean package
```

C++:

```sh
cd cpp
make check
```

Code-quality tooling (compiler warnings, SpotBugs, cppcheck) is described in
[QUALITY.md](QUALITY.md).

## Version history

**Version 5.1 (in development)**

- `-z` option to write gzip-compressed output (Java and C++)
- Fix for large input files in binary mode; records longer than the maximum
  record length are now reported as errors
- C++ CI on GitHub Actions
- Compiler warnings and static analysis (see [QUALITY.md](QUALITY.md))

**Version 5.0.1: July 2023 to September 2025**

- Maven compiles with Java 21
- In text mode, the output file name is optional and defaults to `<input file>.txt`
- Added the C++ implementation
- Added the commit id to the version

**Version 5: March 2021**

- Support for variable-length binary records, prefixed with an RDW (see
  [Options](#options))

## Help and contributing

- Questions and bug reports: see [SUPPORT.md](SUPPORT.md)
- Security vulnerabilities: see [SECURITY.md](SECURITY.md). Don't report them in public issues.
- Contributing: see [CONTRIBUTING.md](CONTRIBUTING.md)
- Project governance: see [GOVERNANCE.md](GOVERNANCE.md) and [COMMITTERS.md](COMMITTERS.md)

## License

[Apache License 2.0](LICENSE)
