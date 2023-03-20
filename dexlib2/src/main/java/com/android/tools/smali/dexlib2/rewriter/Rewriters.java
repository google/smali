/*
 * Copyright 2014, Google LLC
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

package com.android.tools.smali.dexlib2.rewriter;

import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.AnnotationElement;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.ExceptionHandler;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.MethodParameter;
import com.android.tools.smali.dexlib2.iface.TryBlock;
import com.android.tools.smali.dexlib2.iface.debug.DebugItem;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.reference.CallSiteReference;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import com.android.tools.smali.dexlib2.iface.value.EncodedValue;

import javax.annotation.Nonnull;

public interface Rewriters {
    @Nonnull Rewriter<DexFile> getDexFileRewriter();
    @Nonnull Rewriter<ClassDef> getClassDefRewriter();
    @Nonnull Rewriter<Field> getFieldRewriter();

    @Nonnull Rewriter<Method> getMethodRewriter();
    @Nonnull Rewriter<MethodParameter> getMethodParameterRewriter();
    @Nonnull Rewriter<MethodImplementation> getMethodImplementationRewriter();
    @Nonnull Rewriter<Instruction> getInstructionRewriter();
    @Nonnull Rewriter<TryBlock<? extends ExceptionHandler>> getTryBlockRewriter();
    @Nonnull Rewriter<ExceptionHandler> getExceptionHandlerRewriter();
    @Nonnull Rewriter<DebugItem> getDebugItemRewriter();

    @Nonnull Rewriter<String> getTypeRewriter();
    @Nonnull Rewriter<FieldReference> getFieldReferenceRewriter();
    @Nonnull Rewriter<MethodReference> getMethodReferenceRewriter();
    @Nonnull Rewriter<CallSiteReference> getCallSiteReferenceRewriter();

    @Nonnull Rewriter<Annotation> getAnnotationRewriter();
    @Nonnull Rewriter<AnnotationElement> getAnnotationElementRewriter();

    @Nonnull Rewriter<EncodedValue> getEncodedValueRewriter();
}
