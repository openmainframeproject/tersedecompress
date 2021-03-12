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
/*  or contact: clientcenter@de.ibm.com                                      */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*  Author: Iain Lewis                          August 2004 (version 3)      */
/*                                                                           */
/*****************************************************************************/
/* Version 4 with editorial changes for publication as open source code      */
/*          Klaus Egeler, Boris Barth  (clientcenter@de.ibm.com)             */
/*****************************************************************************/

import java.io.*;

class TerseDecompress {

    private static final String DetailedHelp = new String(
            "Usage: \"TerseDecompress <input file> <output file> [-b]\"\n\n"
           +"Java TerseDecompress will decompress a file compressed using the terse program on z/OS\n"
           +"Default mode is text mode, which will attempt ebcdic -> ASCII conversion\n"
           +"The -b flag turns on binary mode, no conversion will be attempted\n"
          );

    private static final String Version = new String ("Version 5, March 2020");
	
	private void printUsageAndExit() {
		System.out.println(DetailedHelp);
		System.out.println(Version);
        System.exit(0);
	}	
	
    private void process (String args[]) throws Exception {
    	
    	String inputFileName = null;
    	String outputFileName = null;
    	boolean textMode = true;   	
    	
    	if (args.length == 0) 
        {
            printUsageAndExit();
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


        try (TerseDecompresser outputWriter 
        		= TerseDecompresser.create(new FileInputStream(inputFileName), new FileOutputStream(outputFileName)))
        {	 
        	outputWriter.TextFlag = textMode;
	        System.out.println("Attempting to decompress input file (" + inputFileName +") to output file (" + outputFileName +")");
	        outputWriter.decode();
        }	
		
        System.out.println("Processing completed");
    }

    public static void main (String args[]) throws Exception {

        TerseDecompress tersed = new TerseDecompress();
        tersed.process(args);
    }

}
