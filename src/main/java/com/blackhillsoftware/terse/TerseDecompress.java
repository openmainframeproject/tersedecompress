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

package com.blackhillsoftware.terse;

import java.io.*;


public class TerseDecompress {

    static public final boolean Debug = false;  /* Control output of debug messages */

    /*
     * These appear to be control parameters to control i/o buffers, stack size etc
     * and hence will affect how much memory we use. The only ones actually used
     * in the current implementation are TreeSize and RecordMark
     */
    static public final int  StackSize = 0x07FF;     /* 2k - 1 */
    static public final int  BufferSize = 0x07FF;    /* 2k - 1 */
    static public final int  HashSize = 0x0FFF;      /* 4k - 1 */
    static public final int  TreeSize = 0x1000;      /* 4k     */
    static public final int  RecordMark = 257;       /*used in the file output functions*/

    /*
    Some Used in the unspack algorithm. I guess tuning affects the
    way which compression/decompression works
    Others are just useful constants
    */
    static public final int Base = 0;
    static public final int CodeSize = 257; /* 2**8+1, EOF, 256 Codepoints, RCM */
    static public final int EndOfFile =0;
    /* This is the new line char that we write into the file. Not sure how
     * this will translate on the various platforms
     */
    static final char EOL = '\n';

    /*Useful Constants*/
    static final int None = -1;
     

    int [] Father = new int[TreeSize];
    int [] CharExt = new int[TreeSize];
    int [] Backward = new int[TreeSize];
    int [] Forward = new int[TreeSize];

    File InputFile;
    FileInputStream InputFileStream;
    BufferedInputStream BufferedStream;

    File OutputFile;
    FileOutputStream OutputFileStream;
    BufferedOutputStream OutputBufferedStream;

    static final String DetailedHelp = new String(
          "Usage: \"TerseDecompress <input file> <output file> [-b]\"\n\n"
         +"Java TerseDecompress will decompress a file compressed using the terse program on z/OS\n"
         +"Default mode is text mode, which will attempt ebcdic -> ASCII conversion\n"
         +"The -b flag turns on binary mode, no conversion will be attempted\n"
         +"Please mail comments/suggestions to: clientcenter@de.ibm.com\n"
        );

    static final String Version = new String ("version 4, December 2018");


    boolean      ExamineFlag   = false; /* display contents of tersed file header   */
    boolean      FixedFlag     = false; /* host compatibility fixed block length    */
    boolean      HelpFlag      = false; /* true when extended help is requested     */
    boolean      InfoFlag      = false; /* true when statistics are requested       */
    boolean      QuietFlag     = false; /* true when quiet is selected              */
    boolean      VariableFlag  = true ; /* true for variable-length records         */
    int          XlateTableEbc = 37   ; /* ebcdic code page                         */
    int          XlateTableAsc = 437  ; /* ascii code page                          */
    boolean      XlateTableDef = true ; /* use default ALMCOPY table                */
    long         OutputPhase   = 0    ; /* position in fixed-length output record   */
    long         OutputTotal   = 0    ; /* total number of bytes                    */
    long         RecordLength  = 256  ; /* host perspective record length           */



    /*
     * Default mapping tables for ascii to ebcdic conversions. The values actually used in the code are
     * EbcToAsc and vice versa, so assign them below. If we use alternative conversion tables then this
     * needs to be done dynamically. (Not implemented)
     * It looks like the default tables are the ones used to generate the alternative tables for different
     * locales. The alm tables are the ones we use when nothing else is specified.
     */

    static final int EbcToAscDef[] = {
        0x00,0x01,0x02,0x03,0xCF,0x09,0xD3,0x7F,0xD4,0xD5,0xC3,0x0B,0x0C,0x0D,0x0E,0x0F,
        0x10,0x11,0x12,0x13,0xC7,0xB4,0x08,0xC9,0x18,0x19,0xCC,0xCD,0x83,0x1D,0xD2,0x1F,
        0x81,0x82,0x1C,0x84,0x86,0x0A,0x17,0x1B,0x89,0x91,0x92,0x95,0xA2,0x05,0x06,0x07,
        0xE0,0xEE,0x16,0xE5,0xD0,0x1E,0xEA,0x04,0x8A,0xF6,0xC6,0xC2,0x14,0x15,0xC1,0x1A,
        0x20,0xA6,0xE1,0x80,0xEB,0x90,0x9F,0xE2,0xAB,0x8B,0x9B,0x2E,0x3C,0x28,0x2B,0x7C,
        0x26,0xA9,0xAA,0x9C,0xDB,0xA5,0x99,0xE3,0xA8,0x9E,0x21,0x24,0x2A,0x29,0x3B,0x5E,
        0x2D,0x2F,0xDF,0xDC,0x9A,0xDD,0xDE,0x98,0x9D,0xAC,0xBA,0x2C,0x25,0x5F,0x3E,0x3F,
        0xD7,0x88,0x94,0xB0,0xB1,0xB2,0xFC,0xD6,0xFB,0x60,0x3A,0x23,0x40,0x27,0x3D,0x22,
        0xF8,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x96,0xA4,0xF3,0xAF,0xAE,0xC5,
        0x8C,0x6A,0x6B,0x6C,0x6D,0x6E,0x6F,0x70,0x71,0x72,0x97,0x87,0xCE,0x93,0xF1,0xFE,
        0xC8,0x7E,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A,0xEF,0xC0,0xDA,0x5B,0xF2,0xF9,
        0xB5,0xB6,0xFD,0xB7,0xB8,0xB9,0xE6,0xBB,0xBC,0xBD,0x8D,0xD9,0xBF,0x5D,0xD8,0xC4,
        0x7B,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0xCB,0xCA,0xBE,0xE8,0xEC,0xED,
        0x7D,0x4A,0x4B,0x4C,0x4D,0x4E,0x4F,0x50,0x51,0x52,0xA1,0xAD,0xF5,0xF4,0xA3,0x8F,
        0x5C,0xE7,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0xA0,0x85,0x8E,0xE9,0xE4,0xD1,
        0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0xB3,0xF7,0xF0,0xFA,0xA7,0xFF
    };
    
