# Architecture

This document describes how TerseDecompress is built, who and what it
interacts with, and its security assessment.

## Actors and actions

TerseDecompress is a command-line program. It has no server, no network
access, no configuration files and no persistent state.

| Actor | Actions |
|---|---|
| User | Runs `tersedecompress.jar` or `tersedecompress++` with options, an input file and, optionally, an output file name. Reads messages on standard output and standard error, and the exit status. |
| Input file | A TERSE-compressed file produced on z/OS or z/VM, read once from start to end. Treated as untrusted. |
| Output file | Created or overwritten with the decompressed data, optionally gzip-compressed. |
| Operating system | Provides file access with the user's own permissions. The program needs no extra privileges. |

## Components

Java (`src/main/java/org/openmainframeproject/tersedecompress`) and C++
(`cpp/src`) implement the same design with the same class names:

| Component | Java | C++ | Role |
|---|---|---|---|
| Command line | `TerseDecompress` | `main.cpp`, `argumentParser.cpp` | Parses options, chooses the output file name, opens the files, reports errors and sets the exit status. |
| Header parser | `TerseHeader` | `TerseHeader.cpp` | Reads and validates the TERSE header: format (PACK, SPACK or native binary), record format, record length and block size. |
| Block reader | `TerseBlockReader` | `TerseBlockReader.cpp` | Reads the input as a stream of 12-bit codes. |
| Decoder factory | `TerseDecompresser` | `TerseDecompresser.cpp` | Creates the PACK or SPACK decoder for the header. Builds records, converts EBCDIC to ASCII in text mode, adds RDWs for variable records in binary mode, and writes the output, through gzip with `-z`. |
| PACK decoder | `NonSpackDecompresser` | `NonSpackDecompresser.cpp` | Decodes PACK (LZW-style) data. |
| SPACK decoder | `SpackDecompresser` | `SpackDecompresser.cpp` | Decodes SPACK data using a fixed-size tree of 4096 nodes with LRU reuse and a fixed-size output stack. |

## Data flow

```
input file ──► header parser ──► block reader ──► PACK or SPACK decoder
                                                     │
                                                     ▼
                                   record builder (text: EBCDIC→ASCII + newline,
                                                   binary: bytes + RDW)
                                                     │
                                                     ▼
                                   output file (plain or gzip)
```

The input is processed as a stream. Memory use doesn't depend on the input
size: the decoder tables have a fixed size, and a record buffer is limited
to the record length from the header, or to 32767 bytes for variable-length
and native binary records.

## External interfaces

The released artifacts, `tersedecompress.jar` and `tersedecompress++`,
have one external interface: the command line. Its options, default output
file names and exit status are documented in [README.md](README.md#usage).

| Interface | Details |
|---|---|
| Arguments | `[-b] [-z] <input file> [<output file>]`, plus `-h` and, in Java, `-v` |
| Input | One file, read only |
| Output | One file, created or overwritten |
| Standard output and error | Progress and error messages |
| Exit status | 0 on success, 1 on error |

Runtime dependencies: a Java 21 or later runtime for the jar; zlib for the
C++ binary.

## Threat model

### Trust boundary

The user and the operating system are trusted. The input file is not:
anyone who can give the user a file controls every byte the decoder reads.
Command-line arguments come from the user, who already has the same
permissions as the program, so they don't cross a trust boundary.

### Attack surface

| Entry point | Attacker controls | Critical code path |
|---|---|---|
| TERSE header (first 4 or 12 bytes) | Format, record format flag, record lengths, block size, MVS flags | `TerseHeader`: picks the decoder and sets the record buffer limit |
| Compressed stream | Every 12-bit code, and where the file ends | `TerseBlockReader`, then `NonSpackDecompresser` (PACK dictionary) or `SpackDecompresser` (tree, LRU list, output stack) |
| Decoded records | Literal values, record marks, record lengths | `TerseDecompresser::putChar` / `endRecord`: record buffer, EBCDIC table lookup, RDW, gzip output |

There is no network interface, no configuration file, no environment
variable that changes behaviour, and no file name taken from the input.

### Threats (STRIDE)

| Threat | Applies? | Mitigation |
|---|---|---|
| Spoofing | No | No identities or authentication. |
| Tampering with the output | Limited | Only the output file chosen on the command line is written. A tampered input yields wrong data, the same as a tampered original. TERSE has no checksum, so this can't be detected. |
| Repudiation | No | Single-user local tool. |
| Information disclosure | Low | Input-derived bytes go only to the output file. Out-of-bounds reads that could leak memory into the output are blocked by bounds checks on codes, tree indexes, records and the EBCDIC table. |
| Denial of service | Yes | Fixed-size decoder tables, a bounded record buffer and a bounded SPACK stack, so memory use doesn't depend on the input. Truncated input is an error. |
| Elevation of privilege | Yes (C++) | Memory corruption in the C++ decoder could lead to code execution with the user's permissions. Mitigated by validation and bounds checks on every attacker-controlled index, and by sanitizer testing. |

## Security assessment

This is the assessment for release 5.1.

### Most likely and most serious problems

The only untrusted data the program handles is the content of the input
file. Users often decompress files that someone else has sent them, so a
malformed or malicious TERSE file is the most likely way to attack it. The
possible effects are:

1. **Memory corruption in the C++ implementation** (out-of-bounds reads or
   writes from corrupt header fields, codes or tree links). This is the most
   serious, because it could allow code execution.
2. **Denial of service**: unbounded memory use or a decoder that never ends.
3. **Wrong output without an error**: corrupt data written as if it were
   valid.
4. **Writing outside the output file the user chose.**

### How they are addressed in 5.1

- The header is validated against the TERSE format and rejected if the
  version, record format flag, record length or MVS fields are invalid.
- Every 12-bit code is range-checked before use. Invalid literals, negative
  codes and references outside the SPACK tree stop decoding with an error.
- The SPACK output stack is bounded (commit 66949cd). Corrupt input stops
  with an error instead of writing past the stack in C++ or throwing
  `ArrayIndexOutOfBoundsException` in Java.
- Records can't grow beyond the record length. A huge record length in the
  header doesn't cause a large allocation.
- Truncated input and read errors are reported as errors, not treated as a
  normal end of file.
- Every failure exits with status 1, so scripts can detect it.
- The program only writes to the output file named on the command line, or
  to the documented default name next to the input file. It never builds a
  file name from the input data.
- The 5.1 C++ test suite (118 tests) passes under AddressSanitizer and
  UndefinedBehaviorSanitizer with no findings.

### Known limitations

- An existing output file is overwritten without asking.
- If decoding fails, a partial output file is left behind.
- Checks for compiler warnings and static analysis ([QUALITY.md](QUALITY.md))
  are best-effort and don't block a merge.

To report a vulnerability, see [SECURITY.md](SECURITY.md).
