# Support and Help

This document explains how users and developers can get help with **TerseDecompress**.

## Getting help

Ask questions and report bugs in the GitHub issue tracker:
https://github.com/openmainframeproject/tersedecompress/issues

When opening an issue, include:
- the TerseDecompress version (`java -jar tersedecompress.jar -v`, or the
  version line printed by `tersedecompress++ -h`)
- which implementation you used: Java or C++
- your operating system, and Java version if you used the jar
- the exact command line, including options such as `-b` and `-z`
- the error message or output
- how the file was tersed on the mainframe, if you know it (PACK or SPACK,
  record format, data set type)

Tersed files often contain confidential data. **Don't attach production files
to a public issue.** If a sample file is needed to reproduce the problem,
create a small test file with non-sensitive content.

You can also ask in the Open Mainframe Project Slack, channel
[#tersedecompress](https://slack.openmainframeproject.org).

## Reporting vulnerabilities

Don't report security vulnerabilities in public issues. Follow
[SECURITY.md](SECURITY.md) to report them privately.

## Response expectations

This is a community project maintained by volunteers.
We try to respond to new issues within a reasonable time, but there is
**no guaranteed SLA**.

## Developer support

Developers can:
- fork the repository and submit pull requests (see [CONTRIBUTING.md](CONTRIBUTING.md))
- comment on issues with design questions
