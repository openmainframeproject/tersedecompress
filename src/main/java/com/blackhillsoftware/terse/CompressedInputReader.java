package com.blackhillsoftware.terse;

import java.io.*;

class CompressedInputReader 
{
	InputStream stream;

	public CompressedInputReader(InputStream instream)
	{
		this.stream = instream;
	}
	
	
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

    public int FileGetRequired(int Bits) {

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
                    temp = buffer & (long)(CodePages.Mask[index]);
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

    int GetBlok() {
        return FileGetRequired(12);
    }

}
