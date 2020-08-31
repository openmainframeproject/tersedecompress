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
	
    byte[] lineseparator = System.lineSeparator().getBytes();
    
	public DecompressedOutputWriter(TerseHeader header, OutputStream outstream)
	{
		this.stream = outstream;
		this.RecordLength = header.RecordLength;
		this.HostFlag = header.HostFlag; 
		this.TextFlag = header.TextFlag;
		this.VariableFlag = header.RecfmV;
	}
	
	
    /* Write a new line to the output file*/
    public void PutNewline() throws IOException {
    	stream.write(lineseparator);
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
                    	stream.write(Constants.EbcToAsc[X-1]);
                    }
                } else {
                	stream.write(Constants.EbcToAsc[X-1]);
                    OutputPhase++;
                    if (OutputPhase == RecordLength) {
                        PutNewline();
                        OutputPhase = 0;
                    }
                }
            } else {
                if (X < Constants.RECORDMARK) { /* discard record marks */
                	stream.write(X-1);
                }
            }
        }
    }
}
