package com.blackhillsoftware.terse;

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

public class TerseDecompress {

    static final boolean DEBUG = false;  /* Control output of debug messages */

    DataInputStream BufferedStream;
    CompressedInputReader input;
    
    DecompressedOutputWriter DecompressedOutputWriter;

    private static final String DetailedHelp = new String(
          "Usage: \"TerseDecompress <input file> <output file> [-b]\"\n\n"
         +"Java TerseDecompress will decompress a file compressed using the terse program on z/OS\n"
         +"Default mode is text mode, which will attempt ebcdic -> ASCII conversion\n"
         +"The -b flag turns on binary mode, no conversion will be attempted\n"
         +"Please mail comments/suggestions to: clientcenter@de.ibm.com\n"
        );

    private static final String Version = new String ("Version 5, March 2020");

    /*
     * Currently working towards a decompress in binary mode only implementation.
     * We assume that we get "TerseDecompress <input file> <output file>". Otherwise exit with
     * an error message.
     */

    private void process (String args[]) throws Exception {

        if (args.length == 0 || args.length > 3) 
        {
            System.out.println(DetailedHelp);
            System.out.println(Version);
            System.exit(0);
        }

        if (args.length == 1) {
            System.out.println(DetailedHelp);
            System.out.println(Version);
            System.exit(0);
        }

        if(DEBUG) {
            System.out.println("Input args exist. About to check header");
        }

        try(DataInputStream imputStream = 
        		new DataInputStream(
        				new BufferedInputStream(
        						new FileInputStream(args[0]))))
		{		        
	        TerseHeader header_rv;
	        /*Apparently need to do this to set the spack flag, but the results are thrown away*/
	        header_rv = TerseHeader.CheckHeader(imputStream);
		        
	        if (args.length == 3) 
	        {
	        	header_rv.TextFlag = false;
	            System.out.println("3rd argument (" +args[2] +") found, so using binary mode.");
	        }		 
		        
	        try (DecompressedOutputWriter outputWriter 
	        		= new DecompressedOutputWriter(
	        				header_rv, 
	        				new BufferedOutputStream(new FileOutputStream(args[1]))))
	        {	 
		        System.out.println("Attempting to decompress input file (" + args[0] +") to output file (" + args[1] +")");
		
		        input = new CompressedInputReader(imputStream);
		        
		        if (!header_rv.SpackFlag) {
		        	NonSpack.decodeNonSpack(header_rv, input, outputWriter);
		        } else {
		        	Spack spack = new Spack();
		        	spack.decodeSpack(header_rv, input, outputWriter);
		        }
	        }	
		}
        System.out.println("Processing completed");
        if (DEBUG) {
            System.err.println("Read " + input.red + " bytes");
        }
    }

    public static void main (String args[]) throws Exception {

        TerseDecompress tersed = new TerseDecompress();
        tersed.process(args);
    }

}
