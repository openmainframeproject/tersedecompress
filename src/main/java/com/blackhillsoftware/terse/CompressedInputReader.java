package com.blackhillsoftware.terse;

import java.io.*;

class CompressedInputReader implements AutoCloseable
{
	InputStream stream;

	public CompressedInputReader(InputStream instream)
	{
		this.stream = instream;
	}
	
	int bitsAvailable = 0;
	int savedBits = 0;
	int red = 0;
	    
    /*
     * Read in 12 bits of data, and put them in the bottom of the returned int
     */

    int GetBlok() throws IOException {
   	
    	if (bitsAvailable == 0)
    	{
    		int byte1 = stream.read();
    		if (byte1 == -1)
    		{
    			return Constants.ENDOFFILE;
    		}
    		red++;
    		int byte2 = stream.read();
    		if (byte2 == -1)
    		{
    			throw new IOException("Tried to read 12 bits but found EOF after reading 8 bits.");
    		}
    		red++;
    		// save the last 4 bits of the second byte
    		savedBits = byte2 & 0x0F;
    		bitsAvailable = 4;
    		
    		return (byte1 << 4) | (byte2 >> 4);
    	}
    	else
    	{
    		if (bitsAvailable != 4)
    		{
    			// should never happen, if it does we made an error
    			throw new IOException("Unexpected count of bits available");
    		}
    		
    		int byte2 = stream.read();
    		if (byte2 == -1)
    		{
    			// assume the 4 bits in the last block were the last real data and 
    			// these 4 bits only exist because you can't write 1/2 a byte 
    			// i.e. this is EOF
    			return Constants.ENDOFFILE;
    		}
    		red++;
    		bitsAvailable = 0;
    		return (savedBits << 8) | byte2;
    	}
    }

	@Override
	public void close() throws Exception {
		stream.close();
	}
}
