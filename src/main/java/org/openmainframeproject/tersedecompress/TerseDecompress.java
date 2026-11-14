package org.openmainframeproject.tersedecompress;

/**
  Copyright Contributors to the TerseDecompress Project.
  SPDX-License-Identifier: Apache-2.0
**/
/*****************************************************************************/
/* Copyright 2018        IBM Corp.                                           */
/*                                                                           */
/*   Licensed under the Apache License, Version 2.0 (the "License");         */
/*   you may not use this file except in compliance with the License.        */
/*   You may obtain a copy of the License at                                 */
/*                                                                           */
/*     http://www.apache.org/licenses/LICENSE-2.0                            */
/*                                                                           */
/*   Unless required by applicable law or agreed to in writing, software     */
/*   distributed under the License is distributed on an "AS IS" BASIS,       */
/*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.*/
/*   See the License for the specific language governing permissions and     */
/*   limitations under the License.                                          */
/*****************************************************************************/
/*                                                                           */
/*  For problems and requirements please create a GitHub issue               */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*  Author: Iain Lewis                          August 2004 (version 3)      */
/*                                                                           */
/*****************************************************************************/
/* Version 4 with editorial changes for publication as open source code      */
/*          Klaus Egeler, Boris Barth  (clientcenter@de.ibm.com)             */
/*****************************************************************************/
/* Version 5: support for variable length binary records                     */
/*          Andrew Rowley, Black Hill Software                               */
/*          Mario Bezzi, Watson Walker                                       */
/*****************************************************************************/

import java.io.*;
import java.util.zip.GZIPOutputStream;

class TerseDecompress {

    private static final String DetailedHelp = new String(
            "Usage: \"TerseDecompress <input file> <output file> [-b]\"\n\n"
           +"Java TerseDecompress will decompress a file compressed using the terse program on z/OS\n"
           +"Default mode is text mode, which will attempt EBCDIC -> ASCII conversion\n"
           +"If no <output file> provided in text mode, it will default to <input file>.txt\n"
           +"Options:\n"
           +"-b flag turns on binary mode, no conversion will be attempted\n"
           +"-z output file will be compressed using available gzip library\n"
           +"-h or --help prints this message\n"
           +"-v or --version prints the version information\n"
          );

    private static final String versionString = new String ("Version 5.1, commit " + Version.COMMIT_ID_ABBREV);
    private String inputFileName = null;
    private String outputFileName = null;
    private boolean isHelpRequested = false;
    private boolean textMode = true;
    private boolean gzipMode = false;
	
	private void printUsageAndExit() {
		System.out.println(DetailedHelp);
		System.out.println(versionString);
        System.exit(0);
	}	
	
    private void process (String args[]) throws Exception {
        parseArgs(args);
    	if (args.length == 0 || inputFileName == null || isHelpRequested == true) {
            printUsageAndExit();
        }
        System.out.println(
                "Decompressing input file '" + inputFileName + "' to output file '" + outputFileName + "'");
        try (InputStream inputStream = new FileInputStream(inputFileName);
             OutputStream fileStream = new FileOutputStream(outputFileName);
             OutputStream outputStream = gzipMode ? new GZIPOutputStream(fileStream) : fileStream;
             TerseDecompresser outputWriter = TerseDecompresser.create(inputStream, outputStream, textMode)) {
	        outputWriter.decode();
        }
		
        System.out.println("Processing completed");
    }

    private void parseArgs(String args[]) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h") || args[i].equals("--help")) {
                isHelpRequested = true;
            }
            else if (args[i].equals("-b")) {
                textMode = false;
            }
            else if (args[i].equals("-z")) {
                gzipMode = true;
            }
            else if (args[i].equals("-v") || args[i].equals("--version")) {
                System.out.println(versionString);
                System.exit(0);
            }
            // first non-flag argument is the input file name
            else if (inputFileName == null) {
                inputFileName = args[i];
            }
            // second non-flag argument is the output file name
            else if (outputFileName == null) {
                outputFileName = args[i];
            }
            else // we have more args than we know what to do with
            {
                isHelpRequested = true;
            }
        }
        if (outputFileName != null) {
            return;
        }
        if (!isHelpRequested && inputFileName != null && inputFileName.startsWith("//")) {
            System.err.println("Error: output file is required for a data set input");
            System.exit(1);
        }
        String outputFileExtension = textMode ? ".txt" : ".bin";
        outputFileExtension += gzipMode ? ".gz" : "";
        outputFileName = inputFileName + outputFileExtension;
    }
    public static void main (String args[]) {
        try {
            new TerseDecompress().process(args);
        } catch (Exception e) {
            System.err.println("Decompression failed: " + (e.getMessage() != null ? e.getMessage() : e));
            System.exit(1);
        }
    }

}
