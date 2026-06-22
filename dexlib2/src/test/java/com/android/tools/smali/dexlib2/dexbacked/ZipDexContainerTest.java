/*
 * Copyright 2026, Google LLC
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

package com.android.tools.smali.dexlib2.dexbacked;

import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.dexbacked.raw.HeaderItem;
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef;
import com.android.tools.smali.dexlib2.immutable.ImmutableDexFile;
import com.android.tools.smali.dexlib2.writer.io.MemoryDataStore;
import com.android.tools.smali.dexlib2.writer.pool.DexPool;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RunWith(JUnit4.class)
public class ZipDexContainerTest {
    @Test
    public void testZipDexEntryCrc() throws Exception {
        ImmutableClassDef classDef = new ImmutableClassDef("Lorg/test/blah;",
                0, "Ljava/lang/Object;", null, null, ImmutableSet.of(), null, null);

        MemoryDataStore dataStore = new MemoryDataStore();
        DexPool.writeTo(dataStore,
                new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(classDef)));
        byte[] dexBytes = dataStore.getData();

        CRC32 crc32 = new CRC32();
        crc32.update(dexBytes);
        long expectedCrc = crc32.getValue();

        File tempZip = File.createTempFile("test", ".apk");
        tempZip.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            ZipEntry entry = new ZipEntry("classes.dex");
            zos.putNextEntry(entry);
            zos.write(dexBytes);
            zos.closeEntry();
        }

        ZipDexContainer container = new ZipDexContainer(tempZip, Opcodes.getDefault());
        List<String> entries = container.getDexEntryNames();
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals("classes.dex", entries.get(0));

        ZipDexContainer.ZipDexEntry entry = container.getEntry("classes.dex");
        Assert.assertNotNull(entry);
        Assert.assertEquals("classes.dex", entry.getEntryName());
        Assert.assertEquals(expectedCrc, entry.getCrc());
    }

    @Test
    public void testZipDexEntryDeterministicOrder() throws Exception {
        ImmutableClassDef classDef = new ImmutableClassDef("Lorg/test/blah;",
                0, "Ljava/lang/Object;", null, null, ImmutableSet.of(), null, null);

        MemoryDataStore dataStore = new MemoryDataStore();
        DexPool.writeTo(dataStore,
                new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(classDef)));
        byte[] dexBytes = dataStore.getData();

        File tempZip = File.createTempFile("test_order", ".apk");
        tempZip.deleteOnExit();

        // Write entries out of order: z.dex, classes2.dex, classes4.dex, classes.dex, a.dex
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            for (String name : new String[]{
                    "z.dex", "classes2.dex", "classes4.dex", "classes.dex", "a.dex"}) {
                ZipEntry entry = new ZipEntry(name);
                zos.putNextEntry(entry);
                zos.write(dexBytes);
                zos.closeEntry();
            }
        }

        ZipDexContainer container = new ZipDexContainer(tempZip, Opcodes.getDefault());
        List<String> entries = container.getDexEntryNames();

        // Expected order: classes.dex, classes2.dex (classes3 missing), then a, classes4, z.dex
        String[] expectedOrder = new String[]{
                "classes.dex", "classes2.dex", "a.dex", "classes4.dex", "z.dex"};
        Assert.assertEquals(expectedOrder.length, entries.size());

        for (int i = 0; i < expectedOrder.length; i++) {
            Assert.assertEquals(expectedOrder[i], entries.get(i));
            ZipDexContainer.ZipDexEntry entry = container.getEntry(expectedOrder[i]);
            Assert.assertNotNull(entry);
        }
    }

    @Test
    public void testZipDexContainerContainerDex() throws Exception {
        ImmutableClassDef classDef = new ImmutableClassDef("Lorg/test/blah;",
                0, "Ljava/lang/Object;", null, null, ImmutableSet.of(), null, null);

        MemoryDataStore dataStore = new MemoryDataStore();
        DexPool.writeTo(dataStore,
                new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(classDef)));
        byte[] dexBytes = dataStore.getData();

        byte[] containerBytes = new byte[dexBytes.length * 2];
        System.arraycopy(dexBytes, 0, containerBytes, 0, dexBytes.length);
        System.arraycopy(dexBytes, 0, containerBytes, dexBytes.length, dexBytes.length);

        for (int headerOffset : new int[]{0, dexBytes.length}) {
            containerBytes[headerOffset + 4] = '0';
            containerBytes[headerOffset + 5] = '4';
            containerBytes[headerOffset + 6] = '1';
            writeInt(containerBytes,
                    headerOffset + HeaderItem.CONTAINER_SIZE_OFFSET, containerBytes.length);
            writeInt(containerBytes,
                    headerOffset + HeaderItem.CONTAINER_OFF_OFFSET, headerOffset);
        }

        File tempZip = File.createTempFile("test_container", ".apk");
        tempZip.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            ZipEntry entry = new ZipEntry("classes.dex");
            zos.putNextEntry(entry);
            zos.write(containerBytes);
            zos.closeEntry();
        }

        ZipDexContainer container = new ZipDexContainer(tempZip, Opcodes.forDexVersion(41));
        List<String> entries = container.getDexEntryNames();
        Assert.assertEquals(2, entries.size());
        Assert.assertEquals("classes.dex/0", entries.get(0));
        Assert.assertEquals("classes.dex/1", entries.get(1));

        Assert.assertNotNull(container.getEntry("classes.dex/0"));
        Assert.assertNotNull(container.getEntry("classes.dex/1"));
    }

    @Test
    public void testZipDexContainerIgnoreTrailingGarbage() throws Exception {
        ImmutableClassDef classDef = new ImmutableClassDef("Lorg/test/blah;",
                0, "Ljava/lang/Object;", null, null, ImmutableSet.of(), null, null);

        MemoryDataStore dataStore = new MemoryDataStore();
        DexPool.writeTo(dataStore,
                new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(classDef)));
        byte[] dexBytes = dataStore.getData();

        byte[] containerBytesWithGarbage = new byte[dexBytes.length * 2 + 50];
        System.arraycopy(dexBytes, 0, containerBytesWithGarbage, 0, dexBytes.length);
        System.arraycopy(dexBytes, 0,
                containerBytesWithGarbage, dexBytes.length, dexBytes.length);

        for (int headerOffset : new int[]{0, dexBytes.length}) {
            containerBytesWithGarbage[headerOffset + 4] = '0';
            containerBytesWithGarbage[headerOffset + 5] = '4';
            containerBytesWithGarbage[headerOffset + 6] = '1';
            writeInt(containerBytesWithGarbage,
                    headerOffset + HeaderItem.CONTAINER_SIZE_OFFSET, dexBytes.length * 2);
            writeInt(containerBytesWithGarbage,
                    headerOffset + HeaderItem.CONTAINER_OFF_OFFSET, headerOffset);
        }

        File tempZip = File.createTempFile("test_garbage", ".apk");
        tempZip.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            ZipEntry entry = new ZipEntry("classes.dex");
            zos.putNextEntry(entry);
            zos.write(containerBytesWithGarbage);
            zos.closeEntry();
        }

        ZipDexContainer container = new ZipDexContainer(tempZip, Opcodes.forDexVersion(41));
        List<String> entries = container.getDexEntryNames();
        Assert.assertEquals(2, entries.size());
        Assert.assertEquals("classes.dex/0", entries.get(0));
        Assert.assertEquals("classes.dex/1", entries.get(1));
    }

    @Test
    public void testZipDexContainerSingleEntryContainerDex() throws Exception {
        ImmutableClassDef classDef = new ImmutableClassDef("Lorg/test/blah;",
                0, "Ljava/lang/Object;", null, null, ImmutableSet.of(), null, null);

        MemoryDataStore dataStore = new MemoryDataStore();
        DexPool.writeTo(dataStore,
                new ImmutableDexFile(Opcodes.getDefault(), ImmutableSet.of(classDef)));
        byte[] dexBytes = dataStore.getData();

        byte[] containerBytes = new byte[dexBytes.length];
        System.arraycopy(dexBytes, 0, containerBytes, 0, dexBytes.length);

        containerBytes[4] = '0';
        containerBytes[5] = '4';
        containerBytes[6] = '1';
        writeInt(containerBytes, HeaderItem.CONTAINER_SIZE_OFFSET, containerBytes.length);
        writeInt(containerBytes, HeaderItem.CONTAINER_OFF_OFFSET, 0);

        File tempZip = File.createTempFile("test_single_container", ".apk");
        tempZip.deleteOnExit();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            ZipEntry entry = new ZipEntry("classes.dex");
            zos.putNextEntry(entry);
            zos.write(containerBytes);
            zos.closeEntry();
        }

        ZipDexContainer container = new ZipDexContainer(tempZip, Opcodes.forDexVersion(41));
        List<String> entries = container.getDexEntryNames();
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals("classes.dex", entries.get(0));
        Assert.assertNotNull(container.getEntry("classes.dex"));
    }

    private static void writeInt(byte[] buf, int offset, int value) {
        buf[offset] = (byte) value;
        buf[offset + 1] = (byte) (value >> 8);
        buf[offset + 2] = (byte) (value >> 16);
        buf[offset + 3] = (byte) (value >> 24);
    }
}
