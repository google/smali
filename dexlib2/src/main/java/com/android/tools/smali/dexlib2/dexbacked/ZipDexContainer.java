/*
 * Copyright 2016, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google LLC nor the names of its
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
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.MultiDexContainer;
import com.android.tools.smali.dexlib2.util.DexUtil;
import com.android.tools.smali.dexlib2.util.DexUtil.InvalidFile;
import com.android.tools.smali.dexlib2.util.DexUtil.UnsupportedFile;
import com.android.tools.smali.util.InputStreamUtil;

import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile.NotADexFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a zip file that contains dex files (i.e. an apk or jar file)
 */
public class ZipDexContainer implements MultiDexContainer<DexBackedDexFile> {

    /**
     * Represents a dex entry in a zip container, including the uncompressed CRC-32 checksum
     * of the zip entry.
     */
    public interface ZipDexEntry extends DexEntry<DexBackedDexFile> {
        /**
         * Gets the uncompressed CRC-32 checksum of the zip entry containing this dex file.
         *
         * @return The uncompressed CRC-32 checksum of the zip entry
         */
        long getCrc();
    }

    private final File zipFilePath;
    @Nullable private final Opcodes opcodes;
    private Map<String, ZipDexEntry> entries;

    /**
     * Constructs a new ZipDexContainer for the given zip file
     *
     * @param zipFilePath The path to the zip file
     */
    public ZipDexContainer(@Nonnull File zipFilePath, @Nullable Opcodes opcodes) {
        this.zipFilePath = zipFilePath;
        this.opcodes = opcodes;
    }

    /**
     * Gets a list of the names of dex files in this zip file.
     * <p>
     * The returned list follows a strict deterministic order: it starts with all dex files in
     * classes.dex, then classes2.dex, etc. sequentially until classesN.dex is missing, followed by
     * any remaining dex entries in lexicographically sorted order.
     *
     * @return A list of the names of dex files in this zip file
     */
    @Nonnull @Override public List<String> getDexEntryNames() throws IOException {
        return new ArrayList<>(getEntries().keySet());
    }

    private Map<String, ZipDexEntry> getEntries() throws IOException {
        if (entries != null) {
          return entries;
        }
        // LinkedHashMap is used to preserve the strict deterministic insertion ordering of entries.
        entries = new LinkedHashMap<String, ZipDexEntry>();
        try (ZipFile zipFile = getZipFile()) {
            Map<String, ZipEntry> validZipEntries = new HashMap<>();
            Enumeration<? extends ZipEntry> entriesEnumeration = zipFile.entries();

            while (entriesEnumeration.hasMoreElements()) {
                ZipEntry entry = entriesEnumeration.nextElement();
                if (isDex(zipFile, entry)) {
                    validZipEntries.put(entry.getName(), entry);
                }
            }

            for (int n = 1; ; n++) {
                String targetName = (n == 1) ? "classes.dex" : ("classes" + n + ".dex");
                ZipEntry entry = validZipEntries.remove(targetName);
                if (entry == null) {
                    break;
                }
                loadDexFilesFromZipEntry(zipFile, entry);
            }

            List<String> remainingNames = new ArrayList<>(validZipEntries.keySet());
            Collections.sort(remainingNames);
            for (String remainingName : remainingNames) {
                ZipEntry entry = validZipEntries.get(remainingName);
                loadDexFilesFromZipEntry(zipFile, entry);
            }

            return entries;
        }
    }

    private void loadDexFilesFromZipEntry(@Nonnull ZipFile zipFile, @Nonnull ZipEntry entry)
            throws IOException {
        long entryCrc = entry.getCrc();
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            byte[] buf = InputStreamUtil.toByteArray(inputStream);
            for (int offset = 0, i = 0; offset < buf.length; i++) {
              DexBackedDexFile dex = new DexBackedDexFile(opcodes, buf, 0, true, offset);
              boolean hasSingleEntry = dex.isDexContainerFirstEntry() &&
                      dex.isDexContainerLastEntry();
              String entryName = entry.getName() + (hasSingleEntry ? "" : ("/" + i));
              ZipDexEntry dexEntry = new ZipDexEntry() {
                  @Nonnull
                  @Override
                  public String getEntryName() {
                      return entryName;
                  }

                  @Nonnull
                  @Override
                  public DexBackedDexFile getDexFile() {
                      return dex;
                  }

                  @Nonnull
                  @Override
                  public MultiDexContainer<DexBackedDexFile> getContainer() {
                      return ZipDexContainer.this;
                  }

                  @Override
                  public long getCrc() {
                      return entryCrc;
                  }
              };
              entries.put(entryName, dexEntry);
              offset += dex.getFileSize();
              if (dex.isDexContainerLastEntry()) {
                  break;
              }
            };
        }
    }

    /**
     * Loads a dex file from a specific named entry.
     *
     * @param entryName The name of the entry
     * @return A DexEntry, or null if there is no entry with the given name
     * @throws NotADexFile If the entry isn't a dex file
     */
    @Nullable @Override public ZipDexEntry getEntry(@Nonnull String entryName) throws IOException {
         return getEntries().get(entryName);
    }

    public boolean isZipFile() {
        try (ZipFile zipFile = getZipFile()) {
            return true;
        } catch (IOException ex) {
            return false;
        } catch (NotAZipFileException ex) {
            return false;
        }
        // just eat it
    }

    protected boolean isDex(@Nonnull ZipFile zipFile, @Nonnull ZipEntry zipEntry)
            throws IOException {
        try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry))) {
            DexUtil.verifyDexHeader(inputStream);
        } catch (NotADexFile ex) {
            return false;
        } catch (InvalidFile ex) {
            return false;
        } catch (UnsupportedFile ex) {
            return false;
        }
        return true;
    }

    protected ZipFile getZipFile() throws IOException {
        try {
            return new ZipFile(zipFilePath);
        } catch (IOException ex) {
            throw new NotAZipFileException();
        }
    }

    public static class NotAZipFileException extends RuntimeException {
    }
}
