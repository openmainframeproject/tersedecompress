package com.blackhillsoftware.terse;

import java.io.DataInputStream;
import java.io.IOException;

/*
 * Data structure used only when checking the header initially
 */

class TerseHeader {

    public int VersionFlag;
    public int VariableFlag;
    public int RecordLen1;
    public int Flags;
    public int Ratio;
    public int BlockSize;
    public int  RecordLen2;

    public int  RecordLength;
    
    public boolean RecfmV = false;
    
    /*Defaults for dump types*/
    boolean TextFlag = true;
    boolean HostFlag = true;
    boolean SpackFlag = true;

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
     
    /*
     * Check that the header of an input tersed file is consistent and set some of the static flags
     * associated with it.
     */

    static TerseHeader CheckHeader(DataInputStream datastream) throws IOException 
    {
        TerseHeader header = new TerseHeader();    

        header.VersionFlag = datastream.readUnsignedByte();
        
        switch (header.VersionFlag) {
        case 0x01: /* native binary mode, 4 byte header, versions 1.2+ */
        case 0x07: /* native binary mode, 4 byte header, versions 1.1- */
        	
            int byte2 = datastream.readUnsignedByte();
            int byte3 = datastream.readUnsignedByte();
            int byte4 = datastream.readUnsignedByte();
            header.RecordLen1 = datastream.readUnsignedShort();
            
            if (byte2 != 0x89 || byte3 != 0x69 || byte4 != 0xA5)
            {
                throw new IOException("Invalid header validation flags");
            }
        	header.HostFlag = false; /* autoswitch to native mode */
        	header.TextFlag = false;

            break;
            
        case 0x02: /* host  PACK compatibility mode, 12 byte header */
        case 0x05: /* host SPACK compatibility mode, 12 byte header */
        	
            header.VariableFlag = datastream.readUnsignedByte();
            header.RecordLen1 = datastream.readUnsignedShort();
            header.Flags = datastream.readUnsignedByte();
            header.Ratio = datastream.readUnsignedByte();
            header.BlockSize = datastream.readUnsignedShort();
            
            // We will assume that the record length doesn't exceed the maximum value
            // for a signed int, for the convenience of using an int instead of a long. 
            header.RecordLen2 = datastream.readInt();
            // but check...
            if (header.RecordLen2 < 0)
            {
            	throw new IOException("Record length exceeds " + Integer.MAX_VALUE);
            
            }
            
       		header.SpackFlag = (header.VersionFlag == 0x05);
        	
            if ((header.VariableFlag != 0x00) && (header.VariableFlag != 0x01))
            	throw new IOException("Record format flag not recognized : " + Integer.toHexString(header.VariableFlag));
            
            if (header.RecordLen1 == 0 && header.RecordLen2 == 0)
            	throw new IOException("Record length is 0");
            
            if (header.RecordLen1 != 0 && header.RecordLen2 != 0 
            		&& header.RecordLen1 != header.RecordLen2)
            	throw new IOException("Ambiguous record length");
            
            header.RecordLength = header.RecordLen1 != 0 ? header.RecordLen1 : header.RecordLen2;
            
            header.RecfmV = (header.VariableFlag == 0x01);
            
            // Preserve checks from previous version, I don't know why these cases are invalid 
            if ((header.Flags & Constants.FLAGMVS) == 0) {
                if (    header.Flags != 0) throw new IOException("Flags specified for non-MVS");
                if (    header.Ratio != 0) throw new IOException("Ratio specified for non-MVS");
                if (header.BlockSize != 0) throw new IOException("BlockSize specified for non-MVS");
            }
            
        	header.HostFlag = true;

            break;
        default:
            throw new IOException("Terse header version not recognized : " + Integer.toHexString(header.VersionFlag));
        }
        
        return header;

    }


}
