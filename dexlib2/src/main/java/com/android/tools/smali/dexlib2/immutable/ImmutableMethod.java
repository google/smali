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

import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;

import com.android.tools.smali.dexlib2.HiddenApiRestriction;
import com.android.tools.smali.dexlib2.base.reference.BaseMethodReference;
import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.MethodParameter;
import com.android.tools.smali.util.CollectionUtils;
import com.android.tools.smali.util.ImmutableConverter;
import com.android.tools.smali.util.ImmutableUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class ImmutableMethod extends BaseMethodReference implements Method {
    @Nonnull protected final String definingClass;
    @Nonnull protected final String name;
    @Nonnull protected final List<? extends ImmutableMethodParameter> parameters;
    @Nonnull protected final String returnType;
    protected final int accessFlags;
    @Nonnull protected final Set<? extends ImmutableAnnotation> annotations;
    @Nonnull protected final Set<HiddenApiRestriction> hiddenApiRestrictions;
    @Nullable protected final ImmutableMethodImplementation methodImplementation;

    public ImmutableMethod(@Nonnull String definingClass,
                           @Nonnull String name,
                           @Nullable Iterable<? extends MethodParameter> parameters,
                           @Nonnull String returnType,
                           int accessFlags,
                           @Nullable Set<? extends Annotation> annotations,
                           @Nullable Set<HiddenApiRestriction> hiddenApiRestrictions,
                           @Nullable MethodImplementation methodImplementation) {
        this.definingClass = definingClass;
        this.name = name;
        this.parameters = ImmutableMethodParameter.immutableListOf(parameters);
        this.returnType = returnType;
        this.accessFlags = accessFlags;
        this.annotations = ImmutableAnnotation.immutableSetOf(annotations);
        this.hiddenApiRestrictions =
                hiddenApiRestrictions == null ? Collections.emptySet() : 
                        unmodifiableSet(Set.copyOf(hiddenApiRestrictions));
        this.methodImplementation = ImmutableMethodImplementation.of(methodImplementation);
    }

    public ImmutableMethod(@Nonnull String definingClass,
                           @Nonnull String name,
                           @Nullable List<? extends ImmutableMethodParameter> parameters,
                           @Nonnull String returnType,
                           int accessFlags,
                           @Nullable Set<? extends ImmutableAnnotation> annotations,
                           @Nullable Set<HiddenApiRestriction> hiddenApiRestrictions,
                           @Nullable ImmutableMethodImplementation methodImplementation) {
        this.definingClass = definingClass;
        this.name = name;
        this.parameters = ImmutableUtils.nullToEmptyList(parameters);
        this.returnType = returnType;
        this.accessFlags = accessFlags;
        this.annotations = ImmutableUtils.nullToEmptySet(annotations);
        this.hiddenApiRestrictions = ImmutableUtils.nullToEmptySet(hiddenApiRestrictions);
        this.methodImplementation = methodImplementation;
    }

    public static ImmutableMethod of(Method method) {
        if (method instanceof ImmutableMethod) {
            return (ImmutableMethod)method;
        }
        return new ImmutableMethod(
                method.getDefiningClass(),
                method.getName(),
                method.getParameters(),
                method.getReturnType(),
                method.getAccessFlags(),
                method.getAnnotations(),
                method.getHiddenApiRestrictions(),
                method.getImplementation());
    }

    @Override @Nonnull public String getDefiningClass() { return definingClass; }
    @Override @Nonnull public String getName() { return name; }
    @Override @Nonnull public List<? extends CharSequence> getParameterTypes() { return parameters; }
    @Override @Nonnull public List<? extends ImmutableMethodParameter> getParameters() { return parameters; }
    @Override @Nonnull public String getReturnType() { return returnType; }
    @Override public int getAccessFlags() { return accessFlags; }
    @Override @Nonnull public Set<? extends ImmutableAnnotation> getAnnotations() { return annotations; }
    @Nonnull @Override public Set<HiddenApiRestriction> getHiddenApiRestrictions() { return hiddenApiRestrictions; }
    @Override @Nullable public ImmutableMethodImplementation getImplementation() { return methodImplementation; }

    @Nonnull
    public static SortedSet<ImmutableMethod> immutableSetOf(@Nullable Iterable<? extends Method> list) {
        return unmodifiableSortedSet(CONVERTER.toSortedSet(CollectionUtils.naturalOrdering(), list));
    }

    private static final ImmutableConverter<ImmutableMethod, Method> CONVERTER =
            new ImmutableConverter<ImmutableMethod, Method>() {
                @Override
                protected boolean isImmutable(@Nonnull Method item) {
                    return item instanceof ImmutableMethod;
                }

                @Nonnull
                @Override
                protected ImmutableMethod makeImmutable(@Nonnull Method item) {
                    return ImmutableMethod.of(item);
                }
            };
}
