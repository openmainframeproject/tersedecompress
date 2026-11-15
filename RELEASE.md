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
  (for example, `release-5.1`). Fixes collect there and are merged to
  `master` for a release.
- When you create a new release branch, set `target-branch` in
  [`.github/dependabot.yml`](.github/dependabot.yml) to it, and merge that
  change to `master`, because Dependabot reads its configuration only from
  `master`.

## Release workflow

1. All planned work is merged into `release-X.Y`.
2. Set the version in all three places:
   - `pom.xml`: `<version>`
   - `src/main/java/org/openmainframeproject/tersedecompress/TerseDecompress.java`: `versionString`
   - `cpp/src/argumentParser.cpp`: the `Version:` line in `showHelp()`
3. Update the version history in `README.md`.
4. CI passes on `release-X.Y` for both Java and C++.
5. Check the C++ build on z/OS (CI doesn't cover z/OS).
   Run the dependency and static analysis checks in
   [SECURITY.md](SECURITY.md#dependency-and-code-scanning), and fix or
   record any findings that block the release.
6. Open a pull request from `release-X.Y` to `master`. At least one other
   maintainer approves it.
7. Merge the pull request.
8. Tag the merge commit on `master` as `release_X.Y.Z` and push the tag.
9. The [release workflow](.github/workflows/release.yml) builds and tests
   the release from the tag and creates a draft GitHub Release with:
   - `tersedecompress-X.Y.Z.jar`
   - `tersedecompress++-X.Y.Z-linux-x86_64.tar.gz` and
     `tersedecompress++-X.Y.Z-macos-arm64.tar.gz`
   - a CycloneDX SBOM for each of them (the same name plus `.cdx.json`)
   - `tersedecompress-X.Y.Z.sha256`, the SHA-256 checksums of all the files

   The workflow fails if the tag doesn't match the version in `pom.xml`.

   Each file is signed with a
   [GitHub artifact attestation](https://docs.github.com/en/actions/security-for-github-actions/using-artifact-attestations)
   (Sigstore build provenance).
10. Add the release notes to the draft and publish it.

Don't upload artifacts built anywhere else, because they wouldn't be
signed. Binaries for other platforms, such as z/OS, can be offered
separately, with a note that they aren't signed.

## Verify a release

Check that a file you downloaded is intact:

```sh
sha256sum --ignore-missing -c tersedecompress-X.Y.Z.sha256
```

Check that it was built by this project's release workflow from a release
tag, using the [GitHub CLI](https://cli.github.com/):

```sh
gh attestation verify tersedecompress-X.Y.Z.jar \
  --repo openmainframeproject/tersedecompress \
  --signer-workflow openmainframeproject/tersedecompress/.github/workflows/release.yml
```

Verification succeeds only if the file was signed by the release workflow
in `openmainframeproject/tersedecompress`. The output shows the tag and
commit it was built from. No other signing identity is used for releases.

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
- Dependencies are tracked with Dependabot, and each release includes an
  SBOM listing them.

## Support and reporting

See [SUPPORT.md](SUPPORT.md) for how users get help and report problems, and
[SECURITY.md](SECURITY.md) for reporting vulnerabilities.
