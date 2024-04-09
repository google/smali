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

import com.android.tools.smali.dexlib2.base.reference.BaseMethodReference;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodReferenceRewriter implements Rewriter<MethodReference> {
    @Nonnull protected final Rewriters rewriters;

    public MethodReferenceRewriter(@Nonnull Rewriters rewriters) {
        this.rewriters = rewriters;
    }

    @Nonnull @Override public MethodReference rewrite(@Nonnull MethodReference methodReference) {
        return new RewrittenMethodReference(methodReference);
    }

    protected class RewrittenMethodReference extends BaseMethodReference {
        @Nonnull protected MethodReference methodReference;

        public RewrittenMethodReference(@Nonnull MethodReference methodReference) {
            this.methodReference = methodReference;
        }

        @Override @Nonnull public String getDefiningClass() {
            return rewriters.getTypeRewriter().rewrite(methodReference.getDefiningClass());
        }

        @Override @Nonnull public String getName() {
            return methodReference.getName();
        }

        @Override @Nonnull public List<? extends CharSequence> getParameterTypes() {
            return RewriterUtils.rewriteList(rewriters.getTypeRewriter(),
                    methodReference.getParameterTypes().stream().map(
                    new Function<CharSequence, String>() {
                        @Nonnull @Override public String apply(CharSequence input) {
                            return input.toString();
                        }
                    }).collect(Collectors.toList()));
        }

        @Override @Nonnull public String getReturnType() {
            return rewriters.getTypeRewriter().rewrite(methodReference.getReturnType());
        }
    }
}
