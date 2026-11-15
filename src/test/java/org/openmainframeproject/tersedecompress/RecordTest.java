// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

package org.openmainframeproject.tersedecompress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.junit.Test;

public class RecordTest {
    @Test public void pdsPackHeader() throws Exception {
        // https://public.dhe.ibm.com/s390/mvs/tools/IMBDSHIP.JCL.TRSD
        byte[] data = {7, 0, 0, 80, 12, 3, 109, 16, 0, 0, 0, 0};
        TerseHeader header = TerseHeader.CheckHeader(new DataInputStream(new ByteArrayInputStream(data)));
        assertFalse(header.SpackFlag);
        assertFalse(header.VariableFlag);
        assertEquals(80, header.RecordLength);
        assertEquals(27920, header.BlockSize);
    }

    @Test public void pdsSpackHeader() throws Exception {
        // https://public.dhe.ibm.com/s390/cbpacs/fixes/R17.SCPPLOAD.TRS
        byte[] data = {9, 1, 24, 0, (byte) 140, 2, 24, 0, 0, 0, 0, 0};
        TerseHeader header = TerseHeader.CheckHeader(new DataInputStream(new ByteArrayInputStream(data)));
        assertTrue(header.SpackFlag);
        assertTrue(header.VariableFlag);
        assertEquals(6144, header.RecordLength);
        assertEquals(6144, header.BlockSize);
    }

    /* Append 12-bit codes to a header, followed by the end marker (0). */
    private static byte[] encode(byte[] header, int[] codes) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.writeBytes(header);
        int bits = 0, available = 0;
        for (int i = 0; i <= codes.length; i++) {
            bits = (bits << 12) | (i < codes.length ? codes[i] : 0);
            available += 12;
            while (available >= 8) {
                available -= 8;
                data.write(bits >> available);
            }
        }
        if (available != 0) data.write(bits << (8 - available));
        return data.toByteArray();
    }

    private static byte[] variableRecords(int... lengths) {
        int[] codes = new int[java.util.Arrays.stream(lengths).map(length -> length + 1).sum()];
        int position = 0;
        for (int length : lengths) {
            java.util.Arrays.fill(codes, position, position + length, 1);
            position += length;
            codes[position++] = Constants.RECORDMARK;
        }
        return encode(new byte[] {5, 1, 127, -1, 0, 0, 0, 0, 0, 0, 0, 0}, codes);
    }

    private static byte[] decode(byte[] data, boolean textMode) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (TerseDecompresser decoder = TerseDecompresser.create(new ByteArrayInputStream(data), output, textMode)) {
            decoder.decode();
        }
        return output.toByteArray();
    }

    @Test public void nativeModeIgnoresHeaderRecordLength() throws Exception {
        // Native mode has no host records, so a zero or short record length in the header must not matter.
        int length = Constants.MAXRECORD + 10;
        int[] codes = new int[length];
        java.util.Arrays.fill(codes, 0xc2);
        byte[] expected = new byte[length];
        java.util.Arrays.fill(expected, (byte) 0xc1);
        for (int recordLength : new int[] {0, 1}) {
            byte[] header = {1, (byte) 0x89, 0x69, (byte) 0xa5, 0, (byte) recordLength};
            assertArrayEquals(expected, decode(encode(header, codes), true));
        }
    }

    @Test public void hugeFixedRecordLengthDoesNotPreallocate() throws Exception {
        // SPACK, fixed, RecordLen2 = 0x7fffffff: allocating that up front would need 2 GB.
        byte[] header = {5, 0, 0, 0, 0, 0, 0, 0, 0x7f, -1, -1, -1};
        assertArrayEquals(new byte[] {(byte) 0xc1}, decode(encode(header, new int[] {0xc2}), false));
    }

    @Test public void fixedRecordsLongerThanInitialBufferGrow() throws Exception {
        // SPACK, fixed, RecordLen2 = 40000: one full record, longer than the initial buffer.
        int length = 40000;
        int[] codes = new int[length];
        java.util.Arrays.fill(codes, 0xc2);
        byte[] expected = new byte[length];
        java.util.Arrays.fill(expected, (byte) 0xc1);
        byte[] header = {5, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x9c, 0x40};
        assertArrayEquals(expected, decode(encode(header, codes), false));
    }

    @Test public void nativeModePreservesBinaryBytes() throws Exception {
        byte[] data = {1, (byte)137, 105, (byte)165, 0, 1, 12, 32, 0};
        for (boolean textMode : new boolean[] {false, true}) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (TerseDecompresser decoder = TerseDecompresser.create(
                    new ByteArrayInputStream(data), output, textMode)) {
                decoder.decode();
            }
            assertArrayEquals(new byte[] {(byte)193}, output.toByteArray());
        }
    }

    @Test public void fullVariableTextRecordsHaveRoomForNewlines() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (TerseDecompresser decoder = TerseDecompresser.create(
                new ByteArrayInputStream(variableRecords(32767, 32767)), output, true)) {
            decoder.decode();
        }
        byte[] expected = new byte[2 * 32768 + 1];
        expected[32767] = expected[65535] = expected[65536] = 10;
        assertArrayEquals(expected, output.toByteArray());
    }

    @Test public void recordLimitDoesNotGrowAfterFullRecord() throws Exception {
        try (TerseDecompresser decoder = TerseDecompresser.create(
                new ByteArrayInputStream(variableRecords(32767, 32768)),
                new ByteArrayOutputStream(), true)) {
            assertThrows(IOException.class, decoder::decode);
        }
    }

    @Test public void fixedRecordMarksDoNotBecomeBytes() throws Exception {
        // SPACK header, record mark (257), literal (1), and end marker (0).
        byte[] data = {5, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 1, 0, 0};
        for (boolean textMode : new boolean[] {false, true}) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (TerseDecompresser decoder = TerseDecompresser.create(
                    new ByteArrayInputStream(data), output, textMode)) {
                decoder.decode();
            }
            assertArrayEquals(textMode ? new byte[] {0, 10} : new byte[] {0}, output.toByteArray());
        }
    }
}
