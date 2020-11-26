package com.blackhillsoftware.terse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class DecompressedOutputWriter implements AutoCloseable
{
	DataOutputStream stream;
	ByteArrayOutputStream record;
	
	boolean HostFlag; 
	boolean TextFlag;
	boolean VariableFlag;
	
    long         OutputTotal   = 0    ; /* total number of bytes                    */
    long         RecordLength; /* host perspective record length           */
	
    byte[] lineseparator = System.lineSeparator().getBytes();
    
	public DecompressedOutputWriter(TerseHeader header, OutputStream outstream)
	{
		this.stream = new DataOutputStream(outstream);
		this.record = new ByteArrayOutputStream();
		this.RecordLength = header.RecordLength;
		this.HostFlag = header.HostFlag; 
		this.TextFlag = header.TextFlag;
		this.VariableFlag = header.RecfmV;
	}
	
    /* Write a new line to the output file*/
    public void endRecord() throws IOException 
    {
    	if (VariableFlag && !TextFlag)
    	{
    		// write a RDW
    		int recordlength = record.size() + 4;
    		int rdw = recordlength << 16;
    		stream.writeInt(rdw);
    	}
    	
    	stream.write(record.toByteArray());
    	record.reset();
    	
    	if (TextFlag)
    	{
    		stream.write(lineseparator);
    	}    		
    }

    /*
     * Write some stuff to the output record
     */

    public void PutChar(int X) throws IOException {
        if (X == 0) {
            if (HostFlag && TextFlag && VariableFlag) {
                endRecord();
            }
        } else {
            if (HostFlag && TextFlag) {
                if (VariableFlag) {
                    if (X == Constants.RECORDMARK) {
                        endRecord();
                    } else {
                    	record.write(Constants.EbcToAsc[X-1]);
                    }
                } else {
                	record.write(Constants.EbcToAsc[X-1]);
                    if (record.size() == RecordLength) {
                        endRecord();
                    }
                }
            } 
            else 
            {
                if (X == Constants.RECORDMARK) 
                {
                	if (VariableFlag)
                	{
                		endRecord();
                	}
                	/* else discard record marks */
                }
                else
                {
                	record.write(X-1);
                }
            }
        }
    }

	@Override
	public void close() throws Exception {
		if (record.size() > 0)
		{
			endRecord();
		}
		this.stream.close();
	}
}
