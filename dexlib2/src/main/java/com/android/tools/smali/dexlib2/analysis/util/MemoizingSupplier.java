/*
 * Copyright 2024, Google LLC
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

package com.android.tools.smali.dexlib2.analysis.util;

import java.util.function.Supplier;

/** 
 * Based on Guava's NonSerializableMemoizing Supplier. This implementation is thread safe.
 */
public class MemoizingSupplier<T> implements Supplier<T> {
    // Delegate will only be null when the value was successfuly computed
    private volatile Supplier<T> delegate;
    private T value;

    private MemoizingSupplier(Supplier<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate == null");
        }
        this.delegate = delegate;
    }

    @Override
    public T get() {
        // Because Supplier is read-heavy, we use the "double-checked locking" pattern.
        if (delegate != null) {
            synchronized (this) {
                if (delegate != null) {
                    T t = delegate.get();
                    value = t;
                    delegate = null;
                }
            }
        }
        // This is safe because we checked `delegate.`
        return value;
    }

    public static <T extends Object> MemoizingSupplier<T> memoize(Supplier<T> delegate) {
        if (delegate instanceof MemoizingSupplier) {
            return (MemoizingSupplier<T>) delegate;
        }
        return new MemoizingSupplier<>(delegate);
    }
}
