/*
 * Copyright 2024, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google LLC nor the names of its
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

package com.android.tools.smali.util;

import java.lang.Iterable;
import java.util.Iterator;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Combines two iterators into a single iterator. The returned iterator iterates across the elements
 * in {@code a}, followed by the elements in {@code b}. The source iterators are not polled until
 * necessary.
 * <p>
 * The returned iterator does not support {@code remove()}.
 */
public class ChainedIterator<T extends @Nullable Object> implements Iterator<T>, Iterable<T> {
    Iterator<T> iteratorA;
    Iterator<T> iteratorB;

    public ChainedIterator(Iterable<T> iterableA, Iterable<T> iterableB) {
        this.iteratorA = iterableA.iterator();
        this.iteratorB = iterableB.iterator();
    }

    @Override
    public final boolean hasNext() {
        return iteratorA.hasNext() || iteratorB.hasNext();
    }

    @Override
    public final T next() {
        if (iteratorA.hasNext()) {
            return iteratorA.next();
        }
        return iteratorB.next();
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Iterator<T> iterator() {
        return this;
    }
}