    static final int AscToEbcDef[] = {
        0x00,0x01,0x02,0x03,0x37,0x2D,0x2E,0x2F,0x16,0x05,0x25,0x0B,0x0C,0x0D,0x0E,0x0F,
        0x10,0x11,0x12,0x13,0x3C,0x3D,0x32,0x26,0x18,0x19,0x3F,0x27,0x22,0x1D,0x35,0x1F,
        0x40,0x5A,0x7F,0x7B,0x5B,0x6C,0x50,0x7D,0x4D,0x5D,0x5C,0x4E,0x6B,0x60,0x4B,0x61,
        0xF0,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0x7A,0x5E,0x4C,0x7E,0x6E,0x6F,
        0x7C,0xC1,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xD1,0xD2,0xD3,0xD4,0xD5,0xD6,
        0xD7,0xD8,0xD9,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7,0xE8,0xE9,0xAD,0xE0,0xBD,0x5F,0x6D,
        0x79,0x81,0x82,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x91,0x92,0x93,0x94,0x95,0x96,
        0x97,0x98,0x99,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7,0xA8,0xA9,0xC0,0x4F,0xD0,0xA1,0x07,
        0x43,0x20,0x21,0x1C,0x23,0xEB,0x24,0x9B,0x71,0x28,0x38,0x49,0x90,0xBA,0xEC,0xDF,
        0x45,0x29,0x2A,0x9D,0x72,0x2B,0x8A,0x9A,0x67,0x56,0x64,0x4A,0x53,0x68,0x59,0x46,
        0xEA,0xDA,0x2C,0xDE,0x8B,0x55,0x41,0xFE,0x58,0x51,0x52,0x48,0x69,0xDB,0x8E,0x8D,
        0x73,0x74,0x75,0xFA,0x15,0xB0,0xB1,0xB3,0xB4,0xB5,0x6A,0xB7,0xB8,0xB9,0xCC,0xBC,
        0xAB,0x3E,0x3B,0x0A,0xBF,0x8F,0x3A,0x14,0xA0,0x17,0xCB,0xCA,0x1A,0x1B,0x9C,0x04,
        0x34,0xEF,0x1E,0x06,0x08,0x09,0x77,0x70,0xBE,0xBB,0xAC,0x54,0x63,0x65,0x66,0x62,
        0x30,0x42,0x47,0x57,0xEE,0x33,0xB6,0xE1,0xCD,0xED,0x36,0x44,0xCE,0xCF,0x31,0xAA,
        0xFC,0x9E,0xAE,0x8C,0xDD,0xDC,0x39,0xFB,0x80,0xAF,0xFD,0x78,0x76,0xB2,0x9F,0xFF
    };

    /*Alternative ascii to ebcdic conversion tables but they appear to be the ones actually used*/
    static final int EbcToAscAlmcopy[] = {
        0x00,0x01,0x02,0x03,0xCF,0x09,0xD3,0x7F,0xD4,0xD5,0xC3,0x0B,0x0C,0x0D,0x0E,0x0F,
        0x10,0x11,0x12,0x13,0xC7,0xB4,0x08,0xC9,0x18,0x19,0xCC,0xCD,0x83,0x1D,0xD2,0x1F,
        0x81,0x82,0x1C,0x84,0x86,0x0A,0x17,0x1B,0x89,0x91,0x92,0x95,0xA2,0x05,0x06,0x07,
        0xE0,0xEE,0x16,0xE5,0xD0,0x1E,0xEA,0x04,0x8A,0xF6,0xC6,0xC2,0x14,0x15,0xC1,0x1A,
        0x20,0xA6,0xE1,0x80,0xEB,0x90,0x9F,0xE2,0xAB,0x8B,0x9B,0x2E,0x3C,0x28,0x2B,0x7C,
        0x26,0xA9,0xAA,0x9C,0xDB,0xA5,0x99,0xE3,0xA8,0x9E,0x21,0x24,0x2A,0x29,0x3B,0x5E,
        0x2D,0x2F,0xDF,0xDC,0x9A,0xDD,0xDE,0x98,0x9D,0xAC,0xBA,0x2C,0x25,0x5F,0x3E,0x3F,
        0xD7,0x88,0x94,0xB0,0xB1,0xB2,0xFC,0xD6,0xFB,0x60,0x3A,0x23,0x40,0x27,0x3D,0x22,
        0xF8,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x96,0xA4,0xF3,0xAF,0xAE,0xC5,
        0x8C,0x6A,0x6B,0x6C,0x6D,0x6E,0x6F,0x70,0x71,0x72,0x97,0x87,0xCE,0x93,0xF1,0xFE,
        0xC8,0x7E,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7A,0xEF,0xC0,0xDA,0x5B,0xF2,0xF9,
        0xB5,0xB6,0xFD,0xB7,0xB8,0xB9,0xE6,0xBB,0xBC,0xBD,0x8D,0xD9,0xBF,0x5D,0xD8,0xC4,
        0x7B,0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0xCB,0xCA,0xBE,0xE8,0xEC,0xED,
        0x7D,0x4A,0x4B,0x4C,0x4D,0x4E,0x4F,0x50,0x51,0x52,0xA1,0xAD,0xF5,0xF4,0xA3,0x8F,
        0x5C,0xE7,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5A,0xA0,0x85,0x8E,0xE9,0xE4,0xD1,
        0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0xB3,0xF7,0xF0,0xFA,0xA7,0xFF
    };
    
