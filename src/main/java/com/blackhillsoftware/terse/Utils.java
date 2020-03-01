package com.blackhillsoftware.terse;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class Utils {
    /*
     * Reads a 32 bit unsigned value from the Input stream and puts it into a long variable.
     * Only used by CheckHeader().
     */
    public static long readUnsignedInt(InputStream stream) throws IOException {

        long rv =0 ;
        long temp;

        for (int i=0; i < 4; i++) {
            int bytes = stream.read();
            if (bytes < 0) {
                throw new EOFException("End of File");
            }
            temp = ((long)bytes) << ((3-i)*8);
            rv = rv + temp;
        }
        return rv;
    }


    /*
     * Couple of utility methods to do some bit manipulation.
     * Returns the lowest 8 bits of an 16bit value stored in an int
     */
    public static int Lo (int value) {
        return (value & 0xFF) ;
    }


    /*
     * Returns the Hi 8 bits of a 16 bit value stored in an int.
     */
    public static int Hi(int value) {
        value = value & 0xFF00;
        value  = value >>>8;
        return value;
    }

    
    /*
     * If condition is false, print a string to stderr and exit
     */
    public static void AssertString(String message, boolean condition) {
        if (!condition) {
            System.err.println(message);
            System.exit(1);
        }
    }

}
