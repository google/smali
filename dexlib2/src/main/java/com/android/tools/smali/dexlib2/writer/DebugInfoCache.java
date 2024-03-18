package com.android.tools.smali.dexlib2.writer;

import java.util.Arrays;

public class DebugInfoCache {
    private final byte[] data;
    private final int hashCode;

    public DebugInfoCache(byte[] data) {
        this.data = data;
        this.hashCode = Arrays.hashCode(data);
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
        return hashCode;
    }
}