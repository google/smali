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

import static java.util.Collections.unmodifiableSortedSet;

import com.android.tools.smali.dexlib2.HiddenApiRestriction;
import com.android.tools.smali.dexlib2.base.reference.BaseFieldReference;
import com.android.tools.smali.dexlib2.iface.Annotation;
import com.android.tools.smali.dexlib2.iface.Field;
import com.android.tools.smali.dexlib2.iface.value.EncodedValue;
import com.android.tools.smali.dexlib2.immutable.value.ImmutableEncodedValue;
import com.android.tools.smali.dexlib2.immutable.value.ImmutableEncodedValueFactory;
import com.android.tools.smali.util.CollectionUtils;
import com.android.tools.smali.util.ImmutableConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

public class ImmutableField extends BaseFieldReference implements Field {
    @Nonnull protected final String definingClass;
    @Nonnull protected final String name;
    @Nonnull protected final String type;
    protected final int accessFlags;
    @Nullable protected final ImmutableEncodedValue initialValue;
    @Nonnull protected final Set<? extends ImmutableAnnotation> annotations;
    @Nonnull protected final Set<HiddenApiRestriction> hiddenApiRestrictions;

    public ImmutableField(@Nonnull String definingClass,
                          @Nonnull String name,
                          @Nonnull String type,
                          int accessFlags,
                          @Nullable EncodedValue initialValue,
                          @Nullable Collection<? extends Annotation> annotations,
                          @Nullable Set<HiddenApiRestriction> hiddenApiRestrictions) {
        this.definingClass = definingClass;
        this.name = name;
        this.type = type;
        this.accessFlags = accessFlags;
        this.initialValue = ImmutableEncodedValueFactory.ofNullable(initialValue);
        this.annotations = ImmutableAnnotation.immutableSetOf(annotations);
        this.hiddenApiRestrictions =
                hiddenApiRestrictions == null ? Collections.emptySet() : Set.copyOf(hiddenApiRestrictions);
    }

    public static ImmutableField of(Field field) {
        if (field instanceof  ImmutableField) {
            return (ImmutableField)field;
        }
        return new ImmutableField(
                field.getDefiningClass(),
                field.getName(),
                field.getType(),
                field.getAccessFlags(),
                field.getInitialValue(),
                field.getAnnotations(),
                field.getHiddenApiRestrictions());
    }

    @Nonnull @Override public String getDefiningClass() { return definingClass; }
    @Nonnull @Override public String getName() { return name; }
    @Nonnull @Override public String getType() { return type; }
    @Override public int getAccessFlags() { return accessFlags; }
    @Override public EncodedValue getInitialValue() { return initialValue;}
    @Nonnull @Override public Set<? extends ImmutableAnnotation> getAnnotations() { return annotations; }
    @Nonnull @Override public Set<HiddenApiRestriction> getHiddenApiRestrictions() { return hiddenApiRestrictions; }

    @Nonnull
    public static SortedSet<ImmutableField> immutableSetOf(@Nullable Iterable<? extends Field> list) {
        return unmodifiableSortedSet(CONVERTER.toSortedSet(CollectionUtils.naturalOrdering(), list));
    }

    private static final ImmutableConverter<ImmutableField, Field> CONVERTER =
            new ImmutableConverter<ImmutableField, Field>() {
                @Override
                protected boolean isImmutable(@Nonnull Field item) {
                    return item instanceof ImmutableField;
                }

                @Nonnull
                @Override
                protected ImmutableField makeImmutable(@Nonnull Field item) {
                    return ImmutableField.of(item);
                }
            };
}
