package org.openmainframeproject.tersedecompress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

abstract class TerseDecompresser implements AutoCloseable
{
	TerseBlockReader input;
	ByteBuffer record;
	DataOutputStream stream;
    final TerseHeader header;
    final boolean TextFlag;
    /* variable: maximum record length; fixed and native: length that ends a record */
    final int recordLimit;

    public abstract void decode() throws IOException;
    
    public static TerseDecompresser create(InputStream inputStream, OutputStream outputStream, boolean textMode) throws IOException
    {
        DataInputStream input = new DataInputStream(new BufferedInputStream(inputStream));
        TerseHeader header_rv = TerseHeader.CheckHeader(input);
        
        if (!header_rv.SpackFlag) {
        	return new NonSpackDecompresser(input, outputStream, header_rv, textMode);
        } else {
        	return new SpackDecompresser(input, outputStream, header_rv, textMode);
        }
    }
    
	public TerseDecompresser(InputStream instream, OutputStream outputStream, TerseHeader header, boolean textMode)
	{		
		this.header = header;
		// Native mode files have no host records: never convert them, and write them out in chunks.
		boolean nativeMode = header.VersionFlag == 0x01;
		this.TextFlag = textMode && !nativeMode;
		this.recordLimit = header.VariableFlag || nativeMode ? Constants.MAXRECORD : header.RecordLength;
		this.input = new TerseBlockReader(instream);
		this.stream = new DataOutputStream(new BufferedOutputStream(outputStream));

		// Don't trust a huge record length from the header: start small and grow as needed.
		this.record = ByteBuffer.allocate(Math.min(recordLimit, Constants.MAXRECORD));
	}
	
    /* Write a new line to the output file*/
    public void endRecord() throws IOException 
    {
    	if (header.VariableFlag && !TextFlag)
    	{
    		// write a RDW
    		int recordlength = record.position() + 4;
    		int rdw = recordlength << 16;
    		stream.writeInt(rdw);
    	}
        stream.write(record.array(), 0, record.position());
        if (TextFlag) {
            stream.writeByte(0x0A);
        }
        record.clear();
    }

    /*
     * Write some stuff to the output record
     */

    public void PutChar(int X) throws IOException {
        if (X == 0 && TextFlag && header.VariableFlag == true) {
            endRecord();
        } else if (X == Constants.RECORDMARK) {
            if (header.VariableFlag) {
                endRecord();
            }
        } else {
            if (X < 1 || X > 256) {
                throw new IOException("Invalid literal in decompressed record");
            }
            if (record.position() >= recordLimit) {
                throw new IOException("Record size exceeds expected maximum. File may be corrupted or not a valid Terse file.");
            }
            if (record.position() == record.capacity()) {
                ByteBuffer larger = ByteBuffer.allocate((int) Math.min(recordLimit, 2L * record.capacity()));
                record.flip();
                record = larger.put(record);
            }
            record.put(TextFlag ? (byte) Constants.EbcToAsc[X - 1] : (byte) (X - 1));
            if (header.VariableFlag != true && record.position() == recordLimit) {
                endRecord();
            }
        }
    }

	@Override
	public void close() throws IOException {
        TerseBlockReader reader = this.input;
        DataOutputStream output = this.stream;
        try (reader; output) {
            if (record.position() > 0 || TextFlag && header.VariableFlag) {
                endRecord();
            }
        }
	}
}
