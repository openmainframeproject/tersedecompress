# tersedecompress
TerseDecompress is a java program that can be used to decompress files which have been previously compressed on an IBM Mainframe using the TERSE / AMATERSE program (on IBM z/OS or IBM z/VM).



Description:

============

TerseDecompress is a java program that can be used to decompress files

which have been previously compressed on an IBM Mainframe

using the TERSE / AMATERSE program (on IBM z/OS or IBM z/VM).

Purpose & benefit:

==================

As java programs can virtually run on any platform / operating system

with a JVM, this java decompression program can be very useful if you

don't have access to an IBM mainframe but need to analyze or process a

file which had been compressed on a IBM mainframe using TERSE on IBM z/OS.

With this java version of TerseDecompress, you can decompress those tersed

IBM Mainframe files on any workstation or laptop etc. that supports Java.

How to run it:

==============

TerseDecompress will uncompress a file that was compressed using the TERSE

program on IBM z/OS.

For execution, TerseDecompress needs a JVM runtime environment

Usage: "TerseDecompress [-b]"

 Default mode is text mode, which will attempt ebcdic -> ASCII conversion

 The -b flag turns on binary mode, no conversion will be attempted

How to build it:

================

For compiling the java source code, a JDK is required

Build command: "javac TerseDecompress.java"

How to report problems and get support/help:

============================================

If you have a problem / need help with TerseDecompress.java, please create a GitHub issue.

You can also send an email to: clientcenter@de.ibm.com or contact us on Slack at https://slack.openmainframeproject.org channel #tersedecompress

Please do not send code changes, but explain in a mail

what you want TerseDecompress to do.
