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

package com.android.tools.smali.dexlib2.builder;

import com.google.common.collect.Lists;
import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction20t;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.OffsetInstruction;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FixGotoTest {
    @Test
    public void testFixGotoToGoto16() {
        MethodImplementationBuilder builder = new MethodImplementationBuilder(1);

        Label gotoTarget = builder.getLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction10t(Opcode.GOTO, gotoTarget));

        for (int i=0; i<500; i++) {
            builder.addInstruction(new BuilderInstruction10x(Opcode.NOP));
        }

        builder.addLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        MethodImplementation impl = builder.getMethodImplementation();

        List<? extends Instruction> instructions = Lists.newArrayList(impl.getInstructions());
        Assert.assertEquals(502, instructions.size());

        Assert.assertEquals(Opcode.GOTO_16, instructions.get(0).getOpcode());
        Assert.assertEquals(502, ((OffsetInstruction)instructions.get(0)).getCodeOffset());
    }

    @Test
    public void testFixGotoToGoto32() {
        MethodImplementationBuilder builder = new MethodImplementationBuilder(1);

        Label gotoTarget = builder.getLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction10t(Opcode.GOTO, gotoTarget));

        for (int i=0; i<70000; i++) {
            builder.addInstruction(new BuilderInstruction10x(Opcode.NOP));
        }

        builder.addLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        MethodImplementation impl = builder.getMethodImplementation();

        List<? extends Instruction> instructions = Lists.newArrayList(impl.getInstructions());
        Assert.assertEquals(70002, instructions.size());

        Assert.assertEquals(Opcode.GOTO_32, instructions.get(0).getOpcode());
        Assert.assertEquals(70003, ((OffsetInstruction)instructions.get(0)).getCodeOffset());
    }

    @Test
    public void testFixGoto16ToGoto32() {
        MethodImplementationBuilder builder = new MethodImplementationBuilder(1);

        Label gotoTarget = builder.getLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction20t(Opcode.GOTO_16, gotoTarget));

        for (int i=0; i<70000; i++) {
            builder.addInstruction(new BuilderInstruction10x(Opcode.NOP));
        }

        builder.addLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        MethodImplementation impl = builder.getMethodImplementation();

        List<? extends Instruction> instructions = Lists.newArrayList(impl.getInstructions());
        Assert.assertEquals(70002, instructions.size());

        Assert.assertEquals(Opcode.GOTO_32, instructions.get(0).getOpcode());
        Assert.assertEquals(70003, ((OffsetInstruction)instructions.get(0)).getCodeOffset());
    }

    @Test
    public void testFixGotoCascading() {
        MethodImplementationBuilder builder = new MethodImplementationBuilder(1);

        Label goto16Target = builder.getLabel("goto16Target");
        builder.addInstruction(new BuilderInstruction20t(Opcode.GOTO_16, goto16Target));

        for (int i=0; i<1000; i++) {
            builder.addInstruction(new BuilderInstruction10x(Opcode.NOP));
        }

        Label gotoTarget = builder.getLabel("gotoTarget");
        builder.addInstruction(new BuilderInstruction10t(Opcode.GOTO, gotoTarget));

        for (int i=0; i<499; i++) {
            builder.addInstruction(new BuilderInstruction10x(Opcode.NOP));
        }

        builder.addLabel("gotoTarget");

        for (int i=0; i<31265; i++) {
            builder.addInstruction(new BuilderInstruction10x(Opcode.NOP));
        }

        builder.addLabel("goto16Target");
        builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        MethodImplementation impl = builder.getMethodImplementation();

        List<? extends Instruction> instructions = Lists.newArrayList(impl.getInstructions());
        Assert.assertEquals(32767, instructions.size());

        Assert.assertEquals(Opcode.GOTO_32, instructions.get(0).getOpcode());
        Assert.assertEquals(32769, ((OffsetInstruction)instructions.get(0)).getCodeOffset());

    }
}
