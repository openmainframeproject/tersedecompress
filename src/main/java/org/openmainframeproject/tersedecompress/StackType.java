package org.openmainframeproject.tersedecompress;

/*
 * Put multiple chars to the output file
 * It looks like the logic is connected to the logic of the spack output
 * code, so probably can't be separated. X is some kind of indication of
 * how much data to write, but the data is taken directly from Tree
 * The stack isn't initialized each time, as it looks like only bits we
 * have written to on this iteration will be read from
 */
class StackType {
    int Head;
    int Data[] = new int[Constants.STACKSIZE+1];
}
