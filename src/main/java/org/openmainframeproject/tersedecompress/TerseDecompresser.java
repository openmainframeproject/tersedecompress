package org.openmainframeproject.tersedecompress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract class TerseDecompresser implements AutoCloseable
{
	TerseBlockReader input;
	ByteArrayOutputStream record;
	DataOutputStream stream;
	
	boolean HostFlag; 
	boolean TextFlag;
	boolean VariableFlag;
	
    long         OutputTotal   = 0    ; /* total number of bytes                    */
    int         RecordLength; /* host perspective record length           */
	
    byte[] lineseparator = System.lineSeparator().getBytes();
    
    public abstract void decode() throws IOException;
    
    public static TerseDecompresser create(InputStream inputStream, OutputStream outputStream) throws IOException
    {
        DataInputStream input = new DataInputStream(new BufferedInputStream(inputStream));
        TerseHeader header_rv = TerseHeader.CheckHeader(input);
        
        if (!header_rv.SpackFlag) {
        	return new NonSpackDecompresser(input, outputStream, header_rv);
        } else {
        	return new SpackDecompresser(input, outputStream, header_rv);
        }
    }
    
	public TerseDecompresser(InputStream instream, OutputStream outputStream, TerseHeader header)
	{		
		this.RecordLength = header.RecordLength;
		this.HostFlag = header.HostFlag; 
		this.VariableFlag = header.RecfmV;
		this.input = new TerseBlockReader(instream);
		this.stream = new DataOutputStream(new BufferedOutputStream(outputStream));
		
		this.record = new ByteArrayOutputStream(RecordLength);
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
        if (record.size() >= Integer.MAX_VALUE - 10) {
            endRecord();
        }
    }

	@Override
	public void close() throws Exception {
		if (record.size() > 0 
				|| TextFlag && VariableFlag)
		{
			endRecord();
		}
		this.stream.close();
		this.input.close();
	}
}
