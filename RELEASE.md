# Release Documentation

This document describes how **TerseDecompress** performs releases, why releases happen, and how to interpret them.

## Purpose

Releases signal reviewed, stable versions of the software for users. They provide:
- a unique version identifier
- release notes describing functionality and fixes
- references to where users can obtain the artifacts and ask questions

## Versioning

We use semantic versioning **MAJOR.MINOR.PATCH**:
- **PATCH**: bug and doc fixes
- **MINOR**: new backward-compatible enhancements
- **MAJOR**: breaking changes (rare)

The Java and C++ implementations are released together under the same version.

## Release frequency

Releases do not happen on a fixed schedule. They occur when:
- a bug impacting users is fixed
- a new feature is completed and tested
- accumulated changes benefit a stable release

## Branches

- `master` holds the latest release.
- Each minor version is developed on a release branch, `release-X.Y`
  (for example, `release-5.1`).

## Release workflow

1. All planned work is merged into `release-X.Y`.
2. Set the version in all three places:
   - `pom.xml`: `<version>`
   - `src/main/java/org/openmainframeproject/tersedecompress/TerseDecompress.java`: `versionString`
   - `cpp/src/argumentParser.cpp`: the `Version:` line in `showHelp()`
3. Update the version history in `README.md`.
4. CI passes on `release-X.Y` for both Java and C++.
5. Check the C++ build on z/OS (CI doesn't cover z/OS).
6. Open a pull request from `release-X.Y` to `master`. At least one other
   maintainer approves it.
7. Merge the pull request.
8. Tag the merge commit on `master` as `release_X.Y.Z` and push the tag.
9. Publish a GitHub Release for the tag, with release notes and these artifacts:
   - `tersedecompress.jar`, built with `mvn clean package`
   - `tersedecompress++` binaries for the platforms the maintainers can build

## Release notes

Release notes include:
- high-level summary of what changed
- user-facing changes, including new or changed command-line options
- deprecated or removed behaviour
- links to issues and pull requests

## Dependencies

- Java: no runtime dependencies beyond a Java 21 or later runtime. Tests use JUnit 4.
- C++: zlib at runtime. Tests use GoogleTest.
- Build plugins are pinned in `pom.xml`.

## Support and reporting

See [SUPPORT.md](SUPPORT.md) for how users get help and report problems, and
[SECURITY.md](SECURITY.md) for reporting vulnerabilities.