    static final int AscToEbcAlmcopy[] = {
        0x00,0x01,0x02,0x03,0x37,0x2D,0x2E,0x2F,0x16,0x05,0x25,0x0B,0x0C,0x0D,0x0E,0x0F,
        0x10,0x11,0x12,0x13,0x3C,0x3D,0x32,0x26,0x18,0x19,0x3F,0x27,0x22,0x1D,0x35,0x1F,
        0x40,0x5A,0x7F,0x7B,0x5B,0x6C,0x50,0x7D,0x4D,0x5D,0x5C,0x4E,0x6B,0x60,0x4B,0x61,
        0xF0,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0x7A,0x5E,0x4C,0x7E,0x6E,0x6F,
        0x7C,0xC1,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,0xD1,0xD2,0xD3,0xD4,0xD5,0xD6,
        0xD7,0xD8,0xD9,0xE2,0xE3,0xE4,0xE5,0xE6,0xE7,0xE8,0xE9,0xAD,0xE0,0xBD,0x5F,0x6D,
        0x79,0x81,0x82,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x91,0x92,0x93,0x94,0x95,0x96,
        0x97,0x98,0x99,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7,0xA8,0xA9,0xC0,0x4F,0xD0,0xA1,0x07,
        0x43,0x20,0x21,0x1C,0x23,0xEB,0x24,0x9B,0x71,0x28,0x38,0x49,0x90,0xBA,0xEC,0xDF,
        0x45,0x29,0x2A,0x9D,0x72,0x2B,0x8A,0x9A,0x67,0x56,0x64,0x4A,0x53,0x68,0x59,0x46,
        0xEA,0xDA,0x2C,0xDE,0x8B,0x55,0x41,0xFE,0x58,0x51,0x52,0x48,0x69,0xDB,0x8E,0x8D,
        0x73,0x74,0x75,0xFA,0x15,0xB0,0xB1,0xB3,0xB4,0xB5,0x6A,0xB7,0xB8,0xB9,0xCC,0xBC,
        0xAB,0x3E,0x3B,0x0A,0xBF,0x8F,0x3A,0x14,0xA0,0x17,0xCB,0xCA,0x1A,0x1B,0x9C,0x04,
        0x34,0xEF,0x1E,0x06,0x08,0x09,0x77,0x70,0xBE,0xBB,0xAC,0x54,0x63,0x65,0x66,0x62,
        0x30,0x42,0x47,0x57,0xEE,0x33,0xB6,0xE1,0xCD,0xED,0x36,0x44,0xCE,0xCF,0x31,0xAA,
        0xFC,0x9E,0xAE,0x8C,0xDD,0xDC,0x39,0xFB,0x80,0xAF,0xFD,0x78,0x76,0xB2,0x9F,0xFF
    };

    /*
    static int EbcToAsc[] = EbcToAscDef;
    static int AscToEbc[] = AscToEbcDef;
    */
    int EbcToAsc[] = EbcToAscAlmcopy;
    int AscToEbc[] = AscToEbcAlmcopy;


    /*
     * These appear to be the flags for the terse file header. Some are used by 
     * CheckHeader() others are only used when writing a compressed file which
     * this implementation doesn't do.
     */
    static final int  FlagUndef = 0x80;       /*  \                                                    */
    static final int  FlagCC1   = 0x40;       /*   \                                               */
    static final int  FlagCC2   = 0x20;       /*    \                                              */
    static final int  FlagVBS   = 0x10;       /*     >-- values of Flags bits in HeaderRecord type */
    static final int  FlagVS    = 0x08;         /*    /                                              */
    static final int  FlagMVS   = 0x04;      /*   /                                               */
    static final int  FlagRbits = 0x03;       /*  /                                                */
    
    /*Defaults for dump types*/
    boolean TextFlag = true;
    boolean HostFlag = true;
    boolean SpackFlag = true;
    boolean DecodeFlag = false;
    boolean EncodeFlag = false; /* true when encoding is selected */


