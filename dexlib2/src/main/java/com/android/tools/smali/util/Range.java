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

import java.util.Comparator;
import java.util.Objects;

/*
 * Represents a Range of values of type C. It can have open bounds if the range doesn't include the
 * bound value. It can also be unbounded on the lower or upper side. Simplified version of a guava
 * Range.
 */
public class Range<C extends Comparable> {
    public static final Comparator<Range<?>> RANGE_LEX_COMPARATOR = new Comparator<Range<?>>() {
        @Override
        public int compare(Range<?> left, Range<?> right) {
            int cmp = 0;
            if (!left.hasLowerBound() && right.hasLowerBound()) {
                return -1;
            } else if (!right.hasLowerBound() && left.hasLowerBound()) {
                return 1;
            } else if (left.hasLowerBound() && right.hasLowerBound()) {
                cmp = left.lowerBound.compareTo(right.lowerBound);
            }

            if (cmp != 0) {
                return cmp;
            }
            if (!left.hasUpperBound() && right.hasUpperBound()) {
                return 1;
            } else if (!right.hasUpperBound() && left.hasUpperBound()) {
                return -1;
            } else if (left.hasUpperBound() && right.hasUpperBound()) {
                cmp = left.upperBound.compareTo(right.upperBound);
            }
            return cmp;
        }
    };

    private C lowerBound;
    private C upperBound;
    private boolean lowerOpen;
    private boolean upperOpen;
    private boolean allValues;

    public static <C extends Comparable> Range<C> closed(C lowerBound, C upperBound) {
        if (lowerBound == null || upperBound == null) {
            throw new NullPointerException();
        }
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lowerBound must be <= upperBound");
        }
        return new Range<>(lowerBound, upperBound, false, false);
    }

    public static <C extends Comparable> Range<C> open(C lowerBound, C upperBound) {
        if (lowerBound == null || upperBound == null) {
            throw new NullPointerException();
        }
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lowerBound must be <= upperBound");
        }
        return new Range<>(lowerBound, upperBound, true, true);
    }

    public static <C extends Comparable> Range<C> openClosed(C lowerBound, C upperBound) {
        if (lowerBound == null || upperBound == null) {
            throw new NullPointerException();
        }
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lowerBound must be <= upperBound");
        }
        return new Range<>(lowerBound, upperBound, true, false);
    }

    public static <C extends Comparable> Range<C> closedOpen(C lowerBound, C upperBound) {
        if (lowerBound == null || upperBound == null) {
            throw new NullPointerException();
        }
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lowerBound must be <= upperBound");
        }
        return new Range<>(lowerBound, upperBound, false, true);
    }

    public static <C extends Comparable> Range<C> atLeast(C lowerBound) {
        if (lowerBound == null) {
            throw new NullPointerException();
        }
        return new Range<>(lowerBound, null, false, false);
    }

    public static <C extends Comparable> Range<C> atMost(C upperBound) {
        if (upperBound == null) {
            throw new NullPointerException();
        }
        return new Range<>(null, upperBound, false, false);
    }

    public static <C extends Comparable> Range<C> allValues() {
        return new Range<>();
    }

    private Range(C lowerBound, C upperBound, boolean lowerOpen, boolean upperOpen) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerOpen = lowerOpen;
        this.upperOpen = upperOpen;
        allValues = false;
    }

    private Range() {
        allValues = true;
        lowerBound = null;
        upperBound = null;
        lowerOpen = false;
        upperOpen = false;
    }

    public boolean isEmpty() {
        return !allValues
                && Objects.equals(lowerBound, upperBound)
                && (lowerOpen || upperOpen);
    }

    public C getLowerBound() {
        return lowerBound;
    }

    public C getUpperBound() {
        return upperBound;
    }

    public boolean openLowerBound() {
        return lowerOpen;
    }

    public boolean openUpperBound() {
        return upperOpen;
    }

    public boolean hasAllValues() {
        return allValues;
    }

    public boolean hasLowerBound() {
        return lowerBound != null;
    }

    public boolean hasUpperBound() {
        return upperBound != null;
    }

    /* Returns true if value is included in the Range */
    public boolean contains(C value) {
        if (value == null) {
            return false;
        }
        if (allValues) {
            return true;
        }

        if (lowerBound != null) {
            if (lowerOpen && value.compareTo(lowerBound) == 0) {
                return false;
            }
            if (value.compareTo(lowerBound) < 0) {
                return false;
            }
        }
        if (upperBound != null) {
            if (upperOpen && value.compareTo(upperBound) == 0) {
                return false;
            }
            if (value.compareTo(upperBound) > 0) {
                return false;
            }
        }
        return true;
    }

    /*
     * Returns true if there exists a (possibly empty) range which is enclosed by both this range
     * and other.
     */
    public boolean isConnected(Range<C> other) {
        return (!hasLowerBound() || !other.hasUpperBound()
                || lowerBound.compareTo(other.getUpperBound()) <= 0)
                && (!hasUpperBound() || !other.hasLowerBound()
                        || other.getLowerBound().compareTo(upperBound) <= 0);
    }

    /* Returns the maximal range enclosed by both this range and other, if such a range exists. */
    public Range<C> intersection(Range<C> other) {
        if (!isConnected(other)) {
            return null;
        }

        // select the max of the lowerBounds. If they're equal,
        // choose the range that has an open lower bound
        Range<C> lowerBoundRange;
        if (!hasLowerBound() || !other.hasLowerBound()) {
            lowerBoundRange = hasLowerBound() ? this : other;
        } else if (Objects.equals(lowerBound, other.getLowerBound())) {
            lowerBoundRange = lowerOpen ? this : other;
        } else {
            lowerBoundRange = lowerBound.compareTo(other.getLowerBound()) > 0 ? this : other;
        }

        // and the min of the upperBounds, or the open upper bound if they're equal
        Range<C> upperBoundRange;
        if (!hasUpperBound() || !other.hasUpperBound()) {
            upperBoundRange = hasUpperBound() ? this : other;
        } else if (Objects.equals(upperBound, other.getUpperBound())) {
            upperBoundRange = upperOpen ? this : other;
        } else {
            upperBoundRange = upperBound.compareTo(other.getUpperBound()) < 0 ? this : other;
        }

        return new Range<C>(
                lowerBoundRange.getLowerBound(),
                upperBoundRange.getUpperBound(),
                lowerBoundRange.openLowerBound(),
                upperBoundRange.openUpperBound());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Range)) {
            return false;
        }
        Range<?> other = (Range<?>) o;

        if (allValues != other.hasAllValues()) {
            return false;
        }

        return Objects.equals(lowerBound, other.lowerBound)
                && Objects.equals(upperBound, other.upperBound)
                && lowerOpen == other.openLowerBound()
                && upperOpen == other.openUpperBound();
    }

    @Override
    public String toString() {
        if (allValues) {
            return "[*]";
        }
        String sb = "";
        if (lowerOpen) {
            sb += "(";
        } else {
            sb += "[";
        }
        sb += lowerBound;
        sb += ", ";
        sb += upperBound;
        if (upperOpen) {
            sb += ")";
        } else {
            sb += "]";
        }
        return sb;
    }

}
