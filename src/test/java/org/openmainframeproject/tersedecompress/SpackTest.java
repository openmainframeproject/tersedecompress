// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

package org.openmainframeproject.tersedecompress;

import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

/*
 * Unit tests for the SPACK decoder's guards against corrupt input. They set
 * up the decoder's tree directly instead of decoding a file.
 */
public class SpackTest {
    private static final int FIRST_NODE = Constants.CODESIZE + 1;

    private SpackDecompresser decoder;

    @Before public void setUp() throws IOException {
        TerseHeader header = new TerseHeader();
        header.VersionFlag = 0x05;
        header.SpackFlag = true;
        header.VariableFlag = true;
        decoder = new SpackDecompresser(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(),
                header, false);
        decoder.TreeInit();
    }

    /* Make a chain of depth left links ending in literal 1, with literal 1 as every right child. */
    private int makeLeftChain(int depth) {
        for (int i = 0; i < depth; i++) {
            decoder.Tree[FIRST_NODE + i].Left = i == 0 ? 1 : FIRST_NODE + i - 1;
            decoder.Tree[FIRST_NODE + i].Right = 1;
        }
        return FIRST_NODE + depth - 1;
    }

    @Test public void deepTreeWithinStackLimitDecodes() throws IOException {
        decoder.PutChars(makeLeftChain(Constants.STACKSIZE));
    }

    @Test public void treeDeeperThanStackIsRejected() {
        int top = makeLeftChain(Constants.STACKSIZE + 1);
        assertThrows(IOException.class, () -> decoder.PutChars(top));
    }

    @Test public void nodeCanLinkOtherCodes() throws IOException {
        decoder.AddNode(1, 2);
    }

    @Test public void reusedNodeAsRightChildIsRejected() {
        int next = decoder.TreeAvail;
        assertThrows(IOException.class, () -> decoder.AddNode(1, next));
    }

    @Test public void reusedNodeAsLeftChildIsRejected() {
        int next = decoder.TreeAvail;
        assertThrows(IOException.class, () -> decoder.AddNode(next, 1));
    }
}
