/*
 * Copyright 2012, Google LLC
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

package com.android.tools.smali.dexlib2.dexbacked.reference;

import com.android.tools.smali.dexlib2.base.reference.BaseStringReference;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBuffer;
import com.android.tools.smali.dexlib2.dexbacked.DexReader;
import com.android.tools.smali.dexlib2.dexbacked.raw.StringIdItem;

import javax.annotation.Nonnull;

public class DexBackedStringReference extends BaseStringReference {
    @Nonnull public final DexBackedDexFile dexFile;
    public final int stringIndex;

    public DexBackedStringReference(@Nonnull DexBackedDexFile dexBuf,
                                    int stringIndex) {
        this.dexFile = dexBuf;
        this.stringIndex = stringIndex;
    }

    @Nonnull
    public String getString() {
        return dexFile.getStringSection().get(stringIndex);
    }

    /**
     * Calculate and return the private size of a string reference.
     *
     * Calculated as: string_data_off + string_data_item size
     *
     * @return size in bytes
     */
    public int getSize() {
        int size = StringIdItem.ITEM_SIZE; //uint for string_data_off
        //add the string data length:
        int stringOffset = dexFile.getStringSection().getOffset(stringIndex);
        int stringDataOffset = dexFile.getBuffer().readSmallUint(stringOffset);
        DexReader<? extends DexBuffer> reader = dexFile.getDataBuffer().readerAt(stringDataOffset);
        size += reader.peekSmallUleb128Size();
        int utf16Length = reader.readSmallUleb128();
        //and string data itself:
        size += reader.peekStringLength(utf16Length);
        return size;
    }

    @Override
    public void validateReference() throws InvalidReferenceException {
        if (stringIndex < 0 || stringIndex >= dexFile.getStringSection().size()) {
            throw new InvalidReferenceException("string@" + stringIndex);
        }
    }
}
