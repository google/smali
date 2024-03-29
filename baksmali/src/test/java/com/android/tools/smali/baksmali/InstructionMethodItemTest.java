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

package com.android.tools.smali.baksmali;

import com.android.tools.smali.baksmali.BaksmaliOptions;
import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.ExceptionHandler;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.MethodParameter;
import com.android.tools.smali.dexlib2.iface.TryBlock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.android.tools.smali.baksmali.Adaptors.ClassDefinition;
import com.android.tools.smali.baksmali.Adaptors.Format.InstructionMethodItem;
import com.android.tools.smali.baksmali.Adaptors.MethodDefinition;
import com.android.tools.smali.baksmali.Adaptors.RegisterFormatter;
import com.android.tools.smali.baksmali.formatter.BaksmaliWriter;
import com.android.tools.smali.dexlib2.Format;
import com.android.tools.smali.dexlib2.HiddenApiRestriction;
import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.ReferenceType;
import com.android.tools.smali.dexlib2.base.reference.BaseMethodReference;
import com.android.tools.smali.dexlib2.base.reference.BaseStringReference;
import com.android.tools.smali.dexlib2.base.reference.BaseTypeReference;
import com.android.tools.smali.dexlib2.iface.debug.DebugItem;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c;
import com.android.tools.smali.dexlib2.iface.reference.Reference;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

public class InstructionMethodItemTest {

    @Test
    public void testInvalidReference() throws IOException {

        Instruction21c instruction = new Instruction21c() {
            @Override
            public int getRegisterA() {
                return 0;
            }

            @Nonnull
            @Override
            public Reference getReference() {
                return new BaseStringReference() {
                    @Override
                    public void validateReference() throws InvalidReferenceException {
                        throw new InvalidReferenceException("blahblahblah");
                    }

                    @Nonnull
                    @Override
                    public String getString() {
                        throw new RuntimeException("invalid reference");
                    }
                };
            }

            @Override
            public int getReferenceType() {
                return ReferenceType.STRING;
            }

            @Override
            public Opcode getOpcode() {
                return Opcode.CONST_STRING;
            }

            @Override
            public int getCodeUnits() {
                return Format.Format21c.size / 2;
            }
        };


        MethodImplementation methodImplementation = new MethodImplementation() {
            @Override
            public int getRegisterCount() {
                return 1;
            }

            @Nonnull
            @Override
            public Iterable<? extends Instruction> getInstructions() {
                return ImmutableList.of(instruction);
            }

            @Nonnull
            @Override
            public List<? extends TryBlock<? extends ExceptionHandler>> getTryBlocks() {
                return ImmutableList.of();
            }

            @Nonnull
            @Override
            public Iterable<? extends DebugItem> getDebugItems() {
                return ImmutableList.of();
            }
        };

        Method method = new TestMethod(methodImplementation);

        ClassDefinition classDefinition = new ClassDefinition(
                new BaksmaliOptions(), new TestClassDef());

        MethodDefinition methodDefinition = new MethodDefinition(classDefinition, method, methodImplementation);
        methodDefinition.registerFormatter = new RegisterFormatter(new BaksmaliOptions(), 1, 0);

        InstructionMethodItem methodItem = new InstructionMethodItem<Instruction21c>(methodDefinition, 0, instruction);

        StringWriter stringWriter = new StringWriter();
        BaksmaliWriter writer = new BaksmaliWriter(stringWriter);
        methodItem.writeTo(writer);

        Assert.assertEquals("#Invalid reference" + System.lineSeparator()
                + "#const-string v0, blahblahblah" + System.lineSeparator() + "nop", stringWriter.toString());
    }

    private static class TestMethod extends BaseMethodReference implements Method {
        private final MethodImplementation methodImplementation;

        public TestMethod(MethodImplementation methodImplementation) {
            this.methodImplementation = methodImplementation;
        }

        @Nonnull
        @Override
        public List<? extends MethodParameter> getParameters() {
            return ImmutableList.of();
        }

        @Override
        public int getAccessFlags() {
            return 0;
        }

        @Nonnull
        @Override
        public Set<? extends Annotation> getAnnotations() {
            return ImmutableSet.of();
        }

        @Nullable
        @Override
        public MethodImplementation getImplementation() {
            return methodImplementation;
        }

        @Nonnull
        @Override
        public String getDefiningClass() {
            return "Ltest;";
        }

        @Nonnull
        @Override
        public String getName() {
            return "test";
        }

        @Nonnull
        @Override
        public List<? extends CharSequence> getParameterTypes() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public String getReturnType() {
            return "V";
        }

        @Nonnull @Override public Set<HiddenApiRestriction> getHiddenApiRestrictions() {
            return ImmutableSet.of();
        }
    }

    private static class TestClassDef extends BaseTypeReference implements ClassDef {
        @Override
        public int getAccessFlags() {
            return 0;
        }

        @Nullable
        @Override
        public String getSuperclass() {
            return "Ljava/lang/Object;";
        }

        @Nonnull
        @Override
        public List<String> getInterfaces() {
            return ImmutableList.of();
        }

        @Nullable
        @Override
        public String getSourceFile() {
            return null;
        }

        @Nonnull
        @Override
        public Set<? extends Annotation> getAnnotations() {
            return ImmutableSet.of();
        }

        @Nonnull
        @Override
        public Iterable<? extends Field> getStaticFields() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public Iterable<? extends Field> getInstanceFields() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public Iterable<? extends Field> getFields() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public Iterable<? extends Method> getDirectMethods() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public Iterable<? extends Method> getVirtualMethods() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public Iterable<? extends Method> getMethods() {
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public String getType() {
            return "Ltest;";
        }
    }
}
