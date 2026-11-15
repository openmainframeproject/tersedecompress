package org.openmainframeproject.tersedecompress;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.*;

import org.junit.*;

public class AppTest 
{
	static final String location = "test-data";
		
	@Test public void testBinaryPack01() throws Exception { testBinary("FB.A.TXT",        "PACK"); }
	@Test public void testBinaryPack02() throws Exception { testBinary("FB.AAA.TXT",      "PACK"); }
	@Test public void testBinaryPack03() throws Exception { testBinary("FB.ALICE29.TXT",  "PACK"); }
	@Test public void testBinaryPack04() throws Exception { testBinary("FB.ALPHABET.TXT", "PACK"); }
	@Test public void testBinaryPack05() throws Exception { testBinary("FB.ASYOULIK.TXT", "PACK"); }
	@Test public void testBinaryPack06() throws Exception { testBinary("FB.BIBLE.TXT",    "PACK"); }
	@Test public void testBinaryPack07() throws Exception { testBinary("FB.CP.HTML",      "PACK"); }
	@Test public void testBinaryPack08() throws Exception { testBinary("FB.E.COLI",       "PACK"); }
	@Test public void testBinaryPack09() throws Exception { testBinary("FB.FIELDS.C",     "PACK"); }
	@Test public void testBinaryPack10() throws Exception { testBinary("FB.GRAMMAR.LSP",  "PACK"); }
	@Test public void testBinaryPack11() throws Exception { testBinary("FB.KENNEDY.XLS",  "PACK"); }
	@Test public void testBinaryPack12() throws Exception { testBinary("FB.LCET10.TXT",   "PACK"); }
	@Test public void testBinaryPack13() throws Exception { testBinary("FB.PI.TXT",       "PACK"); }
	@Test public void testBinaryPack14() throws Exception { testBinary("FB.PLRABN12.TXT", "PACK"); }
	@Test public void testBinaryPack15() throws Exception { testBinary("FB.PTT5",         "PACK"); }
	@Test public void testBinaryPack16() throws Exception { testBinary("FB.RANDOM.TXT",   "PACK"); }
	@Test public void testBinaryPack17() throws Exception { testBinary("FB.SUM",          "PACK"); }
	@Test public void testBinaryPack18() throws Exception { testBinary("FB.WORLD192.TXT", "PACK"); }
	@Test public void testBinaryPack19() throws Exception { testBinary("FB.XARGS",        "PACK"); }
	@Test public void testBinaryPack20() throws Exception { testBinary("VB.BIBLE.TXT",    "PACK"); }
	@Test public void testBinaryPack21() throws Exception { testBinary("VB.CP.HTML",      "PACK"); }
	@Test public void testBinaryPack22() throws Exception { testBinary("VB.ENWIK8.XML",   "PACK"); }
	@Test public void testBinaryPack23() throws Exception { testBinary("VB.FIELDS.C",     "PACK"); }
	@Test public void testBinaryPack24() throws Exception { testBinary("VB.GRAMMAR.LSP",  "PACK"); }
	@Test public void testBinaryPack25() throws Exception { testBinary("VB.LCET10.TXT",   "PACK"); }
	@Test public void testBinaryPack26() throws Exception { testBinary("VB.WORLD192.TXT", "PACK"); }
	@Test public void testBinaryPack27() throws Exception { testBinary("VB.XARGS",        "PACK"); }
	@Test public void testBinaryPack28() throws Exception { testBinary("VB.A.TXT",        "PACK"); }	
	@Test public void testBinaryPack29() throws Exception { testBinary("VB.AAA.TXT",      "PACK"); }
	@Test public void testBinaryPack30() throws Exception { testBinary("VB.ALPHABET.TXT", "PACK"); }
	@Test public void testBinaryPack31() throws Exception { testBinary("VB.E.COLI",       "PACK"); }
	@Test public void testBinaryPack32() throws Exception { testBinary("VB.PI.TXT",       "PACK"); }
	@Test public void testBinaryPack33() throws Exception { testBinary("VB.RANDOM.TXT",   "PACK"); }
	@Test public void testBinaryPack34() throws Exception { testBinary("VB.ALICE29.TXT",  "PACK"); }
	@Test public void testBinaryPack35() throws Exception { testBinary("VB.ASYOULIK.TXT", "PACK"); }
	@Test public void testBinaryPack36() throws Exception { testBinary("VB.PLRABN12.TXT", "PACK"); }

	
	// The following test fails, but also uncompresses incorrectly using AMATERSE on z/OS.
	// The failure seems to be in the SPACK compression using AMATERSE.
	//@Test public void testBinarySPack01() throws Exception { testBinary("FB.A.TXT",        "SPACK"); }
	@Test public void testBinarySPack02() throws Exception { testBinary("FB.AAA.TXT",      "SPACK"); }
	@Test public void testBinarySPack03() throws Exception { testBinary("FB.ALICE29.TXT",  "SPACK"); }
	@Test public void testBinarySPack04() throws Exception { testBinary("FB.ALPHABET.TXT", "SPACK"); }
	@Test public void testBinarySPack05() throws Exception { testBinary("FB.ASYOULIK.TXT", "SPACK"); }
	@Test public void testBinarySPack06() throws Exception { testBinary("FB.BIBLE.TXT",    "SPACK"); }
	@Test public void testBinarySPack07() throws Exception { testBinary("FB.CP.HTML",      "SPACK"); }
	@Test public void testBinarySPack08() throws Exception { testBinary("FB.E.COLI",       "SPACK"); }
	@Test public void testBinarySPack09() throws Exception { testBinary("FB.FIELDS.C",     "SPACK"); }
	@Test public void testBinarySPack10() throws Exception { testBinary("FB.GRAMMAR.LSP",  "SPACK"); }
	@Test public void testBinarySPack11() throws Exception { testBinary("FB.KENNEDY.XLS",  "SPACK"); }
	@Test public void testBinarySPack12() throws Exception { testBinary("FB.LCET10.TXT",   "SPACK"); }
	@Test public void testBinarySPack13() throws Exception { testBinary("FB.PI.TXT",       "SPACK"); }
	@Test public void testBinarySPack14() throws Exception { testBinary("FB.PLRABN12.TXT", "SPACK"); }
	@Test public void testBinarySPack15() throws Exception { testBinary("FB.PTT5",         "SPACK"); }
	@Test public void testBinarySPack16() throws Exception { testBinary("FB.RANDOM.TXT",   "SPACK"); }
	@Test public void testBinarySPack17() throws Exception { testBinary("FB.SUM",          "SPACK"); }
	@Test public void testBinarySPack18() throws Exception { testBinary("FB.WORLD192.TXT", "SPACK"); }
	@Test public void testBinarySPack19() throws Exception { testBinary("FB.XARGS",        "SPACK"); }
	@Test public void testBinarySPack20() throws Exception { testBinary("VB.BIBLE.TXT",    "SPACK"); }
	@Test public void testBinarySPack21() throws Exception { testBinary("VB.CP.HTML",      "SPACK"); }
	@Test public void testBinarySPack22() throws Exception { testBinary("VB.ENWIK8.XML",   "SPACK"); }
	@Test public void testBinarySPack23() throws Exception { testBinary("VB.FIELDS.C",     "SPACK"); }
	@Test public void testBinarySPack24() throws Exception { testBinary("VB.GRAMMAR.LSP",  "SPACK"); }
	@Test public void testBinarySPack25() throws Exception { testBinary("VB.LCET10.TXT",   "SPACK"); }
	@Test public void testBinarySPack26() throws Exception { testBinary("VB.WORLD192.TXT", "SPACK"); }
	@Test public void testBinarySPack27() throws Exception { testBinary("VB.XARGS",        "SPACK"); }
	// The following test fails, but also uncompresses incorrectly using AMATERSE on z/OS.
	// The failure seems to be in the SPACK compression using AMATERSE.
	//@Test public void testBinarySPack28() throws Exception { testBinary("VB.A.TXT",        "SPACK"); }	
	@Test public void testBinarySPack29() throws Exception { testBinary("VB.AAA.TXT",      "SPACK"); }
	@Test public void testBinarySPack30() throws Exception { testBinary("VB.ALPHABET.TXT", "SPACK"); }
	@Test public void testBinarySPack31() throws Exception { testBinary("VB.E.COLI",       "SPACK"); }
	@Test public void testBinarySPack32() throws Exception { testBinary("VB.PI.TXT",       "SPACK"); }
	@Test public void testBinarySPack33() throws Exception { testBinary("VB.RANDOM.TXT",   "SPACK"); }
	@Test public void testBinarySPack34() throws Exception { testBinary("VB.ALICE29.TXT",  "SPACK"); }
	@Test public void testBinarySPack35() throws Exception { testBinary("VB.ASYOULIK.TXT", "SPACK"); }
	@Test public void testBinarySPack36() throws Exception { testBinary("VB.PLRABN12.TXT", "SPACK"); }
	
