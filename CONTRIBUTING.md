# Contributing to TerseDecompress

All code contributed to this project adheres to the
[Contribution Guidelines for Open Mainframe Project hosted projects](https://github.com/openmainframeproject/tsc/blob/master/process/contribution_guidelines.md).

## Developer Certificate of Origin

Every commit must be signed off under the
[Developer Certificate of Origin](https://developercertificate.org/). Sign off with:

```sh
git commit -s
```

This adds a `Signed-off-by: Your Name <your@email>` line. Pull requests with
commits that aren't signed off can't be merged.

## Branches

- `master` holds the latest release.
- New work happens on the current release branch, `release-X.Y` (for example,
  `release-5.1`). Open pull requests against that branch, unless a
  maintainer asks you to target another one.

## Before opening a pull request

1. Initialize the test data: `git submodule update --init`
2. Build and test the Java implementation:
   `mvn clean package`
3. Build and test the C++ implementation: `cd cpp && make check`
4. Add tests for the change you're making.

CI runs both test suites on every pull request. See [README.md](README.md)
for the build requirements.

## Test policy

New features and bug fixes must include automated tests that cover them,
in the Java suite (`src/test`), the C++ suite (`cpp/tests`), or both if
both implementations change. A fix for a decoding bug should include a
regression test that fails without the fix. Reviewers will ask for tests
before merging a pull request that changes behaviour.

## Keep both implementations in sync

The Java and C++ implementations should behave the same way. If you fix a
decoding bug or change command-line behaviour in one, make the same change
in the other in the same pull request, or open an issue for the other one.

## Code quality

Check the compiler warnings and static analysis output before you open a
pull request. See [QUALITY.md](QUALITY.md).

## Reporting security issues

Don't open a public issue or pull request for a security vulnerability.
See [SECURITY.md](SECURITY.md).
