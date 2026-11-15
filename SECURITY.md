# Security Policy

## Reporting a vulnerability

**Do not report security vulnerabilities in public GitHub issues, pull
requests, discussions or Slack.**

Report them privately with GitHub's private vulnerability reporting:

1. Go to the repository's [Security tab](https://github.com/openmainframeproject/tersedecompress/security).
2. Click **Report a vulnerability**.
3. Describe the issue, the affected implementation (Java, C++ or both),
   the version, and how to reproduce it.

Only the project maintainers can see the report.

## What happens after you report

1. We acknowledge the report within 14 days.
2. We confirm whether it's a vulnerability, assess its severity, and keep
   you updated at least every 30 days while we work on it.
3. We aim to release a fix within 90 days of the report. If we need longer,
   we agree a new date with you.
4. When the fix is released, we publish a
   [GitHub security advisory](https://github.com/openmainframeproject/tersedecompress/security/advisories)
   and name you as the reporter, unless you ask to stay anonymous.

Please keep the issue private until the advisory is published.

Don't include real customer or production data in a report. If you need a
sample input to reproduce the problem, create a minimal test file instead.

## Dependency and code scanning

These rules apply to every release. [RELEASE.md](RELEASE.md) includes the
checks in the release workflow.

**Automated checks.** Every pull request to `master` or a `release-*`
branch is checked automatically, and can't be merged while a check fails:

- [Dependency review](.github/workflows/dependency-review.yml) fails if the
  pull request adds a dependency with a known high or critical
  vulnerability, a malicious-package advisory, or a license that's not
  allowed.
- [CodeQL](.github/workflows/codeql.yml) scans the Java and C++ code for
  security weaknesses. Merging is blocked while the pull request adds
  high or critical findings.

A maintainer can override a check only by dismissing the finding in GitHub
with a recorded reason, such as a false positive.

**Dependencies.** Before each release, the maintainers check the
dependencies (the Maven dependencies and plugins in `pom.xml`, zlib and
GoogleTest) for known vulnerabilities and license problems:

- A known vulnerability rated high or critical (CVSS 7.0 or more) that
  affects TerseDecompress blocks the release until it's fixed.
- Medium and low vulnerabilities are fixed in the next release or recorded
  as not affecting the project.
- Every dependency must have a license compatible with Apache-2.0.

**Static analysis.** Before each release, the maintainers run SpotBugs and
cppcheck ([QUALITY.md](QUALITY.md)). Any finding that could be a security
weakness (buffer overrun, out-of-bounds access, null dereference, use of
uninitialised data, integer overflow, resource leak) must be fixed or
recorded as a false positive before the release. Other findings are
best-effort.

**Vulnerabilities that don't affect us.** If a known vulnerability in a
dependency doesn't affect TerseDecompress, for example because the
vulnerable code isn't used, we record it in an
[OpenVEX](https://openvex.dev/) document attached to the GitHub release.
The document explains why the vulnerability can't be exploited.

## Secrets and credentials

- The CI workflows use no secrets and run with a read-only token.
- If a workflow ever needs a secret, it's stored only as a GitHub encrypted
  secret, scoped to the job that needs it, and never exposed to pull
  requests from forks.
- Secrets, tokens, passwords and private keys must never be committed to
  the repository or put in an issue. If one is exposed, revoke it at once
  and tell the maintainers.
- Maintainer access to GitHub is personal. Accounts and tokens are never
  shared.

## Supported versions

Security fixes go into the latest release only. Older versions are not
patched.

## Scope

TerseDecompress reads untrusted input files. Problems in how it handles
malformed or malicious TERSE files are in scope, for example crashes,
out-of-bounds reads or writes, unbounded memory use, or writing outside the
requested output file.
