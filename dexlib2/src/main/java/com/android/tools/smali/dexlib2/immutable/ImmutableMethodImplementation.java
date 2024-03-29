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

package com.android.tools.smali.dexlib2.immutable;

import com.android.tools.smali.dexlib2.iface.ExceptionHandler;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.TryBlock;
import com.android.tools.smali.dexlib2.iface.debug.DebugItem;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.immutable.debug.ImmutableDebugItem;
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction;
import com.android.tools.smali.util.ImmutableUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ImmutableMethodImplementation implements MethodImplementation {
    protected final int registerCount;
    @Nonnull protected final List<? extends ImmutableInstruction> instructions;
    @Nonnull protected final List<? extends ImmutableTryBlock> tryBlocks;
    @Nonnull protected final List<? extends ImmutableDebugItem> debugItems;

    public ImmutableMethodImplementation(int registerCount,
                                         @Nullable Iterable<? extends Instruction> instructions,
                                         @Nullable List<? extends TryBlock<? extends ExceptionHandler>> tryBlocks,
                                         @Nullable Iterable<? extends DebugItem> debugItems) {
        this.registerCount = registerCount;
        this.instructions = ImmutableInstruction.immutableListOf(instructions);
        this.tryBlocks = ImmutableTryBlock.immutableListOf(tryBlocks);
        this.debugItems = ImmutableDebugItem.immutableListOf(debugItems);
    }

    public ImmutableMethodImplementation(int registerCount,
                                         @Nullable List<? extends ImmutableInstruction> instructions,
                                         @Nullable List<? extends ImmutableTryBlock> tryBlocks,
                                         @Nullable List<? extends ImmutableDebugItem> debugItems) {
        this.registerCount = registerCount;
        this.instructions = ImmutableUtils.nullToEmptyList(instructions);
        this.tryBlocks = ImmutableUtils.nullToEmptyList(tryBlocks);
        this.debugItems = ImmutableUtils.nullToEmptyList(debugItems);
    }

    @Nullable
    public static ImmutableMethodImplementation of(@Nullable MethodImplementation methodImplementation) {
        if (methodImplementation == null) {
            return null;
        }
        if (methodImplementation instanceof ImmutableMethodImplementation) {
            return (ImmutableMethodImplementation)methodImplementation;
        }
        return new ImmutableMethodImplementation(
                methodImplementation.getRegisterCount(),
                methodImplementation.getInstructions(),
                methodImplementation.getTryBlocks(),
                methodImplementation.getDebugItems());
    }

    @Override public int getRegisterCount() { return registerCount; }
    @Nonnull @Override public List<? extends ImmutableInstruction> getInstructions() { return instructions; }
    @Nonnull @Override public List<? extends ImmutableTryBlock> getTryBlocks() { return tryBlocks; }
    @Nonnull @Override public List<? extends ImmutableDebugItem> getDebugItems() { return debugItems; }
}
