package com.blackhillsoftware.terse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class NonSpack extends DecompressedOutputWriter {
		
	NonSpack(InputStream instream, OutputStream outputStream, TerseHeader header)
	{
		super(instream, outputStream, header);
	}
	
    /* 
     * Decode the file on the other end of the input stream using a non spack decode.
     * Write the output to the output stream.
     * Assume that both streams are initialized and ready to be read from/written to.
     */
    public void decode() throws IOException {

        int [] Father = new int[Constants.TREESIZE];
        int [] CharExt = new int[Constants.TREESIZE];
        int [] Backward = new int[Constants.TREESIZE];
        int [] Forward = new int[Constants.TREESIZE];
    	      
        int  H1 = 0, H2 = 0, H3 = 0, H4 = 0, H5 = 0, H6 = 0, H7 = 0;
        int x = 0, d = 0, y = 0, q = 0, r = 0, e = 0, p = 0, h = 0;

        H2 = 1 + Constants.AscToEbcDef[' '];

        for (H1 = 258; H1 < 4096; H1++) {
          Father [H1] = H2;
          CharExt[H1] = 1 + Constants.AscToEbcDef[' '];
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

        if (TerseDecompress.DEBUG) {
            System.out.println("Done setup in Decode2. About to read from the file");
        }

        if (TerseDecompress.DEBUG) {
            System.out.println("Have read header info again.");
            System.out.println("h1 is " +H1 +" h2 is " +H2 +" h3 is " +H3 +" h4 is " +H4 +" h5 is " +H5 +" h6 is " +H6 +" h7 is " +H7);
        }
        
        x=0;
        d = input.GetBlok();

        while (d != Constants.ENDOFFILE) {
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
            PutChar(d);
            x = y;
            while (p != 0) {
                e = Father[p];
                PutChar( CharExt[p]);
                Father[p] = d;
                d = p;
                p = e;
            }
            Father[y] = d;
            d = input.GetBlok();
        }
    }
}
