package org.openmainframeproject.tersedecompress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class NonSpackDecompresser extends TerseDecompresser {
		
	NonSpackDecompresser(InputStream instream, OutputStream outputStream, TerseHeader header, boolean textMode) throws IOException
	{
		super(instream, outputStream, header, textMode);
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
    	      
        int  H1 = 0, H2 = 0;
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
