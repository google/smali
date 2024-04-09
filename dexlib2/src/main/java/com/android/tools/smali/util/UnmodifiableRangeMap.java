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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;

/* 
 * Based on guava's ImmutableRangeMap
 */
public class UnmodifiableRangeMap<K extends Comparable<?>, V> {
    private static final UnmodifiableRangeMap<Comparable<?>, Object> EMPTY = new UnmodifiableRangeMap<>(
            Collections.emptyList(), Collections.emptyList());

    /** Returns a new builder for an immutable range map. */
    public static <K extends Comparable<?>, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * Returns an empty immutable range map.
     * <p>
     * <b>Performance note:</b> the instance returned is a singleton.
     */
    public static <K extends Comparable<?>, V> UnmodifiableRangeMap<K, V> of() {
        return (UnmodifiableRangeMap<K, V>) EMPTY;
    }

    /**
     * A builder for immutable range maps. Overlapping ranges are prohibited.
     *
     * @since 14.0
     */
    public static final class Builder<K extends Comparable<?>, V> {

        private final List<Entry<Range<K>, V>> entries;

        public Builder() {
            this.entries = new ArrayList<>();
        }

        /**
         * Associates the specified range with the specified value.
         *
         * @throws IllegalArgumentException if {@code range} is empty
         */
        public Builder<K, V> put(Range<K> range, V value) {
            if (range == null || value == null) {
                throw new NullPointerException("Both range and value must be non-null");
            }

            if (range.isEmpty()) {
                throw new IllegalArgumentException("Ranges cannot be empty");
            }
            entries.add(new UnmodifiableEntry(range, value));
            return this;
        }

        /**
         * Returns an {@code ImmutableRangeMap} containing the associations previously added to this
         * builder.
         *
         * @throws IllegalArgumentException if any two ranges inserted into this builder overlap
         */
        public UnmodifiableRangeMap<K, V> build() {
            Collections.sort(entries,
                    (e1, e2) -> Range.RANGE_LEX_COMPARATOR.compare(e1.getKey(), e2.getKey()));
            ArrayList<Range<K>> rangesList = new ArrayList<>(entries.size());
            ArrayList<V> valuesList = new ArrayList<>(entries.size());
            for (int i = 0; i < entries.size(); i++) {
                Range<K> range = entries.get(i).getKey();
                if (i > 0) {
                    Range<K> prevRange = entries.get(i - 1).getKey();
                    if (range.isConnected(prevRange) && !range.intersection(prevRange).isEmpty()) {
                        throw new IllegalArgumentException(
                                "Overlapping ranges: range " + prevRange + " overlaps with entry "
                                        + range);
                    }
                }
                rangesList.add(range);
                valuesList.add(entries.get(i).getValue());
            }
            return new UnmodifiableRangeMap(rangesList, valuesList);
        }
    }

    private final transient List<Range<K>> ranges;
    private final transient List<V> values;

    private UnmodifiableRangeMap(List<Range<K>> ranges, List<V> values) {
        this.ranges = Collections.unmodifiableList(ranges);
        this.values = Collections.unmodifiableList(values);
    }

    @CheckForNull
    public V get(K key) {
        if (key == null) {
            return null;
        }

        int index = rangeBinarySearch(ranges, key);
        if (index == -1) {
            return null;
        } else {
            Range<K> range = ranges.get(index);
            return range.contains(key) ? values.get(index) : null;
        }
    }

    @CheckForNull
    public Entry<Range<K>, V> getEntry(K key) {
        if (key == null) {
            return null;
        }

        int index = rangeBinarySearch(ranges, key);
        if (index == -1) {
            return null;
        } else {
            Range<K> range = ranges.get(index);
            return range.contains(key) ? new UnmodifiableEntry(range, values.get(index)) : null;
        }
    }

    private static <T extends Comparable> int rangeBinarySearch(List<Range<T>> l, T key) {
        int low = 0;
        int high = l.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Range<T> midRange = l.get(mid);
            if (midRange.contains(key)) {
                return mid;
            }

            int cmp = midRange.hasLowerBound() ? key.compareTo(midRange.getLowerBound())
                    : key.compareTo(midRange.getUpperBound());

            if (cmp > 0)
                low = mid + 1;
            else if (cmp < 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -1; // key not found
    }

    public static class UnmodifiableEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        public UnmodifiableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
}