	@Test public void testTextPack01() throws Exception { testText("FB.A.TXT",        "PACK"); }
	@Test public void testTextPack02() throws Exception { testText("FB.AAA.TXT",      "PACK"); }
	@Test public void testTextPack04() throws Exception { testText("FB.ALPHABET.TXT", "PACK"); }
	@Test public void testTextPack06() throws Exception { testText("FB.BIBLE.TXT",    "PACK"); }
	@Test public void testTextPack07() throws Exception { testText("FB.CP.HTML",      "PACK"); }
	@Test public void testTextPack08() throws Exception { testText("FB.E.COLI",       "PACK"); }
	@Test public void testTextPack09() throws Exception { testText("FB.FIELDS.C",     "PACK"); }
	@Test public void testTextPack10() throws Exception { testText("FB.GRAMMAR.LSP",  "PACK"); }
	@Test public void testTextPack12() throws Exception { testText("FB.LCET10.TXT",   "PACK"); }
	@Test public void testTextPack13() throws Exception { testText("FB.PI.TXT",       "PACK"); }
	@Test public void testTextPack16() throws Exception { testText("FB.RANDOM.TXT",   "PACK"); }
	@Test public void testTextPack18() throws Exception { testText("FB.WORLD192.TXT", "PACK"); }
	@Test public void testTextPack19() throws Exception { testText("FB.XARGS",        "PACK"); }
	@Test public void testTextPack20() throws Exception { testText("VB.BIBLE.TXT",    "PACK"); }
	@Test public void testTextPack21() throws Exception { testText("VB.CP.HTML",      "PACK"); }
	@Test public void testTextPack23() throws Exception { testText("VB.FIELDS.C",     "PACK"); }
	@Test public void testTextPack24() throws Exception { testText("VB.GRAMMAR.LSP",  "PACK"); }
	@Test public void testTextPack25() throws Exception { testText("VB.LCET10.TXT",   "PACK"); }
	@Test public void testTextPack26() throws Exception { testText("VB.WORLD192.TXT", "PACK"); }
	@Test public void testTextPack27() throws Exception { testText("VB.XARGS",        "PACK"); }
	@Test public void testTextPack28() throws Exception { testText("VB.A.TXT",        "PACK"); }	
	@Test public void testTextPack29() throws Exception { testText("VB.AAA.TXT",      "PACK"); }
	@Test public void testTextPack30() throws Exception { testText("VB.ALPHABET.TXT", "PACK"); }
	@Test public void testTextPack31() throws Exception { testText("VB.E.COLI",       "PACK"); }
	@Test public void testTextPack32() throws Exception { testText("VB.PI.TXT",       "PACK"); }
	@Test public void testTextPack33() throws Exception { testText("VB.RANDOM.TXT",   "PACK"); }
	
