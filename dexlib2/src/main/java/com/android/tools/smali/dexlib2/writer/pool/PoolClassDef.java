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

package com.android.tools.smali.dexlib2.writer.pool;

import com.android.tools.smali.dexlib2.base.reference.BaseTypeReference;
import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.util.ArraySortedSet;
import com.android.tools.smali.util.CollectionUtils;
import com.android.tools.smali.util.IteratorUtils;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PoolClassDef extends BaseTypeReference implements ClassDef {
    @Nonnull final ClassDef classDef;
    @Nonnull final TypeListPool.Key<List<String>> interfaces;
    @Nonnull final SortedSet<Field> staticFields;
    @Nonnull final SortedSet<Field> instanceFields;
    @Nonnull final SortedSet<PoolMethod> directMethods;
    @Nonnull final SortedSet<PoolMethod> virtualMethods;

    int classDefIndex = DexPool.NO_INDEX;
    int annotationDirectoryOffset = DexPool.NO_OFFSET;

    PoolClassDef(@Nonnull ClassDef classDef) {
        this.classDef = classDef;

        interfaces = new TypeListPool.Key<List<String>>(Collections.unmodifiableList(new ArrayList<>(classDef.getInterfaces())));
        staticFields = ArraySortedSet.copyOf(CollectionUtils.naturalOrdering(), 
            IteratorUtils.toList(classDef.getStaticFields()));
        instanceFields = ArraySortedSet.copyOf(CollectionUtils.naturalOrdering(), 
            IteratorUtils.toList(classDef.getInstanceFields()));
        directMethods = ArraySortedSet.copyOf(CollectionUtils.naturalOrdering(),
                IteratorUtils.toList(classDef.getDirectMethods()).stream().map(PoolMethod.TRANSFORM)
                        .collect(Collectors.toList()));
        virtualMethods = ArraySortedSet.of(CollectionUtils.naturalOrdering(),
                IteratorUtils.toList(classDef.getVirtualMethods()).stream().map(PoolMethod.TRANSFORM)
                .collect(Collectors.toList()));
    }

    @Nonnull @Override public String getType() {
        return classDef.getType();
    }

    @Override public int getAccessFlags() {
        return classDef.getAccessFlags();
    }

    @Nullable @Override public String getSuperclass() {
        return classDef.getSuperclass();
    }

    @Nonnull @Override public List<String> getInterfaces() {
        return interfaces.types;
    }

    @Nullable @Override public String getSourceFile() {
        return classDef.getSourceFile();
    }

    @Nonnull @Override public Set<? extends Annotation> getAnnotations() {
        return classDef.getAnnotations();
    }

    @Nonnull @Override public SortedSet<Field> getStaticFields() {
        return staticFields;
    }

    @Nonnull @Override public SortedSet<Field> getInstanceFields() {
        return instanceFields;
    }

    @Nonnull @Override public Collection<Field> getFields() {
        return new AbstractCollection<Field>() {
            @Nonnull @Override public Iterator<Field> iterator() {
                ArrayList<Field> fields = new ArrayList<>(staticFields);
                fields.addAll(instanceFields);
                fields.sort(CollectionUtils.naturalOrdering());
                return fields.iterator();
            }

            @Override public int size() {
                return staticFields.size() + instanceFields.size();
            }
        };
    }

    @Nonnull @Override public SortedSet<PoolMethod> getDirectMethods() {
        return directMethods;
    }

    @Nonnull @Override public SortedSet<PoolMethod> getVirtualMethods() {
        return virtualMethods;
    }

    @Nonnull @Override public Collection<PoolMethod> getMethods() {
        return new AbstractCollection<PoolMethod>() {
            @Nonnull @Override public Iterator<PoolMethod> iterator() {
                ArrayList<PoolMethod> methods = new ArrayList<>(directMethods);
                methods.addAll(virtualMethods);
                methods.sort(CollectionUtils.naturalOrdering());
                return methods.iterator();
            }

            @Override public int size() {
                return directMethods.size() + virtualMethods.size();
            }
        };
    }
}
