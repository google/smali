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

package com.android.tools.smali.dexlib2.dexbacked.instruction;

import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.iface.instruction.formats.ArrayPayload;
import com.android.tools.smali.util.ExceptionWithContext;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.util.FixedSizeList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Collections;

public class DexBackedArrayPayload extends DexBackedInstruction implements ArrayPayload {
    public static final Opcode OPCODE = Opcode.ARRAY_PAYLOAD;

    public final int elementWidth;
    public final int elementCount;

    private static final int ELEMENT_WIDTH_OFFSET = 2;
    private static final int ELEMENT_COUNT_OFFSET = 4;
    private static final int ELEMENTS_OFFSET = 8;

    public DexBackedArrayPayload(@Nonnull DexBackedDexFile dexFile,
                                 int instructionStart) {
        super(dexFile, OPCODE, instructionStart);

        int localElementWidth = dexFile.getDataBuffer().readUshort(instructionStart + ELEMENT_WIDTH_OFFSET);

        if (localElementWidth == 0) {
            elementWidth = 1;
            elementCount = 0;
        } else {
            elementWidth = localElementWidth;

            elementCount = dexFile.getDataBuffer().readSmallUint(instructionStart + ELEMENT_COUNT_OFFSET);
            if (((long) elementWidth) * elementCount > Integer.MAX_VALUE) {
                throw new ExceptionWithContext("Invalid array-payload instruction: element width*count overflows");
            }
        }
    }

    @Override public int getElementWidth() { return elementWidth; }

    @Nonnull
    @Override
    public List<Number> getArrayElements() {
        final int elementsStart = instructionStart + ELEMENTS_OFFSET;

        abstract class ReturnedList extends FixedSizeList<Number> {
            @Override public int size() { return elementCount; }
        }

        if (elementCount == 0) {
            return Collections.unmodifiableList(List.of());
        }

        switch (elementWidth) {
            case 1:
                return new ReturnedList() {
                    @Nonnull
                    @Override
                    public Number readItem(int index) {
                        return dexFile.getDataBuffer().readByte(elementsStart + index);
                    }
                };
            case 2:
                return new ReturnedList() {
                    @Nonnull
                    @Override
                    public Number readItem(int index) {
                        return dexFile.getDataBuffer().readShort(elementsStart + index*2);
                    }
                };
            case 4:
                return new ReturnedList() {
                    @Nonnull
                    @Override
                    public Number readItem(int index) {
                        return dexFile.getDataBuffer().readInt(elementsStart + index*4);
                    }
                };
            case 8:
                return new ReturnedList() {
                    @Nonnull
                    @Override
                    public Number readItem(int index) {
                        return dexFile.getDataBuffer().readLong(elementsStart + index*8);
                    }
                };
            default:
                throw new ExceptionWithContext("Invalid element width: %d", elementWidth);
        }
    }

    @Override
    public int getCodeUnits() {
        return 4 + (elementWidth*elementCount + 1) / 2;
    }
}
