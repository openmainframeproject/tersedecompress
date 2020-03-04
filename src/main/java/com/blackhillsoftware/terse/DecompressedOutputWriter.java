package com.blackhillsoftware.terse;

import java.io.IOException;
import java.io.OutputStream;

class DecompressedOutputWriter 
{
	OutputStream stream;
	
	boolean HostFlag; 
	boolean TextFlag;
	boolean VariableFlag;
	
    long         OutputPhase   = 0    ; /* position in fixed-length output record   */
    long         OutputTotal   = 0    ; /* total number of bytes                    */
    long         RecordLength; /* host perspective record length           */
	
	public DecompressedOutputWriter(TerseHeader header, OutputStream outstream)
	{
		this.stream = outstream;
		this.RecordLength = header.RecordLength;
		this.HostFlag = header.HostFlag; 
		this.TextFlag = header.TextFlag;
		this.VariableFlag = header.RecfmV;
	}
	
	
    /* Write a new line to the output file*/
    /* This only works when a new line is a single /n. Which it may not be on Windows */
    public void PutNewline() throws IOException {
        FilePutRequired(8, (int)Constants.EOL);
        return;
    }


    /*
     * Write some stuff to the output file
     */

    public void PutChar(int X) throws IOException {
        if (X == 0) {
            if (HostFlag && TextFlag && VariableFlag) {
                PutNewline();
            }
        } else {
            if (HostFlag && TextFlag) {
                if (VariableFlag) {
                    if (X == Constants.RECORDMARK) {
                        PutNewline();
                    } else {
                        FilePutRequired( 8, Constants.EbcToAsc[X-1]);
                    }
                } else {
                    FilePutRequired( 8, Constants.EbcToAsc[X-1]);
                    OutputPhase++;
                    if (OutputPhase == RecordLength) {
                        PutNewline();
                        OutputPhase = 0;
                    }
                }
            } else {
                if (X < Constants.RECORDMARK) { /* discard record marks */
                    FilePutRequired( 8, X-1);
                }
            }
        }
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

    public boolean FilePutRequired (int Bits, int Value) throws IOException {
        while (Bits > 0) {
            if ((Bits > 7) && (OutputMask == 0x80)) {
                OutputValue = ((Value >>> (Bits - 8)) & 0xFF);

                OutputTotal++;
                stream.write(OutputValue);

                Bits = Bits - 8;

            } else {
                if ((Value & Constants.Mask[Bits]) != 0) {
                    OutputValue = OutputValue | OutputMask;
                }
                if (OutputMask == 0x01) {
                        stream.write(OutputValue);

                } else {
                    OutputMask = OutputMask >>> 1;
                }
                Bits--;
            }
        }
        return true;
    }
    

}
