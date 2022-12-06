/*
 * Copyright 2019, Google LLC
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

package org.jf.dexlib2.immutable.instruction;

import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction4rcc;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.reference.ImmutableReference;
import org.jf.dexlib2.immutable.reference.ImmutableReferenceFactory;
import org.jf.dexlib2.util.Preconditions;

import javax.annotation.Nonnull;

public class ImmutableInstruction4rcc extends ImmutableInstruction implements Instruction4rcc {
    private static final Format FORMAT = Format.Format4rcc;

    protected final int startRegister;
    protected final int registerCount;

    @Nonnull protected final ImmutableReference reference;
    @Nonnull protected final ImmutableReference reference2;

    public ImmutableInstruction4rcc(
            @Nonnull Opcode opcode,
            int startRegister,
            int registerCount,
            @Nonnull Reference reference,
            @Nonnull Reference reference2) {
        super(opcode);
        this.startRegister = Preconditions.checkShortRegister(startRegister);
        this.registerCount = Preconditions.checkRegisterRangeCount(registerCount);
        this.reference = ImmutableReferenceFactory.of(reference);
        this.reference2 = ImmutableReferenceFactory.of(reference2);
    }

    public static ImmutableInstruction4rcc of(Instruction4rcc instruction) {
        if (instruction instanceof ImmutableInstruction4rcc) {
            return (ImmutableInstruction4rcc) instruction;
        }
        return new ImmutableInstruction4rcc(
                instruction.getOpcode(),
                instruction.getStartRegister(),
                instruction.getRegisterCount(),
                instruction.getReference(),
                instruction.getReference2());
    }

    @Override public int getStartRegister() { return startRegister; }
    @Override public int getRegisterCount() { return registerCount; }

    @Override public Reference getReference() { return reference; }
    @Override public int getReferenceType() { return opcode.referenceType; }

    @Override public Reference getReference2() { return reference2; }
    @Override public int getReferenceType2() { return opcode.referenceType2; }

    @Override public Format getFormat() { return FORMAT; }
}
