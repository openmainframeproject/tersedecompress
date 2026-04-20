# Code Quality

This document describes the compiler warnings and static analysis tools enabled in
TerseDecompress.  All quality tooling is **best-effort** — findings are reported but
do not automatically block the build or the merge of a PR.

---

## Compiler Warnings

### Java

Java compiler lint warnings are enabled via the `-Xlint:all` flag in
[`pom.xml`](pom.xml).  The flag is passed through `maven-compiler-plugin`'s
`compilerArgs` section and applies to every `mvn compile` or `mvn package`
invocation.

The build **will not fail** on warnings; they are surfaced as informational
messages in the Maven output.

### C++

Sane diagnostic flags are enabled in [`cpp/envdef.mak`](cpp/envdef.mak):

| Flag | Purpose |
|------|---------|
| `-Wall` | Enable commonly-used warnings |
| `-Wextra` | Enable extra warnings not covered by `-Wall` |
| `-Wpedantic` | Enforce strict ISO C++ conformance |

These flags apply to both the Linux/macOS (`clang++`) and IBM z/OS
(`ibm-clang++`) toolchains.  Warnings are **not** treated as errors
(`-Werror` is intentionally omitted).

---

## Static Analysis

### Java — SpotBugs

[SpotBugs](https://spotbugs.github.io/) is configured as a Maven plugin in
[`pom.xml`](pom.xml).

**Run locally:**

```bash
mvn spotbugs:check
```

Or to open the interactive GUI report:

```bash
mvn spotbugs:gui
```

SpotBugs is configured with:

| Setting | Value | Reason |
|---------|-------|--------|
| `failOnError` | `false` | Best-effort; never breaks the build |
| `effort` | `Max` | Most thorough analysis |
| `threshold` | `Low` | Surface all potential findings |
| `noClassOk` | `true` | Skip unresolvable JDK references gracefully |

### C++ — cppcheck

[cppcheck](https://cppcheck.sourceforge.io/) is invoked via the `lint` target
in [`cpp/Makefile`](cpp/Makefile).

**Install cppcheck:**

```bash
# Debian / Ubuntu
sudo apt-get install cppcheck

# macOS (Homebrew)
brew install cppcheck

# Windows (Chocolatey)
choco install cppcheck
```

**Run locally** (from the `cpp/` directory):

```bash
make lint
```

This runs:

```bash
cppcheck --enable=all --std=c++17 --suppress=missingIncludeSystem src/
```

| Option | Purpose |
|--------|---------|
| `--enable=all` | Enable style, performance, portability and unused-function checks |
| `--std=c++17` | Match the project's C++ standard |
| `--suppress=missingIncludeSystem` | Suppress noise from system headers that cppcheck cannot resolve |

---

## Best-Effort Statement

The quality tooling described in this document is provided on a **best-effort**
basis.  There is no SLA or guarantee that:

- every warning will be fixed in a given timeframe,
- the tools will run in all CI environments, or
- a PR that introduces new warnings will be automatically rejected.

Contributors are encouraged to check the output of these tools before
submitting a PR and to address warnings where practical.
