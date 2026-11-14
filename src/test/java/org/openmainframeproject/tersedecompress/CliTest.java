// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

package org.openmainframeproject.tersedecompress;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CliTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private Process run(String... args) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", Path.of(TerseDecompress.class.getProtectionDomain().getCodeSource()
                        .getLocation().toURI()).toString(), TerseDecompress.class.getName()));
        command.addAll(Arrays.asList(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(temporary.newFile()).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("CLI timed out");
        }
        return process;
    }

    @Test public void gzipMatchesCorpus() throws Exception {
        for (String format : List.of("PACK", "SPACK")) {
            for (String file : List.of("FB.RANDOM.TXT", "VB.RANDOM.TXT")) {
                for (boolean binary : List.of(false, true)) {
                    Path output = temporary.newFile().toPath();
                    List<String> args = new ArrayList<>(List.of("-z"));
                    if (binary) args.add("-b");
                    args.add("test-data/TERSED/" + file + "." + format);
                    args.add(output.toString());
                    assertEquals(0, run(args.toArray(String[]::new)).exitValue());
                    try (InputStream gzip = new GZIPInputStream(Files.newInputStream(output))) {
                        assertArrayEquals(file + "." + format,
                                Files.readAllBytes(Path.of("test-data", binary ? "ZOSBINARY" : "ZOSTEXT", file)),
                                gzip.readAllBytes());
                    }
                }
            }
        }
    }

    @Test public void malformedInputReturnsFailure() throws Exception {
        Path input = temporary.newFile().toPath();
        Files.write(input, new byte[] {2});
        Path output = temporary.newFile().toPath();
        assertNotEquals(0, run(input.toString(), output.toString()).exitValue());
    }

    @Test public void dataSetInputRequiresOutputName() throws Exception {
        for (String input : List.of("//'MY.DATA.SET'", "//MY.DATA.SET")) {
            assertEquals(1, run(input).exitValue());
            assertEquals(1, run("-b", input).exitValue());
        }
    }

    @Test public void missingInputPreservesExistingOutput() throws Exception {
        Path output = temporary.newFile().toPath();
        byte[] original = {1, 2, 3};
        Files.write(output, original);
        assertNotEquals(0, run(temporary.getRoot().toPath().resolve("missing").toString(),
                output.toString()).exitValue());
        assertArrayEquals(original, Files.readAllBytes(output));
    }
}
