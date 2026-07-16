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

import com.android.tools.smali.dexlib2.dexbacked.raw.HeaderItem;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CDexTest {

    @Test
    public void testSyntheticCdexHeader() throws Exception {
        // CDEX header layout (112 bytes min header):
        // 0-7: magic "cdex001\0"
        // 32-35: file_size = 112 (0x70)
        // 36-39: header_size = 112 (0x70)
        // 40-43: endian_tag = 0x12345678
        // 52-55: map_off = 120 (0x78)  <- Notice map_off is relative to data_off (112)
        // 104-107: data_size = 128
        // 108-111: data_off = 112
        //
        // In CDEX, map item is at absolute offset data_off + map_off = 112 + 120 = 232.
        // At absolute offset map_off (120), write invalid large uint (0xFFFFFFFF) to ensure
        // mapSize is read from dataBuffer (offset 232) and NOT dexBuffer (offset 120).
        byte[] buf = new byte[256];
        byte[] magic = new byte[]{'c', 'd', 'e', 'x', '0', '0', '1', 0};
        System.arraycopy(magic, 0, buf, 0, 8);
        int fileSize = 112;
        int dataStart = 112;
        int dataSize = 128;
        int mapOffset = 120;

        writeInt(buf, HeaderItem.FILE_SIZE_OFFSET, fileSize);
        writeInt(buf, HeaderItem.HEADER_SIZE_OFFSET, fileSize);
        writeInt(buf, HeaderItem.ENDIAN_TAG_OFFSET, HeaderItem.LITTLE_ENDIAN_TAG);
        writeInt(buf, HeaderItem.MAP_OFFSET, mapOffset);
        writeInt(buf, HeaderItem.DATA_SIZE_OFFSET, dataSize);
        writeInt(buf, HeaderItem.DATA_START_OFFSET, dataStart);

        writeInt(buf, mapOffset, 0xFFFFFFFF); // Trap if reading from dexBuffer at mapOffset (120)
        writeInt(buf, dataStart + mapOffset, 0); // Correct map_size = 0 at dataStart + mapOffset (232)

        CDexBackedDexFile cdex = new CDexBackedDexFile(null, buf, 0, false);
        Assert.assertNotNull(cdex);
    }

    private static void writeInt(byte[] buf, int offset, int value) {
        buf[offset] = (byte) value;
        buf[offset + 1] = (byte) (value >> 8);
        buf[offset + 2] = (byte) (value >> 16);
        buf[offset + 3] = (byte) (value >> 24);
    }
}
