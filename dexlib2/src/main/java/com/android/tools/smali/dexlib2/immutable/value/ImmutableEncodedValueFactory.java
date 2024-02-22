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

package com.android.tools.smali.dexlib2.immutable.value;

import com.android.tools.smali.dexlib2.ValueType;
import com.android.tools.smali.dexlib2.iface.value.AnnotationEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.ArrayEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.BooleanEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.ByteEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.CharEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.DoubleEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.EncodedValue;
import com.android.tools.smali.dexlib2.iface.value.EnumEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.FieldEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.FloatEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.IntEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.LongEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.MethodEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.MethodHandleEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.MethodTypeEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.ShortEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue;
import com.android.tools.smali.dexlib2.iface.value.TypeEncodedValue;
import com.android.tools.smali.util.ExceptionWithContext;
import com.android.tools.smali.util.ImmutableConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ImmutableEncodedValueFactory {
    @Nonnull
    public static ImmutableEncodedValue of(@Nonnull EncodedValue encodedValue) {
        switch (encodedValue.getValueType()) {
            case ValueType.BYTE:
                return ImmutableByteEncodedValue.of((ByteEncodedValue)encodedValue);
            case ValueType.SHORT:
                return ImmutableShortEncodedValue.of((ShortEncodedValue)encodedValue);
            case ValueType.CHAR:
                return ImmutableCharEncodedValue.of((CharEncodedValue)encodedValue);
            case ValueType.INT:
                return ImmutableIntEncodedValue.of((IntEncodedValue)encodedValue);
            case ValueType.LONG:
                return ImmutableLongEncodedValue.of((LongEncodedValue)encodedValue);
            case ValueType.FLOAT:
                return ImmutableFloatEncodedValue.of((FloatEncodedValue)encodedValue);
            case ValueType.DOUBLE:
                return ImmutableDoubleEncodedValue.of((DoubleEncodedValue)encodedValue);
            case ValueType.STRING:
                return ImmutableStringEncodedValue.of((StringEncodedValue)encodedValue);
            case ValueType.TYPE:
                return ImmutableTypeEncodedValue.of((TypeEncodedValue)encodedValue);
            case ValueType.FIELD:
                return ImmutableFieldEncodedValue.of((FieldEncodedValue)encodedValue);
            case ValueType.METHOD:
                return ImmutableMethodEncodedValue.of((MethodEncodedValue)encodedValue);
            case ValueType.ENUM:
                return ImmutableEnumEncodedValue.of((EnumEncodedValue)encodedValue);
            case ValueType.ARRAY:
                return ImmutableArrayEncodedValue.of((ArrayEncodedValue)encodedValue);
            case ValueType.ANNOTATION:
                return ImmutableAnnotationEncodedValue.of((AnnotationEncodedValue)encodedValue);
            case ValueType.NULL:
                return ImmutableNullEncodedValue.INSTANCE;
            case ValueType.BOOLEAN:
                return ImmutableBooleanEncodedValue.of((BooleanEncodedValue)encodedValue);
            case ValueType.METHOD_HANDLE:
                return ImmutableMethodHandleEncodedValue.of((MethodHandleEncodedValue) encodedValue);
            case ValueType.METHOD_TYPE:
                return ImmutableMethodTypeEncodedValue.of((MethodTypeEncodedValue) encodedValue);
            default:
                throw new IllegalArgumentException("Invalid value type.");
        }
    }

    @Nonnull
    public static EncodedValue defaultValueForType(String type) {
        switch (type.charAt(0)) {
            case 'Z':
                return ImmutableBooleanEncodedValue.FALSE_VALUE;
            case 'B':
                return new ImmutableByteEncodedValue((byte)0);
            case 'S':
                return new ImmutableShortEncodedValue((short)0);
            case 'C':
                return new ImmutableCharEncodedValue((char)0);
            case 'I':
                return new ImmutableIntEncodedValue(0);
            case 'J':
                return new ImmutableLongEncodedValue(0);
            case 'F':
                return new ImmutableFloatEncodedValue(0);
            case 'D':
                return new ImmutableDoubleEncodedValue(0);
            case 'L':
            case '[':
                return ImmutableNullEncodedValue.INSTANCE;
            default:
                throw new ExceptionWithContext("Unrecognized type: %s", type);
        }
    }

    @Nullable
    public static ImmutableEncodedValue ofNullable(@Nullable EncodedValue encodedValue) {
        if (encodedValue == null) {
            return null;
        }
        return of(encodedValue);
    }

    @Nonnull
    public static List<ImmutableEncodedValue> immutableListOf
            (@Nullable Iterable<? extends EncodedValue> list) {
        return CONVERTER.toList(list);
    }

    private static final ImmutableConverter<ImmutableEncodedValue, EncodedValue> CONVERTER =
            new ImmutableConverter<ImmutableEncodedValue, EncodedValue>() {
                @Override
                protected boolean isImmutable(@Nonnull EncodedValue item) {
                    return item instanceof ImmutableEncodedValue;
                }

                @Nonnull
                @Override
                protected ImmutableEncodedValue makeImmutable(@Nonnull EncodedValue item) {
                    return of(item);
                }
            };
}