    /*
     * Data structure used only when checking the header initially
     */

    public class TerseHeader {
    
        public int VersionFlag;
        public int VariableFlag;
        public int RecordLen1;
        public int Flags;
        public int Ratio;
        public int BlockSize;
        public long  RecordLen2;
    
    
        public String toString() {
        
            return new String (
                "\n"
                +"Version flag is " + VersionFlag +"\n"
                +"Variable Flag is " + VariableFlag +"\n"
                +"RecordLen1 is " + RecordLen1 +"\n"
                +"Flags are " + Flags +"\n"
                +"Ratio is " + Ratio +"\n"
                +"Block Size is " + BlockSize +"\n"
                +"RecordLen2 is " + RecordLen2 +"\n"
                );
        
        }
    
    }

    /*
     * Check that the header of an input tersed file is consistent and set some of the static flags
     * associated with it.
     */

    public boolean CheckHeader(File InFile) {

        TerseHeader Header = new TerseHeader();

        FileInputStream filestream;
        DataInputStream datastream;

        try {

            filestream = new FileInputStream(InFile);
            datastream = new DataInputStream(filestream);

            Header.VersionFlag = datastream.readUnsignedByte();
            Header.VariableFlag = datastream.readUnsignedByte();
            Header.RecordLen1 = datastream.readUnsignedShort();
            Header.Flags = datastream.readUnsignedByte();
            Header.Ratio = datastream.readUnsignedByte();
            Header.BlockSize = datastream.readUnsignedShort();
            Header.RecordLen2 = readUnsignedInt(datastream);
            datastream.close();

        } catch (Exception e) {
            System.err.println("Error while reading header from input file");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        if (Debug) {
            System.out.println("Header is:\n" +Header);
        }

        switch (Header.VersionFlag) {
        case 0x01: /* native binary mode, 4 byte header, versions 1.2+ */
            if (Header.VariableFlag    != 0x89)
                return false;
            if (Lo( Header.RecordLen1) != 0x69)
                return false;
            if (Hi( Header.RecordLen1) != 0xA5)
                return false;
            if (HostFlag) {
                if (TextFlag) { /* defaults */
                    HostFlag = false; /* autoswitch to native mode */
                    TextFlag = false;
                } else {
                    return false; /* header conflicts with explicit "/b" switch */
                }
            }
            break;

        case 0x02: /* host  PACK compatibility mode, 12 byte header */
            SpackFlag = false;
            if ((Header.VariableFlag != 0x00) && (Header.VariableFlag != 0x01))
                return false;
            if ( (Header.RecordLen1 == 0) ^  (Header.RecordLen2 != 0))
                return false;
            if ((Header.Flags & FlagMVS) == 0) {
                if (    Header.Flags != 0) return false;
                if (    Header.Ratio != 0) return false;
                if (Header.BlockSize != 0) return false;
            }
            if (!HostFlag) {
                return false; /* header conflicts with explicit "-n" switch */
            }
            break;

        case 0x05: /* host SPACK compatibility mode, 12 byte header */
            if ((Header.VariableFlag != 0x00) && (Header.VariableFlag != 0x01))
                return false;
            if (  (Header.RecordLen1 == 0) ^  (Header.RecordLen2 != 0) )
                return false;
            if ((Header.Flags & FlagMVS) == 0) {
                if ( Header.Flags != 0)
                    return false;
                if ( Header.Ratio != 0)
                     return false;
                if (Header.BlockSize != 0)
                    return false;
            }
            if (!HostFlag) {
                return false; /* header conflicts with explicit "-n" switch */
            }
            break;

        case 0x07: /* native binary mode, 4 byte header, versions 1.1- */
            if (Header.VariableFlag      != 0x89)
                return false;
            if (Lo( Header.RecordLen1  ) != 0x69)
                return false;
            if (Hi( Header.RecordLen1  ) != 0xA5)
                return false;
            if (HostFlag) {
                if (TextFlag) { /* defaults */
                    HostFlag = false; /* autoswitch to native mode */
                    TextFlag = false;
                } else {
                    return false; /* header conflicts with explicit "/b" switch */
                }
            }
            break;
        default:
            return false;
        }

        return true;

    }


    /*
     * Reads a 32 bit unsigned value from the Input stream and puts it into a long variable.
     * Only used by CheckHeader().
     */
    public long readUnsignedInt(InputStream stream) throws IOException {

        long rv =0 ;
        long temp;

        for (int i=0; i < 4; i++) {
            int bytes = stream.read();
            if (bytes < 0) {
                throw new EOFException("End of File");
            }
            temp = ((long)bytes) << ((3-i)*8);
            rv = rv + temp;
        }
        return rv;
    }


    /*
     * Couple of utility methods to do some bit manipulation.
     * Returns the lowest 8 bits of an 16bit value stored in an int
     */
    public int Lo (int value) {
        return (value & 0xFF) ;
    }


    /*
     * Returns the Hi 8 bits of a 16 bit value stored in an int.
     */
    public int Hi(int value) {
        value = value & 0xFF00;
        value  = value >>>8;
        return value;
    }

    
    /*
     * If condition is false, print a string to stderr and exit
     */
    public void AssertString(String message, boolean condition) {
        if (!condition) {
            System.err.println(message);
            System.exit(1);
        }
    }




    /* A list of input masks for use by FilePut() and FileGetRequired */
    static final int Mask[] = {
                 0, 
            0x0001,    0x0002,    0x0004,    0x0008,
            0x0010,    0x0020,    0x0040,    0x0080,
            0x0100,    0x0200,    0x0400,    0x0800,
            0x1000,    0x2000,    0x4000,    0x8000,
           0x10000,   0x20000,   0x40000,   0x80000,
          0x100000,  0x200000,  0x400000,  0x800000,
         0x1000000, 0x2000000, 0x4000000, 0x8000000,
        0x10000000,0x20000000,0x40000000,0x80000000,
    };



    /*
     *  Read Bits number of bits. Assumes we will never read be asked for more
     * than 16 bits of data. Returns them in the bottom of the returned int.
     * If we hit an io exception then we are probably stuffed anyway, as
     * we shouldn't get an EOF exception from any of the read methods that are used, so 
     * exit with an error message.
     * Do need to take account of what happens when we get to the end of the file?
     */

    long buffer = 0; /*Used as an input buffer*/
    int index = 0; /*The number of bits currently in buffer*/
    long temp =0;
    boolean endOfInput = false;
    long data;
    int red=0;

    public int FileGetRequired(int Bits, InputStream stream) {

        try {

            if (index < 16) {
                /*We have less than 16 bits in the buffer so read in 16 more*/
                temp = 0;
                temp = stream.read();
                red = red +1;
                if (temp != -1) {
                    buffer = buffer << 8;
                    buffer = buffer | (temp & 0xFF);
                    index = index +8;
                } else {
                    endOfInput = true;
                }
    
                temp = 0;
                temp = stream.read();
                red = red +1;
                if (temp != -1) {
                    buffer = buffer << 8;
                    buffer = buffer | (temp & 0xFF);
                    index = index +8;
                } else {
                    endOfInput = true;
                }
            }

            if (endOfInput && (index < Bits)) {
                /*  We have reached the end of the file and don't have enough
                 *  data to satisfy the request
                 */
                return 0;
            }

            data =0;
            /*Loop until we have filled data with the number of bits requested*/
            while (Bits > 0) {
    
                /*Read in 1 byte into data*/
                if (Bits >7) {
                    temp =0;
                    /*make room in the bottom of data for another byte*/
                    data = data << 8;
                    /*take the top byte of buffer*/
                    temp = 0xFF & (buffer >>> (index-8));
                    /*copy it into the bottom of data*/
                    data = data | (temp & 0xFF);
                    /*update the various numbers*/
                    index = index -8;
                    Bits = Bits - 8;
                }
    
                /*Add one bit at at time to the bottom of data*/
                else if (Bits <8) {
                    temp = 0;
                    /*get the top bit of buffer to the bottom of temp*/
                    temp = buffer & (long)(Mask[index]);
                    temp = temp >>> (index-1);
                    /*make space in the bottom of data and insert the new bit*/
                    data = data <<1;
                    data = data | temp;
                    /*Update the input numbers*/
                    index = index -1;
                    Bits = Bits - 1;
                }
    
            }
        } catch (IOException e) {
            System.err.println("Unable to read from input file. Caught IOException:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return (int)data;
    }

    
    /*
     * Read in 12 bits of data, and put them in the bottom of the returned int
     */

    int GetBlok(InputStream stream) {
        return FileGetRequired(12, stream);
    }



    /*
     *  Write Bits number of bits from the bottom of Value to the output file
     *  This assumes that we are never asked to write more than 16 bits.
     *  Returns true if the requested number of bits was written, 
     *  If we catch an exception while writing, exit, as we are stuffed.
     *  Need to clean up the output file on exceptions
     *  Uses a global byte OutputValue, and only writes when this is full
     */

    int OutputValue   = 0    ; /* current output byte   */
    int OutputMask    = 0x80 ; /* mask to write next bit to                */

    public boolean FilePutRequired (int Bits, int Value, OutputStream stream) {
        while (Bits > 0) {
            if ((Bits > 7) && (OutputMask == 0x80)) {
                OutputValue = ((Value >>> (Bits - 8)) & 0xFF);

                OutputTotal++;
                try {
                    stream.write(OutputValue);
                } catch (IOException e) {
                    System.err.println("Error while writing to output file: " + OutputFile);
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }

                Bits = Bits - 8;

            } else {
                if ((Value & Mask[Bits]) != 0) {
                    OutputValue = OutputValue | OutputMask;
                }
                if (OutputMask == 0x01) {
                    try {
                        stream.write(OutputValue);
                    } catch (IOException e) {
                        System.err.println("Error while writing to output file: " + OutputFile);
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    OutputMask = OutputMask >>> 1;
                }
                Bits--;
            }
        }
        return true;
    }
    

    /* Write a new line to the output file*/
    /* This only works when a new line is a single /n. Which it may not be on Windows */
    public void PutNewline(OutputStream stream) {
        FilePutRequired(8, (int)EOL, stream);
        return;
    }


    /*
     * Write some stuff to the output file
     */

    public void PutChar(int X, OutputStream stream) {
        if (X == 0) {
            if (HostFlag && TextFlag && VariableFlag) {
                PutNewline(stream);
            }
        } else {
            if (HostFlag && TextFlag) {
                if (VariableFlag) {
                    if (X == RecordMark) {
                        PutNewline(stream);
                    } else {
                        FilePutRequired( 8, EbcToAsc[X-1], stream);
                    }
                } else {
                    FilePutRequired( 8, EbcToAsc[X-1], stream);
                    OutputPhase++;
                    if (OutputPhase == RecordLength) {
                        PutNewline(stream);
                        OutputPhase = 0;
                    }
                }
            } else {
                if (X < RecordMark) { /* discard record marks */
                    FilePutRequired( 8, X-1, stream);
                }
            }
        }
    }

    /*
     * Put multiple chars to the output file
     * It looks like the logic is connected to the logic of the spack output
     * code, so probably can't be separated. X is some kind of indication of
     * how much data to write, but the data is taken directly from Tree
     * The stack isn't initialized each time, as it looks like only bits we
     * have written to on this iteration will be read from
     */
    public class StackType {
        int Head;
        int Data[] = new int[StackSize+1];
    }

    StackType Stack = new StackType();


    public void PutChars(int X, OutputStream outstream) {
        Stack.Head = 0;

        while (true) {
            while (X > CodeSize) {
                Stack.Head++;
                Stack.Data[Stack.Head] = Tree[X].Right;
                X = Tree[X].Left;
            }
            PutChar( X, outstream);

            if (Stack.Head > 0) {
                X = Stack.Data[Stack.Head];
                Stack.Head--;
            } else
                break;
        }

    }


    /*Inner classe and globals used as data structures for spack decode*/
    /* Might be possible to wrap the Tree structure in a class along with
     * its initializer method, which would be cleaner, but unnecessarily anal??
     */
    public class TreeRecord {

        int Left;
        int Right;
        int Back;
        int NextCount;
    }

    int TreeAvail;

    TreeRecord Tree[] = new TreeRecord[TreeSize+1];


    public void TreeInit() {

        for (int i =0; i < Tree.length; i ++) {
            Tree[i] = new TreeRecord();
        }

        int init_index = Base;
        while (init_index <= CodeSize) {
            Tree[init_index].Left  = None;
            Tree[init_index].Right = init_index++;
        }
        for (init_index = CodeSize+1; init_index <= TreeSize-1; init_index++) {
            Tree[init_index].NextCount  = init_index+1;
            Tree[init_index].Left  = None;
            Tree[init_index].Right = None;
        }
        Tree[TreeSize].NextCount = None;
        Tree[Base].NextCount = Base;
        Tree[Base].Back = Base;
        for (init_index = 1; init_index <= CodeSize; init_index++) {
            Tree[init_index].NextCount = None;
        }
        TreeAvail = CodeSize+1;
    }


    /*
     * The following methods are all used by the spack decode algorithm
     * The precise use of all of them is unknown!!
     */

    int lru_p = 0, lru_q = 0, lru_r = 0;

    public void LruKill() {
        lru_p = Tree[0].NextCount;
        lru_q = Tree[lru_p].NextCount;
        lru_r = Tree[lru_p].Back;
        Tree[lru_q].Back = lru_r;
        Tree[lru_r].NextCount = lru_q;
        DeleteRef(Tree[lru_p].Left);
        DeleteRef(Tree[lru_p].Right);
        Tree[lru_p].NextCount = TreeAvail;
        TreeAvail = lru_p;
    }

    public void DeleteRef(int dref) {
        if (Tree[dref].NextCount == -1) {
            LruAdd(dref);
        } else {
            Tree[dref].NextCount++;
        }
    }


    int lru_back=0;

    public void LruAdd(int lru_next) {
        lru_back = Tree[Base].Back;
        Tree[lru_next].NextCount = Base;
        Tree[Base].Back = lru_next;
        Tree[lru_next].Back = lru_back;
        Tree[lru_back].NextCount = lru_next;
    }


    int node =0;

    public int GetTreeNode() {
        node = TreeAvail;
        TreeAvail = Tree[node].NextCount;
        return node;
    }


    int forwards = 0, prev = 0;

    public void BumpRef(int bref) {
        if (Tree[bref].NextCount < 0) {
            Tree[bref].NextCount--;
        } else {
            forwards = Tree[bref].NextCount;
            prev = Tree[bref].Back;
            Tree[prev].NextCount = forwards;
            Tree[forwards].Back = prev;
            Tree[bref].NextCount = -1;
        }
    }


    /*
     * Decode logic for a file compressed with the spack algorithm
     * Inputstream should wrap the compressed data, outputstream is where we write
     * the decompressed data to.
     */

    public void Decode1( InputStream stream, OutputStream outstream) {

        if (Debug) {
            System.out.println("Text Flag is: " + TextFlag);
            System.out.println("Host Flag is: " + HostFlag);
            System.out.println("Spack Flag is: " + SpackFlag);
            System.out.println("Decode Flag is: " + DecodeFlag);
            System.out.println("Encode Flag is: " + EncodeFlag);
            System.out.println("Examine Flag is: " + ExamineFlag);
            System.out.println("Fixed Flag is: " + FixedFlag);
            System.out.println("Variable Flag is: " + VariableFlag);
            System.out.println("Help Flag is: " + HelpFlag);
        }



        TreeAvail = 0;
        int N = 0, G = 0, H = 0;
        int H1 = 0, H2 = 0, H3 = 0, H4 = 0, H5 = 0, H6 = 0, H7 = 0;
        if (HostFlag) { /* examine vm header */
            H1 = FileGetRequired(8, stream); /* terse version */
            H2 = FileGetRequired(8, stream); /* variable-length record flag */
            H3 = FileGetRequired(16, stream); /* record length */
            H4 = FileGetRequired(16, stream); /* filler */
            H5 = FileGetRequired(16, stream); /* filler */
            H6 = FileGetRequired(16, stream); /* filler */
            H7 = FileGetRequired(16, stream); /* filler */
            AssertString( "Invalid File Header: Terse Version Flag", H1 == 5);
            AssertString( "Invalid File Header: Fixed/Variable Block Flag", (H2 == 0) || (H2 == 1));
            if (H3 == 0) {
                AssertString( "Invalid File Header: Zero Record Length", (H6 != 0) || (H7 != 0));
            } else {
                AssertString( "Invalid File Header: Ambiguous Record Length", (H6 == 0) && (H7 == 0));
            }
            if ((H4 & 0x0400) == 0) {
                AssertString( "Invalid File Header: Non-Zero Filler 1", H4 == 0);
                AssertString( "Invalid File Header: Non-Zero Filler 2", H5 == 0);
            }
            VariableFlag = (H2 == 1);
            if (H3 == 0) {
                RecordLength = ((long) H6 << 16) | (long) H7;
            } else {
                RecordLength = (long) H3;
            }
        } else {
            H1 = FileGetRequired(8, stream); /* terse version */
            H2 = FileGetRequired(8, stream); /* validation flag 1 */
            H3 = FileGetRequired(8, stream); /* validation flag 2 */
            H4 = FileGetRequired(8, stream); /* validation flag 3 */
            AssertString( "Invalid File Header: Terse Version Flag"   , (H1 == 1) || (H1 == 7));
            AssertString( "Invalid File Header: Validation Flag One"  , H2 == 0x89);
            AssertString( "Invalid File Header: Validation Flag Two"  , H3 == 0x69);
            AssertString( "Invalid File Header: Validation Flag Three", H4 == 0xA5);
        }

        TreeInit();
        Tree[TreeSize-1].NextCount = None;

        H = GetBlok(stream);
        PutChars( H, outstream);

        while (H != EndOfFile) {

            G = GetBlok(stream);
            if (TreeAvail == None) {
                LruKill();
            }
            PutChars(G, outstream);
            N = GetTreeNode();
            Tree[N].Left = H;
            Tree[N].Right = G;
            BumpRef(H);
            BumpRef(G);
            LruAdd(N);
            H = G;
        }

    }


    /* 
     * Decode the file on the other end of the input stream using a non spack decode.
     * Write the output to the output stream.
     * Assume that both streams are initialized and ready to be read from/written to.
     */
    public void Decode2(InputStream stream, OutputStream outstream) {

        if (Debug) {
            System.out.println("Decode2");
        }

        if (Debug) {
            System.out.println("Text Flag is: " + TextFlag);
            System.out.println("Host Flag is: " + HostFlag);
            System.out.println("Spack Flag is: " + SpackFlag);
            System.out.println("Decode Flag is: " + DecodeFlag);
            System.out.println("Encode Flag is: " + EncodeFlag);
            System.out.println("Examine Flag is: " + ExamineFlag);
            System.out.println("Fixed Flag is: " + FixedFlag);
            System.out.println("Variable Flag is: " + VariableFlag);
            System.out.println("Help Flag is: " + HelpFlag);
        }

        
        int  H1 = 0, H2 = 0, H3 = 0, H4 = 0, H5 = 0, H6 = 0, H7 = 0;
        int x = 0, d = 0, y = 0, q = 0, r = 0, e = 0, p = 0, h = 0;

        H2 = 1 + AscToEbcDef[' '];

        for (H1 = 258; H1 < 4096; H1++) {
          Father [H1] = H2;
          CharExt[H1] = 1 + AscToEbcDef[' '];
          H2 = H1;
        }

        for (H1 = 258; H1 < 4095; H1++) {
          Backward[H1+1] = H1  ;
          Forward [H1  ] = H1+1;
        }

        Backward [0] = 4095;
        Forward [0] = 258;
        Backward [258] = 0;
        Forward [4095] = 0;

        if (Debug) {
            System.out.println("Done setup in Decode2. About to read from the file");
        }


        H1 = FileGetRequired(8, stream); /* terse version */
        H2 = FileGetRequired(8, stream); /* variable-length record flag */
        H3 = FileGetRequired(16, stream); /* record length */
        H4 = FileGetRequired(16, stream); /* filler */
        H5 = FileGetRequired(16, stream); /* filler */
        H6 = FileGetRequired(16, stream); /* filler */
        H7 = FileGetRequired(16, stream); /* filler */


        if (Debug) {
            System.out.println("Have read header info again.");
            System.out.println("h1 is " +H1 +" h2 is " +H2 +" h3 is " +H3 +" h4 is " +H4 +" h5 is " +H5 +" h6 is " +H6 +" h7 is " +H7);
        }



        AssertString( "Invalid File Header: Terse Version Flag", H1 == 2);
        AssertString( "Invalid File Header: Fixed/Variable Block Flag", (H2 == 0) || (H2 == 1));
        if (H3 == 0) {
            AssertString( "Invalid File Header: Zero Record Length", (H6 != 0) || (H7 != 0));
        } else {
            AssertString( "Invalid File Header: Ambiguous Record Length", (H6 == 0) && (H7 == 0));
        }
        if ((H4 & 0x0400) == 0) {
            AssertString( "Invalid File Header: Non-Zero Filler 1", H4 == 0);
            AssertString( "Invalid File Header: Non-Zero Filler 2", H5 == 0);
        }

        if (Debug) {
            System.out.println("Checked the headers");
        }


        VariableFlag = (H2 == 1);
        if (H3 == 0) {
            RecordLength = ( ((long)H6) << 16) | ((long)H7);
        } else {
            RecordLength = (long)H3;
        }

        x=0;
        d = GetBlok(stream);

        while (d != 0) {
            h = 0;
            y = Backward[0];
            q = Backward[y];
            Backward[0] = q;
            Forward [q] = 0;
            h = y;
            p = 0;
            while (d > 257) {
                q = Forward [d];
                r = Backward[d];
                Forward [r] = q;
                Backward[q] = r;
                Forward [d] = h;
                Backward[h] = d;
                h = d;
                e = Father[d];
                Father[d] = p;
                p = d;
                d = e;
            }
            q = Forward[0];
            Forward [y] = q;
            Backward[q] = y;
            Forward [0] = h;
            Backward[h] = 0;
            CharExt [x] = d;
            PutChar(d, outstream);
            x = y;
            while (p != 0) {
                e = Father[p];
                PutChar( CharExt[p], outstream);
                Father[p] = d;
                d = p;
                p = e;
            }
            Father[y] = d;
            d = GetBlok(stream);
        }

        return;
    }

    /*
     * Currently working towards a decompress in binary mode only implementation.
     * We assume that we get "TerseDecompress <input file> <output file>". Otherwise exit with
     * an error message.
     */

    public void process (String args[]) {

        if (args.length == 0 || args.length > 3) {
            System.out.println(DetailedHelp);
            System.out.println(Version);
            System.exit(0);
            /*
            if (args[0].equals("-b")) {
                TextFlag = false;
                System.out.println("Using binary decompression. No ascii/ebcdic conversion will be performed");
            } else {
                System.out.println("Unsupported option: " +args[0]);
            }
        } else {
            System.out.println("Text mode decompression. Ascii/ebcdic conversion will be done");
            */
        }

        if (args.length == 1) {
            System.out.println(DetailedHelp);
            System.out.println(Version);
            System.exit(0);
        }

        if(Debug) {
            System.out.println("Input args exist. About to check header");
        }


        boolean header_rv;
        /*Apparently need to do this to set the spack flag, but the results are thrown away*/
        header_rv = CheckHeader(new File(args[0]));

        if (!header_rv) {
            System.err.println("Failed to read header of input file.");
            System.exit(1);
        }
        
        if(Debug) {
            System.out.println("Header is checked. About to open streams.");
        }


        try {
            InputFile = new File(args[0]);
            InputFileStream = new FileInputStream(InputFile);
            BufferedStream = new BufferedInputStream(InputFileStream);

        } catch (Exception e) {
            System.err.println("Can't open input file: " +InputFile);
            System.exit(1);
        }

        try {
            OutputFile = new File(args[1]);
            OutputFileStream = new FileOutputStream(OutputFile);
            OutputBufferedStream = new BufferedOutputStream(OutputFileStream);

        } catch (Exception e) {
            System.err.println("Can't open output file: " +OutputFile);
            System.exit(1);
        }

        if (args.length == 3) {
            TextFlag = false;
            System.out.println("3rd argument (" +args[2] +") found, so using binary mode.");
        }


        if(Debug) {
            System.out.println("Input and output streams opened. About to decode");
        }

        System.out.println("Attempting to decompress input file (" +InputFile +") to output file (" +OutputFile +")");

        if (!SpackFlag) {
            Decode2(BufferedStream, OutputBufferedStream);
        } else {
            Decode1(BufferedStream, OutputBufferedStream);
        }

        try {
            BufferedStream.close();
            OutputBufferedStream.flush();
            OutputBufferedStream.close();
        } catch (IOException e) {
            System.err.println("Error while closing file streams");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Processing completed");
        if (Debug) {
            System.err.println("Read " +red +" bytes");
        }

    }

    public static void main (String args[]) {

        TerseDecompress tersed = new TerseDecompress();
        tersed.process(args);
    }

}
