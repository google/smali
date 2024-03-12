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

import static java.util.Collections.unmodifiableList;

import com.android.tools.smali.dexlib2.base.reference.BaseTypeReference;
import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.util.FieldUtil;
import com.android.tools.smali.dexlib2.util.MethodUtil;
import com.android.tools.smali.util.ChainedIterator;
import com.android.tools.smali.util.ImmutableConverter;
import com.android.tools.smali.util.ImmutableUtils;
import com.android.tools.smali.util.IteratorUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;


public class ImmutableClassDef extends BaseTypeReference implements ClassDef {
    @Nonnull protected final String type;
    protected final int accessFlags;
    @Nullable protected final String superclass;
    @Nonnull protected final List<String> interfaces;
    @Nullable protected final String sourceFile;
    @Nonnull protected final Set<? extends ImmutableAnnotation> annotations;
    @Nonnull protected final SortedSet<? extends ImmutableField> staticFields;
    @Nonnull protected final SortedSet<? extends ImmutableField> instanceFields;
    @Nonnull protected final SortedSet<? extends ImmutableMethod> directMethods;
    @Nonnull protected final SortedSet<? extends ImmutableMethod> virtualMethods;

    public ImmutableClassDef(@Nonnull String type,
                             int accessFlags,
                             @Nullable String superclass,
                             @Nullable Collection<String> interfaces,
                             @Nullable String sourceFile,
                             @Nullable Collection<? extends Annotation> annotations,
                             @Nullable Iterable<? extends Field> fields,
                             @Nullable Iterable<? extends Method> methods) {
        if (fields == null) {
            fields = Collections.emptyList();
        }
        if (methods == null) {
            methods = Collections.emptyList();
        }

        this.type = type;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = interfaces == null ? Collections.emptyList() : unmodifiableList(new ArrayList<>(interfaces));
        this.sourceFile = sourceFile;
        this.annotations = ImmutableAnnotation.immutableSetOf(annotations);
        this.staticFields = ImmutableField.immutableSetOf(IteratorUtils.filter(fields, FieldUtil.FIELD_IS_STATIC));
        this.instanceFields = ImmutableField.immutableSetOf(IteratorUtils.filter(fields, FieldUtil.FIELD_IS_INSTANCE));
        this.directMethods = ImmutableMethod.immutableSetOf(IteratorUtils.filter(methods, MethodUtil.METHOD_IS_DIRECT));
        this.virtualMethods = ImmutableMethod.immutableSetOf(IteratorUtils.filter(methods, MethodUtil.METHOD_IS_VIRTUAL));
    }

    public ImmutableClassDef(@Nonnull String type,
                             int accessFlags,
                             @Nullable String superclass,
                             @Nullable Collection<String> interfaces,
                             @Nullable String sourceFile,
                             @Nullable Collection<? extends Annotation> annotations,
                             @Nullable Iterable<? extends Field> staticFields,
                             @Nullable Iterable<? extends Field> instanceFields,
                             @Nullable Iterable<? extends Method> directMethods,
                             @Nullable Iterable<? extends Method> virtualMethods) {
        this.type = type;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = interfaces == null ? Collections.emptyList() : unmodifiableList(new ArrayList<>(interfaces));
        this.sourceFile = sourceFile;
        this.annotations = ImmutableAnnotation.immutableSetOf(annotations);
        this.staticFields = ImmutableField.immutableSetOf(staticFields);
        this.instanceFields = ImmutableField.immutableSetOf(instanceFields);
        this.directMethods = ImmutableMethod.immutableSetOf(directMethods);
        this.virtualMethods = ImmutableMethod.immutableSetOf(virtualMethods);
    }

    public ImmutableClassDef(@Nonnull String type,
                             int accessFlags,
                             @Nullable String superclass,
                             @Nullable List<String> interfaces,
                             @Nullable String sourceFile,
                             @Nullable Set<? extends ImmutableAnnotation> annotations,
                             @Nullable SortedSet<? extends ImmutableField> staticFields,
                             @Nullable SortedSet<? extends ImmutableField> instanceFields,
                             @Nullable SortedSet<? extends ImmutableMethod> directMethods,
                             @Nullable SortedSet<? extends ImmutableMethod> virtualMethods) {
        this.type = type;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = ImmutableUtils.nullToEmptyList(interfaces);
        this.sourceFile = sourceFile;
        this.annotations = ImmutableUtils.nullToEmptySet(annotations);
        this.staticFields = ImmutableUtils.nullToEmptySortedSet(staticFields);
        this.instanceFields = ImmutableUtils.nullToEmptySortedSet(instanceFields);
        this.directMethods = ImmutableUtils.nullToEmptySortedSet(directMethods);
        this.virtualMethods = ImmutableUtils.nullToEmptySortedSet(virtualMethods);
    }

    public static ImmutableClassDef of(ClassDef classDef) {
        if (classDef instanceof ImmutableClassDef) {
            return (ImmutableClassDef)classDef;
        }
        return new ImmutableClassDef(
                classDef.getType(),
                classDef.getAccessFlags(),
                classDef.getSuperclass(),
                classDef.getInterfaces(),
                classDef.getSourceFile(),
                classDef.getAnnotations(),
                classDef.getStaticFields(),
                classDef.getInstanceFields(),
                classDef.getDirectMethods(),
                classDef.getVirtualMethods());
    }

    @Nonnull @Override public String getType() { return type; }
    @Override public int getAccessFlags() { return accessFlags; }
    @Nullable @Override public String getSuperclass() { return superclass; }
    @Nonnull @Override public List<String> getInterfaces() { return interfaces; }
    @Nullable @Override public String getSourceFile() { return sourceFile; }
    @Nonnull @Override public Set<? extends ImmutableAnnotation> getAnnotations() { return annotations; }
    @Nonnull @Override public Set<? extends ImmutableField> getStaticFields() { return staticFields; }
    @Nonnull @Override public Set<? extends ImmutableField> getInstanceFields() { return instanceFields; }
    @Nonnull @Override public Set<? extends ImmutableMethod> getDirectMethods() { return directMethods; }
    @Nonnull @Override public Set<? extends ImmutableMethod> getVirtualMethods() { return virtualMethods; }

    @Nonnull
    @Override
    public Iterable<? extends ImmutableField> getFields() {
        return new ChainedIterator(staticFields, instanceFields);
    }

    @Nonnull
    @Override
    public Iterable<? extends ImmutableMethod> getMethods() {
        return new ChainedIterator(directMethods, virtualMethods);
    }

    @Nonnull
    public static Set<ImmutableClassDef> immutableSetOf(@Nullable Iterable<? extends ClassDef> iterable) {
        return CONVERTER.toSet(iterable);
    }

    private static final ImmutableConverter<ImmutableClassDef, ClassDef> CONVERTER =
            new ImmutableConverter<ImmutableClassDef, ClassDef>() {
                @Override
                protected boolean isImmutable(@Nonnull ClassDef item) {
                    return item instanceof ImmutableClassDef;
                }

                @Nonnull
                @Override
                protected ImmutableClassDef makeImmutable(@Nonnull ClassDef item) {
                    return ImmutableClassDef.of(item);
                }
            };
}
