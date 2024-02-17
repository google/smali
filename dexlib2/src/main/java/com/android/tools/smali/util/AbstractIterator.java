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

package com.android.tools.smali.util;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractIterator<T> implements Iterator<T>, Iterable<T> {
    /** We have computed the next element and haven't returned it yet. */
    private static final int STATE_READY = 1;

    /** We haven't yet computed or have already returned the element. */
    private static final int STATE_NOT_READY = 2;

    /** We have reached the end of the data and are finished. */
    private static final int STATE_DONE = 3;

    /** We've suffered an exception and are kaputt. */
    private static final int STATE_FAILED = 4;

    private int state = STATE_NOT_READY;
    private T next;

    protected final T endOfData() {
        state = STATE_DONE;
        return null;
    }

    @Override
    public final boolean hasNext() {
        switch (state) {
            case STATE_DONE:
                return false;
            case STATE_READY:
                return true;
            case STATE_FAILED:
                throw new IllegalStateException();
            default:
        }
        return tryToComputeNext();
    }

    private boolean tryToComputeNext() {
        state = STATE_FAILED; // temporary pessimism
        next = computeNext();
        if (state != STATE_DONE) {
            state = STATE_READY;
            return true;
        }
        return false;
    }

    @Override
    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = STATE_NOT_READY;
        T result = next;
        next = null;
        return result;
    }

    @Override
    public final Iterator<T> iterator() {
        return this;
    }

    /**
     * Computes next item for the iterator. If the end of the list has been reached, it should call
     * endOfData. endOfData has a return value of T, so you can simply {@code return endOfData()}
     *
     * @return The item that was read. If endOfData was called, the return value is ignored.
     */

    @Nullable
    protected abstract T computeNext();
}