	// The following test fails, but the file also uncompresses incorrectly using AMATERSE on z/OS.
	// The failure seems to be in the SPACK compression using AMATERSE.
	//@Test public void testTextSPack01() throws Exception { testText("FB.A.TXT",        "SPACK"); }
	@Test public void testTextSPack02() throws Exception { testText("FB.AAA.TXT",      "SPACK"); }
	@Test public void testTextSPack04() throws Exception { testText("FB.ALPHABET.TXT", "SPACK"); }
	@Test public void testTextSPack06() throws Exception { testText("FB.BIBLE.TXT",    "SPACK"); }
	@Test public void testTextSPack07() throws Exception { testText("FB.CP.HTML",      "SPACK"); }
	@Test public void testTextSPack08() throws Exception { testText("FB.E.COLI",       "SPACK"); }
	@Test public void testTextSPack09() throws Exception { testText("FB.FIELDS.C",     "SPACK"); }
	@Test public void testTextSPack10() throws Exception { testText("FB.GRAMMAR.LSP",  "SPACK"); }
	@Test public void testTextSPack12() throws Exception { testText("FB.LCET10.TXT",   "SPACK"); }
	@Test public void testTextSPack13() throws Exception { testText("FB.PI.TXT",       "SPACK"); }
	@Test public void testTextSPack16() throws Exception { testText("FB.RANDOM.TXT",   "SPACK"); }
	@Test public void testTextSPack18() throws Exception { testText("FB.WORLD192.TXT", "SPACK"); }
	@Test public void testTextSPack19() throws Exception { testText("FB.XARGS",        "SPACK"); }
	@Test public void testTextSPack20() throws Exception { testText("VB.BIBLE.TXT",    "SPACK"); }
	@Test public void testTextSPack21() throws Exception { testText("VB.CP.HTML",      "SPACK"); }
	@Test public void testTextSPack23() throws Exception { testText("VB.FIELDS.C",     "SPACK"); }
	@Test public void testTextSPack24() throws Exception { testText("VB.GRAMMAR.LSP",  "SPACK"); }
	@Test public void testTextSPack25() throws Exception { testText("VB.LCET10.TXT",   "SPACK"); }
	@Test public void testTextSPack26() throws Exception { testText("VB.WORLD192.TXT", "SPACK"); }
	@Test public void testTextSPack27() throws Exception { testText("VB.XARGS",        "SPACK"); }
	// The following test fails, but the file also uncompresses incorrectly using AMATERSE on z/OS.
	// The failure seems to be in the SPACK compression using AMATERSE.
	//@Test public void testTextSPack28() throws Exception { testText("VB.A.TXT",        "SPACK"); }	
	@Test public void testTextSPack29() throws Exception { testText("VB.AAA.TXT",      "SPACK"); }
	@Test public void testTextSPack30() throws Exception { testText("VB.ALPHABET.TXT", "SPACK"); }
	@Test public void testTextSPack31() throws Exception { testText("VB.E.COLI",       "SPACK"); }
	@Test public void testTextSPack32() throws Exception { testText("VB.PI.TXT",       "SPACK"); }
	@Test public void testTextSPack33() throws Exception { testText("VB.RANDOM.TXT",   "SPACK"); }
	
