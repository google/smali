/*
 * Copyright 2013, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.tools.smali.util;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ClassFileNameHandlerTest {
    private final Charset UTF8 = StandardCharsets.UTF_8;

    @Test
    public void test1ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<100; i++) {
            sb.append((char)i);
        }

        String result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 5);
        Assert.assertEquals(95, result.getBytes(UTF8).length);
        Assert.assertEquals(95, result.length());
    }

    @Test
    public void test2ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0x80; i<0x80+100; i++) {
            sb.append((char)i);
        }

        // remove a total of 3 2-byte characters, and then add back in the 1-byte '#'
        String result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 4);
        Assert.assertEquals(200, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(195, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());

        // remove a total of 3 2-byte characters, and then add back in the 1-byte '#'
        result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 5);
        Assert.assertEquals(200, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(195, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());
    }

    @Test
    public void test3ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0x800; i<0x800+100; i++) {
            sb.append((char)i);
        }

        // remove a total of 3 3-byte characters, and then add back in the 1-byte '#'
        String result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 6);
        Assert.assertEquals(300, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(292, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());

        // remove a total of 3 3-byte characters, and then add back in the 1-byte '#'
        result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 7);
        Assert.assertEquals(300, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(292, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());
    }

    @Test
    public void test4ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0x10000; i<0x10000+100; i++) {
            sb.appendCodePoint(i);
        }

        // we remove 3 codepoints == 6 characters == 12 bytes, and then add back in the 1-byte '#'
        String result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 8);
        Assert.assertEquals(400, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(389, result.getBytes(UTF8).length);
        Assert.assertEquals(195, result.length());

        // we remove 2 codepoints == 4 characters == 8 bytes, and then add back in the 1-byte '#'
        result = ClassFileNameHandler.shortenPathComponent(sb.toString(), 7);
        Assert.assertEquals(400, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(393, result.getBytes(UTF8).length);
        Assert.assertEquals(197, result.length());
    }

    @Test
    public void testMultipleLongNames() throws IOException {
        String filenameFragment = Strings.repeat("a", 512);

        File tempDir = Files.createTempDir().getCanonicalFile();
        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali");

        // put the differentiating character in the middle, where it will get stripped out by the filename shortening
        // logic
        File file1 = handler.getUniqueFilenameForClass("La/a/" + filenameFragment  + "1" + filenameFragment + ";");
        checkFilename(tempDir, file1, "a", "a", Strings.repeat("a", 124) + "#" + Strings.repeat("a", 118) + ".smali");

        File file2 = handler.getUniqueFilenameForClass("La/a/" + filenameFragment + "2" + filenameFragment + ";");
        checkFilename(tempDir, file2, "a", "a", Strings.repeat("a", 124) + "#" + Strings.repeat("a", 118) + ".1.smali");

        Assert.assertFalse(file1.getAbsolutePath().equals(file2.getAbsolutePath()));
    }

    @Test
    public void testBasicFunctionality() throws IOException {
        File tempDir = Files.createTempDir().getCanonicalFile();
        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali");

        File file = handler.getUniqueFilenameForClass("La/b/c/d;");
        checkFilename(tempDir, file, "a", "b", "c", "d.smali");

        file = handler.getUniqueFilenameForClass("La/b/c/e;");
        checkFilename(tempDir, file, "a", "b", "c", "e.smali");

        file = handler.getUniqueFilenameForClass("La/b/d/d;");
        checkFilename(tempDir, file, "a", "b", "d", "d.smali");

        file = handler.getUniqueFilenameForClass("La/b;");
        checkFilename(tempDir, file, "a", "b.smali");

        file = handler.getUniqueFilenameForClass("Lb;");
        checkFilename(tempDir, file, "b.smali");
    }

    @Test
    public void testCaseInsensitiveFilesystem() throws IOException {
        File tempDir = Files.createTempDir().getCanonicalFile();
        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali", false, false);

        File file = handler.getUniqueFilenameForClass("La/b/c;");
        checkFilename(tempDir, file, "a", "b", "c.smali");

        file = handler.getUniqueFilenameForClass("La/b/C;");
        checkFilename(tempDir, file, "a", "b", "C.1.smali");

        file = handler.getUniqueFilenameForClass("La/B/c;");
        checkFilename(tempDir, file, "a", "B.1", "c.smali");
    }

    @Test
    public void testCaseSensitiveFilesystem() throws IOException {

        File tempDir = Files.createTempDir().getCanonicalFile();
        if (!PathUtil.testCaseSensitivity(tempDir)) {
            // Test can only be performed on case sensitive systems
            return;
        }

        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali", true, false);

        File file = handler.getUniqueFilenameForClass("La/b/c;");
        checkFilename(tempDir, file, "a", "b", "c.smali");

        file = handler.getUniqueFilenameForClass("La/b/C;");
        checkFilename(tempDir, file, "a", "b", "C.smali");

        file = handler.getUniqueFilenameForClass("La/B/c;");
        checkFilename(tempDir, file, "a", "B", "c.smali");
    }

    @Test
    public void testWindowsReservedFilenames() throws IOException {
        File tempDir = Files.createTempDir().getCanonicalFile();
        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali", false, true);

        File file = handler.getUniqueFilenameForClass("La/con/c;");
        checkFilename(tempDir, file, "a", "con#", "c.smali");

        file = handler.getUniqueFilenameForClass("La/Con/c;");
        checkFilename(tempDir, file, "a", "Con#.1", "c.smali");

        file = handler.getUniqueFilenameForClass("La/b/PRN;");
        checkFilename(tempDir, file, "a", "b", "PRN#.smali");

        file = handler.getUniqueFilenameForClass("La/b/prN;");
        checkFilename(tempDir, file, "a", "b", "prN#.1.smali");

        file = handler.getUniqueFilenameForClass("La/b/com0;");
        checkFilename(tempDir, file, "a", "b", "com0.smali");

        for (String reservedName: new String[] {"con", "prn", "aux", "nul", "com1", "com9", "lpt1", "lpt9"}) {
            file = handler.getUniqueFilenameForClass("L" + reservedName + ";");
            checkFilename(tempDir, file, reservedName +"#.smali");
        }
    }

    @Test
    public void testIgnoringWindowsReservedFilenames() throws IOException {
        File tempDir = Files.createTempDir().getCanonicalFile();
        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali", true, false);

        File file = handler.getUniqueFilenameForClass("La/con/c;");
        checkFilename(tempDir, file, "a", "con", "c.smali");

        file = handler.getUniqueFilenameForClass("La/Con/c;");
        if (PathUtil.testCaseSensitivity(tempDir)) {
            checkFilename(tempDir, file, "a", "Con", "c.smali");
        } else {
            checkFilename(tempDir, file, "a", "Con.1", "c.smali");
        }

        file = handler.getUniqueFilenameForClass("La/b/PRN;");
        checkFilename(tempDir, file, "a", "b", "PRN.smali");

        file = handler.getUniqueFilenameForClass("La/b/prN;");
        if (PathUtil.testCaseSensitivity(tempDir)) {
            checkFilename(tempDir, file, "a", "b", "prN.smali");
        } else {
            checkFilename(tempDir, file, "a", "b", "prN.1.smali");
        }

        file = handler.getUniqueFilenameForClass("La/b/com0;");
        checkFilename(tempDir, file, "a", "b", "com0.smali");

        for (String reservedName: new String[] {"con", "prn", "aux", "nul", "com1", "com9", "lpt1", "lpt9"}) {
            file = handler.getUniqueFilenameForClass("L" + reservedName + ";");
            checkFilename(tempDir, file, reservedName +".smali");
        }
    }

    @Test
    public void testUnicodeCollisionOnMac() throws IOException {
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            // The test is only applicable when run on a mac system
            return;
        }

        File tempDir = Files.createTempDir().getCanonicalFile();
        ClassFileNameHandler handler = new ClassFileNameHandler(tempDir, ".smali", true, false);

        File file = handler.getUniqueFilenameForClass("Lε;");
        checkFilename(tempDir, file, "ε.smali");

        file = handler.getUniqueFilenameForClass("Lϵ;");
        checkFilename(tempDir, file, "ϵ.1.smali");

        file = handler.getUniqueFilenameForClass("Lε/ε;");
        checkFilename(tempDir, file, "ε", "ε.smali");

        file = handler.getUniqueFilenameForClass("Lε/ϵ;");
        checkFilename(tempDir, file, "ε", "ϵ.1.smali");

        file = handler.getUniqueFilenameForClass("Lϵ/ϵ;");
        checkFilename(tempDir, file, "ϵ.1", "ϵ.smali");

        file = handler.getUniqueFilenameForClass("Lϵ/ε;");
        checkFilename(tempDir, file, "ϵ.1", "ε.1.smali");
    }

    private void checkFilename(File base, File file, String... elements) {
        for (int i=elements.length-1; i>=0; i--) {
            Assert.assertEquals(elements[i], file.getName());
            file = file.getParentFile();
        }
        Assert.assertEquals(base.getAbsolutePath(), file.getAbsolutePath());
    }
}
