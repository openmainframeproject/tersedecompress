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
           +"-h or --help prints this message\n"
          );
	
    private static final String Version = new String ("Version 6, March 2024");
    private String inputFileName = null;
    private String outputFileName = null;
    private boolean isHelpRequested = false;
    private boolean textMode = true;
	
	private void printUsageAndExit() {
		System.out.println(DetailedHelp);
		System.out.println(Version);
        System.exit(0);
	}	
	
    private void process (String args[]) throws Exception {
		parseArgs(args);
    	if (args.length == 0 || (inputFileName == null && outputFileName == null) || (outputFileName == null && textMode == false) || isHelpRequested == true) 
        {
            printUsageAndExit();
        }

        if (outputFileName == null) {
            outputFileName = inputFileName + ".txt";
        }    	
        
    	for (int i=0; i < args.length; i++)
    	{
    		if (args[i].equals("-h") || args[i].equals("--help"))
    		{
                printUsageAndExit();
    		}
    		else if (args[i].equals("-b"))
    		{
    			textMode = false;
    		}
    		// first non-flag argument is the input file name 
    		else if (inputFileName == null)
    		{
    			inputFileName = args[i];
    		}
    		// second non-flag argument is the input file name 
    		else if (outputFileName == null)
    		{
    			outputFileName = args[i];
    		}
    		else // we have more args than we know what to do with
    		{
                printUsageAndExit();   		
    		}
    	}
    	if (inputFileName == null || outputFileName == null)
    	{
    		printUsageAndExit();
    	}

		System.out.println("Attempting to decompress input file (" + inputFileName +") to output file (" + outputFileName +")");

		if (outputFileName.endsWith(".gz")) {
			try (TerseDecompresser outputWriter = TerseDecompresser.create(new FileInputStream(inputFileName), new GZIPOutputStream(new FileOutputStream(outputFileName), 8192, true)))
			{	 
				outputWriter.TextFlag = textMode;
				outputWriter.decode();
			}
		}
		else {
			try (TerseDecompresser outputWriter = TerseDecompresser.create(new FileInputStream(inputFileName), new FileOutputStream(outputFileName)))
			{	 
				outputWriter.TextFlag = textMode;
				outputWriter.decode();
			}
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
            // first non-flag argument is the input file name
            else if (inputFileName == null) {
                inputFileName = args[i];
            }
            // second non-flag argument is the input file name
            else if (outputFileName == null) {
                outputFileName = args[i];
            }
            else // we have more args than we know what to do with
            {
                isHelpRequested = true;
            }
        }
    }
	
    public static void main (String args[]) throws Exception {

        TerseDecompress tersed = new TerseDecompress();
        tersed.process(args);
    }

}