	private void testBinary(String file, String packSpack) throws Exception 
	{
		String tersed = location + "/TERSED/" + file + "." + packSpack;
		String untersed = location + "/ZOSBINARY/" + file;
		
		byte[] expected = Files.readAllBytes(Paths.get(untersed));
		System.out.println("Binary: " + packSpack + " " + file  + " size=" + expected.length);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
        try (TerseDecompresser outputWriter 
        		= TerseDecompresser.create(new FileInputStream(tersed), out, false))
        {
        	outputWriter.decode();
        }
		assertArrayEquals(file, expected, out.toByteArray());
	}
	
	private void testText(String file, String packSpack) throws Exception 
	{
		String tersed = location + "/TERSED/" + file + "." + packSpack;
		String untersed = location + "/ZOSTEXT/" + file;
		
		// Assumes that the record separators in the ZOSTEXT file are correct for this system, 
		// i.e. the data was checked out in text mode with git crlf conversion or otherwise
		// converted.
		byte[] expected = Files.readAllBytes(Paths.get(untersed));
		System.out.println("Text  : " + packSpack + " " + file  + " size=" + expected.length);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
        try (TerseDecompresser outputWriter 
        		= TerseDecompresser.create(new FileInputStream(tersed), out, true))
        {
        	outputWriter.decode();
        }
		assertArrayEquals(file, expected, out.toByteArray());
	}
	
}
