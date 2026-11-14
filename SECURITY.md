# Security Policy

## Reporting a vulnerability

**Do not report security vulnerabilities in public GitHub issues, pull
requests, discussions or Slack.**

Report them privately with GitHub's private vulnerability reporting:

1. Go to the repository's [Security tab](https://github.com/openmainframeproject/tersedecompress/security).
2. Click **Report a vulnerability**.
3. Describe the issue, the affected implementation (Java, C++ or both),
   the version, and how to reproduce it.

Only the project maintainers can see the report. We will acknowledge it,
work with you on a fix, and publish a GitHub security advisory when the fix
is released.

Don't include real customer or production data in a report. If you need a
sample input to reproduce the problem, create a minimal test file instead.

## Supported versions

Security fixes go into the latest release only. Older versions are not
patched.

## Scope

TerseDecompress reads untrusted input files. Problems in how it handles
malformed or malicious TERSE files are in scope, for example crashes,
out-of-bounds reads or writes, unbounded memory use, or writing outside the
requested output file.
