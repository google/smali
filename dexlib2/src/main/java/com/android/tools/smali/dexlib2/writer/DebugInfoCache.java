package com.android.tools.smali.dexlib2.writer;

import java.util.Arrays;

public class DebugInfoCache {
    private final byte[] data;
    private final int threshold = 128;

    public DebugInfoCache(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugInfoCache that = (DebugInfoCache) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int hashSize = Math.min(data.length, threshold);
        int result = 0;
        for (int i = 0; i < hashSize; i++) {
          result = 31 * result + data[i];
        }
        return result;
    }
}
