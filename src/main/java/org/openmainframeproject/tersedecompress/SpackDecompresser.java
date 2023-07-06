package org.openmainframeproject.tersedecompress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SpackDecompresser extends TerseDecompresser {
	
	SpackDecompresser(InputStream instream, OutputStream outputStream, TerseHeader header)
	{
		super(instream, outputStream, header);
	}
	
    private int node =0;

    private int TreeAvail;

    private TreeRecord Tree[] = new TreeRecord[Constants.TREESIZE+1];

    StackType Stack = new StackType();

    private void PutChars(int X) throws IOException {
        Stack.Head = 0;

        while (true) {
            while (X > Constants.CODESIZE) {
                Stack.Head++;
                Stack.Data[Stack.Head] = Tree[X].Right;
                X = Tree[X].Left;
            }
            if(X < 0)
            {
    			throw new IOException("Unexpected sequence, seems like file is corrupted");
            }
            PutChar( X );

            if (Stack.Head > 0) {
                X = Stack.Data[Stack.Head];
                Stack.Head--;
            } else
                break;
        }

    }
    
    private void TreeInit() {

        for (int i =0; i < Tree.length; i ++) {
            Tree[i] = new TreeRecord();
        }

        int init_index = Constants.BASE;
        while (init_index <= Constants.CODESIZE) {
            Tree[init_index].Left  = Constants.NONE;
            Tree[init_index].Right = init_index++;
        }
        for (init_index = Constants.CODESIZE+1; init_index <= Constants.TREESIZE-1; init_index++) {
            Tree[init_index].NextCount  = init_index+1;
            Tree[init_index].Left  = Constants.NONE;
            Tree[init_index].Right = Constants.NONE;
        }
        Tree[Constants.TREESIZE].NextCount = Constants.NONE;
        Tree[Constants.BASE].NextCount = Constants.BASE;
        Tree[Constants.BASE].Back = Constants.BASE;
        for (init_index = 1; init_index <= Constants.CODESIZE; init_index++) {
            Tree[init_index].NextCount = Constants.NONE;
        }
        TreeAvail = Constants.CODESIZE+1;
    }
    
    private int GetTreeNode() {
        node = TreeAvail;
        TreeAvail = Tree[node].NextCount;
        return node;
    }


    private int forwards = 0, prev = 0;

    private void BumpRef(int bref) {
        if (Tree[bref].NextCount < 0) {
            Tree[bref].NextCount--;
        } else {
            forwards = Tree[bref].NextCount;
            prev = Tree[bref].Back;
            Tree[prev].NextCount = forwards;
            Tree[forwards].Back = prev;
            Tree[bref].NextCount = -1;
        }
    }
    

    /*
     * The following methods are all used by the spack decode algorithm
     * The precise use of all of them is unknown!!
     */

    private int lru_p = 0, lru_q = 0, lru_r = 0;

    private void LruKill() {
        lru_p = Tree[0].NextCount;
        lru_q = Tree[lru_p].NextCount;
        lru_r = Tree[lru_p].Back;
        Tree[lru_q].Back = lru_r;
        Tree[lru_r].NextCount = lru_q;
        DeleteRef(Tree[lru_p].Left);
        DeleteRef(Tree[lru_p].Right);
        Tree[lru_p].NextCount = TreeAvail;
        TreeAvail = lru_p;
    }

    private void DeleteRef(int dref) {
        if (Tree[dref].NextCount == -1) {
            LruAdd(dref);
        } else {
            Tree[dref].NextCount++;
        }
    }


    private int lru_back=0;

    private void LruAdd(int lru_next) {
        lru_back = Tree[Constants.BASE].Back;
        Tree[lru_next].NextCount = Constants.BASE;
        Tree[Constants.BASE].Back = lru_next;
        Tree[lru_next].Back = lru_back;
        Tree[lru_back].NextCount = lru_next;
    }
	
    /*
     * Decode logic for a file compressed with the spack algorithm
     * Inputstream should wrap the compressed data, outputstream is where we write
     * the decompressed data to.
     */

    public void decode() throws IOException {
              
        TreeAvail = 0;
        int N = 0, G = 0, H = 0;
       
        TreeInit();
        Tree[Constants.TREESIZE-1].NextCount = Constants.NONE;

        // Testing showed that SPACK wrote an extra newline at the end of a VB text file,
        // compared to PACK.
        // On investigation, I found that the SPACK code passed the 0x000 end of file
        // marker to PutChars and thence to PutChar.
        // If the file is host LRECL=V and text format, PutChar writes a newline character
        // when the argument is zero. In all other cases, the zero argument is ignored and
        // nothing is written.
        // PACK does not pass the end of file marker to PutChar hence the different output.
        
        // Rearranged the loop and end of file testing so that SPACK does not send the 
        // EOF to PutChars.
        
        // However, the additional newline as written by SPACK might be the more correct
        // format. Should all lines of a text file be terminated by an end of line character?
        // That seems to be how e.g. FTP does it. If so we probably want to write the newline,
        // but write it from both PACK and SPACK - probably when closing the writer.
        
        // Terse will not process an empty file so we probably don't have to distinguish
        // between an empty file and 1 record with no data. 
        
        H = input.GetBlok();

        if (H != Constants.ENDOFFILE)
        {
	        PutChars( H );
            G = input.GetBlok();

        	while (G != Constants.ENDOFFILE) {
	            
	        	if (TreeAvail == Constants.NONE) {
	                LruKill();
	            }
	        	
                PutChars(G);
	            N = GetTreeNode();
	            Tree[N].Left = H;
	            Tree[N].Right = G;
	            BumpRef(H);
	            BumpRef(G);
	            LruAdd(N);
	            H = G;
	            G = input.GetBlok();	    
	        }
        }

    }

}
