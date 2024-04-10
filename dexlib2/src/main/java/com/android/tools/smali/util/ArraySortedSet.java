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

package com.android.tools.smali.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;

public class ArraySortedSet<T> implements SortedSet<T> {
    @Nonnull private final Comparator<? super T> comparator;
    @Nonnull private final Object[] arr;

    private ArraySortedSet(@Nonnull Comparator<? super T> comparator, @Nonnull T[] arr) {
        this.comparator = comparator;
        this.arr = arr;
        assert assertSorted();
    }

    private ArraySortedSet(@Nonnull Comparator<? super T> comparator, @Nonnull Collection<? extends T> collection) {
        this.comparator = comparator;
        this.arr = collection.toArray();
        assert assertSorted();
    }

    public static <T> ArraySortedSet<T> of(@Nonnull Comparator<? super T> comparator, @Nonnull T[] arr) {
        return new ArraySortedSet<T>(comparator, arr);
    }

    public static <T> ArraySortedSet<T> of(@Nonnull Comparator<? super T> comparator, @Nonnull Collection<? extends T> collection) {
        return new ArraySortedSet<T>(comparator, collection);
    }
    private boolean assertSorted() {
        for (int i = 1; i < arr.length; i++) {
          assert comparator.compare((T)arr[i - 1], (T)arr[i]) < 0;
        }
        return true;
    }

    @Override
    public int size() {
        return arr.length;
    }

    @Override
    public boolean isEmpty() {
        return arr.length > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Arrays.binarySearch((T[])arr, (T)o, comparator) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return Arrays.asList((T[])arr).iterator();
    }

    @Override
    public Object[] toArray() {
        return arr.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length <= arr.length) {
            System.arraycopy(arr, 0, (Object[])a, 0, arr.length);
            return a;
        }
        return Arrays.copyOf((T[])arr, arr.length);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T first() {
        if (arr.length == 0) {
            throw new NoSuchElementException();
        }
        return (T)arr[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T last() {
        if (arr.length == 0) {
            throw new NoSuchElementException();
        }
        return (T)arr[arr.length-1];
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Object o: arr) {
            result += o.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof SortedSet) {
            SortedSet other = (SortedSet)o;
            if (arr.length != other.size()) {
                return false;
            }
            return IteratorUtils.elementsEqual(iterator(), other.iterator());
        }
        if (o instanceof Set) {
            Set other = (Set)o;
            if (arr.length != other.size()) {
                return false;
            }
            return this.containsAll(other);
        }
        return false;
    }
}
