package com.blackhillsoftware.terse;

/*
 * Data structure used only when checking the header initially
 */

class TerseHeader {

    public int VersionFlag;
    public int VariableFlag;
    public int RecordLen1;
    public int Flags;
    public int Ratio;
    public int BlockSize;
    public long  RecordLen2;


    public String toString() {
    
        return new String (
            "\n"
            +"Version flag is " + VersionFlag +"\n"
            +"Variable Flag is " + VariableFlag +"\n"
            +"RecordLen1 is " + RecordLen1 +"\n"
            +"Flags are " + Flags +"\n"
            +"Ratio is " + Ratio +"\n"
            +"Block Size is " + BlockSize +"\n"
            +"RecordLen2 is " + RecordLen2 +"\n"
            );
    
    }

}
