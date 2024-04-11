
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.android.tools.smali.util.Range;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RangeTest {

    @Test
    public void testClosed() {
        Range<Integer> range = Range.closed(1, 10);
        assertTrue(range.contains(1));
        assertTrue(range.contains(10));
        assertTrue(range.contains(5));
        assertFalse(range.contains(0));
        assertFalse(range.contains(11));
    }

    @Test
    public void testOpenClosed() {
        Range<Integer> range = Range.openClosed(1, 10);
        assertTrue(range.contains(10));
        assertFalse(range.contains(1));
        assertTrue(range.contains(5));
        assertFalse(range.contains(0));
        assertFalse(range.contains(11));
    }

    @Test
    public void testClosedOpen() {
        Range<Integer> range = Range.closedOpen(1, 10);
        assertFalse(range.contains(10));
        assertTrue(range.contains(1));
        assertTrue(range.contains(5));
        assertFalse(range.contains(0));
        assertFalse(range.contains(11));
    }

    @Test
    public void testOpen() {
        Range<Integer> range = Range.open(1, 10);
        assertFalse(range.contains(10));
        assertFalse(range.contains(1));
        assertTrue(range.contains(5));
        assertFalse(range.contains(11));
        assertFalse(range.contains(0));
    }

    @Test
    public void testAtLeast() {
        Range<Integer> range = Range.atLeast(1);
        assertTrue(range.contains(1));
        assertTrue(range.contains(10));
        assertFalse(range.contains(0));
    }

    @Test
    public void testAtMost() {
        Range<Integer> range = Range.atMost(10);
        assertFalse(range.contains(11));
        assertTrue(range.contains(10));
        assertTrue(range.contains(5));
    }

    @Test
    public void testAllValues() {
        Range<Integer> range = Range.allValues();
        assertTrue(range.contains(1));
        assertTrue(range.contains(10));
        assertTrue(range.contains(0));
        assertTrue(range.contains(11));
    }

    @Test
    public void testEquals() {
        Range<Integer> range1 = Range.closed(1, 10);
        Range<Integer> range2 = Range.closed(1, 10);
        assertTrue(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.open(1, 10);
        assertTrue(range1.equals(range2));

        range1 = Range.closedOpen(1, 10);
        range2 = Range.closedOpen(1, 10);
        assertTrue(range1.equals(range2));

        range1 = Range.openClosed(1, 10);
        range2 = Range.openClosed(1, 10);
        assertTrue(range1.equals(range2));

        range1 = Range.atLeast(1);
        range2 = Range.atLeast(1);
        assertTrue(range1.equals(range2));

        range1 = Range.atMost(10);
        range2 = Range.atMost(10);
        assertTrue(range1.equals(range2));

        range1 = Range.allValues();
        range2 = Range.allValues();
        assertTrue(range1.equals(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.closed(1, 11);
        assertFalse(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.open(0, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.open(1, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.closedOpen(1, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.closedOpen(1, 10);
        range2 = Range.openClosed(1, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.atLeast(1);
        assertFalse(range1.equals(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.atMost(10);
        assertFalse(range1.equals(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.allValues();
        assertFalse(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.closedOpen(1, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.openClosed(1, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.atLeast(1);
        assertFalse(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.atMost(10);
        assertFalse(range1.equals(range2));

        range1 = Range.open(1, 10);
        range2 = Range.allValues();
        assertFalse(range1.equals(range2));

        range1 = Range.closedOpen(1, 10);
        range2 = Range.openClosed(1, 10);
        assertFalse(range1.equals(range2));

        range1 = Range.closedOpen(1, 10);
        range2 = Range.atLeast(1);
        assertFalse(range1.equals(range2));

        range1 = Range.closedOpen(1, 10);
        range2 = Range.atMost(10);
        assertFalse(range1.equals(range2));

        range1 = Range.closedOpen(1, 10);
        range2 = Range.allValues();
        assertFalse(range1.equals(range2));

        range1 = Range.openClosed(1, 10);
        range2 = Range.atLeast(1);
        assertFalse(range1.equals(range2));

        range1 = Range.openClosed(1, 10);
        range2 = Range.atMost(10);
        assertFalse(range1.equals(range2));

        range1 = Range.openClosed(1, 10);
        range2 = Range.allValues();
        assertFalse(range1.equals(range2));

        range1 = Range.atLeast(1);
        range2 = Range.atMost(1);
        assertFalse(range1.equals(range2));

        range1 = Range.atLeast(1);
        range2 = Range.allValues();
        assertFalse(range1.equals(range2));

        range1 = Range.atMost(10);
        range2 = Range.allValues();
        assertFalse(range1.equals(range2));
    }

    @Test
    public void testIsEmpty() {
        Range<Integer> range = Range.closed(1, 1);
        assertFalse(range.isEmpty());

        range = Range.openClosed(2, 2);
        assertTrue(range.isEmpty());

        range = Range.closedOpen(3, 3);
        assertTrue(range.isEmpty());

        range = Range.open(4, 4);
        assertTrue(range.isEmpty());

        range = Range.open(4, 5);
        assertFalse(range.isEmpty());

        range = Range.atMost(10);
        assertFalse(range.isEmpty());

        range = Range.atLeast(10);
        assertFalse(range.isEmpty());

        range = Range.allValues();
        assertFalse(range.isEmpty());
    }

    @Test
    public void testGetLowerBound() {
        Range<Integer> range = Range.closed(1, 10);
        assertEquals((Integer) 1, range.getLowerBound());
    }

    @Test
    public void testGetUpperBound() {
        Range<Integer> range = Range.closed(1, 10);
        assertEquals((Integer) 10, range.getUpperBound());
    }

    @Test
    public void testHasLowerBound() {
        assertTrue(Range.closed(1, 10).hasLowerBound());
        assertTrue(Range.open(1, 10).hasLowerBound());
        assertTrue(Range.openClosed(1, 10).hasLowerBound());
        assertTrue(Range.closedOpen(1, 10).hasLowerBound());
        assertTrue(Range.atLeast(1).hasLowerBound());
        assertFalse(Range.atMost(10).hasLowerBound());
        assertFalse(Range.allValues().hasLowerBound());
    }

    @Test
    public void testHasUpperBound() {
        assertTrue(Range.closed(1, 10).hasUpperBound());
        assertTrue(Range.open(1, 10).hasUpperBound());
        assertTrue(Range.openClosed(1, 10).hasUpperBound());
        assertTrue(Range.closedOpen(1, 10).hasUpperBound());
        assertFalse(Range.atLeast(1).hasUpperBound());
        assertTrue(Range.atMost(10).hasUpperBound());
        assertFalse(Range.allValues().hasUpperBound());
    }

    @Test
    public void testIsConnected() {
        Range<Integer> range1 = Range.closed(1, 5);
        Range<Integer> range2 = Range.closed(3, 7);
        assertTrue(range1.isConnected(range2));
        assertTrue(range2.isConnected(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(6, 7);
        assertFalse(range1.isConnected(range2));
        assertFalse(range2.isConnected(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(0, 2);
        assertTrue(range1.isConnected(range2));
        assertTrue(range2.isConnected(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(10, 20);
        assertFalse(range1.isConnected(range2));
        assertFalse(range2.isConnected(range1));
    }

    @Test
    public void testIsConnected_edgeConnection() {
        Range<Integer> range1 = Range.closed(1, 5);
        Range<Integer> range2 = Range.closed(5, 7);
        assertTrue(range1.isConnected(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(3, 5);
        assertTrue(range1.isConnected(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(1, 3);
        assertTrue(range1.isConnected(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(1, 5);
        assertTrue(range1.isConnected(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(5, 5);
        assertTrue(range1.isConnected(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.openClosed(5, 10);
        assertTrue(range1.isConnected(range2));
    }

    @Test
    public void testIntersection() {
        Range<Integer> range1 = Range.closed(1, 5);
        Range<Integer> range2 = Range.closed(3, 7);
        assertEquals(Range.closed(3, 5), range1.intersection(range2));
        assertEquals(Range.closed(3, 5), range2.intersection(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(6, 7);
        assertNull(range1.intersection(range2));
        assertNull(range2.intersection(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(0, 2);
        assertEquals(Range.closed(1, 2), range1.intersection(range2));
        assertEquals(Range.closed(1, 2), range2.intersection(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(5, 7);
        assertEquals(Range.closed(5, 5), range1.intersection(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(3, 5);
        assertEquals(Range.closed(3, 5), range1.intersection(range2));
        assertEquals(Range.closed(3, 5), range2.intersection(range1));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(2, 4);
        assertEquals(Range.closed(2, 4), range1.intersection(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(1, 3);
        assertEquals(Range.closed(1, 3), range1.intersection(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(1, 5);
        assertEquals(Range.closed(1, 5), range1.intersection(range2));

        range1 = Range.closed(1, 5);
        range2 = Range.closed(5, 5);
        assertEquals(Range.closed(5, 5), range1.intersection(range2));
    }

    @Test
    public void testIntersection_openBounds() {
        Range<Integer> range1 = Range.openClosed(1, 5);
        Range<Integer> range2 = Range.closed(3, 7);
        assertEquals(Range.closed(3, 5), range1.intersection(range2));

        range1 = Range.openClosed(1, 5);
        range2 = Range.closed(6, 7);
        assertNull(range1.intersection(range2));

        range1 = Range.openClosed(1, 5);
        range2 = Range.closed(5, 7);
        assertEquals(Range.closed(5, 5), range1.intersection(range2));

        range1 = Range.openClosed(4, 6);
        range2 = Range.closed(1, 5);
        assertEquals(Range.openClosed(4, 5), range1.intersection(range2));

        range1 = Range.closedOpen(1, 5);
        range2 = Range.closed(3, 5);
        assertEquals(Range.closedOpen(3, 5), range1.intersection(range2));

        range1 = Range.openClosed(1, 5);
        range2 = Range.closed(2, 4);
        assertEquals(Range.closed(2, 4), range1.intersection(range2));

        range1 = Range.open(1, 5);
        range2 = Range.closed(1, 3);
        assertEquals(Range.openClosed(1, 3), range1.intersection(range2));

        range1 = Range.open(1, 5);
        range2 = Range.closed(1, 5);
        assertEquals(Range.open(1, 5), range1.intersection(range2));

        range1 = Range.closed(1, 10);
        range2 = Range.open(2, 4);
        assertEquals(Range.open(2, 4), range1.intersection(range2));

        range1 = Range.open(1, 10);
        range2 = Range.closed(2, 5);
        assertEquals(Range.closed(2, 5), range1.intersection(range2));

        range1 = Range.openClosed(1, 5);
        range2 = Range.closed(1, 5);
        assertEquals(Range.openClosed(1, 5), range1.intersection(range2));

        range1 = Range.closedOpen(1, 5);
        range2 = Range.closed(1, 5);
        assertEquals(Range.closedOpen(1, 5), range1.intersection(range2));
    }

    @Test
    public void testIntersection_atLeastAtMost(){
        Range<Integer> range1 = Range.atLeast(1);
        Range<Integer> range2 = Range.atLeast(5);
        assertEquals(Range.atLeast(5), range1.intersection(range2));

        range1 = Range.atMost(1);
        range2 = Range.atMost(5);
        assertEquals(Range.atMost(1), range1.intersection(range2));
    }

    @Test
    public void testRangeLexComparator() {
        List<Range<Integer>> ranges = Arrays.asList(
                Range.closed(1, 3),
                Range.closed(2, 4),
                Range.closed(1, 4),
                Range.closed(2, 3),
                Range.closed(7, 10),
                Range.closed(3, 4),
                Range.closed(1, 2));
        ranges.sort(Range.RANGE_LEX_COMPARATOR);
        assertEquals(
                Arrays.asList(
                        Range.closed(1, 2),
                        Range.closed(1, 3),
                        Range.closed(1, 4),
                        Range.closed(2, 3),
                        Range.closed(2, 4),
                        Range.closed(3, 4),
                        Range.closed(7, 10)),
                ranges);
    }

    @Test
    public void testRangeLexComparator_openBounds() {
        List<Range<Integer>> ranges = Arrays.asList(
                Range.closed(2, 4),
                Range.closed(1, 4),
                Range.closed(2, 3),
                Range.atLeast(7),
                Range.atMost(3),
                Range.closed(3, 4),
                Range.closed(1, 2));
        ranges.sort(Range.RANGE_LEX_COMPARATOR);
        assertEquals(
                Arrays.asList(
                    Range.atMost(3),
                    Range.closed(1, 2),
                    Range.closed(1, 4),
                    Range.closed(2, 3),
                    Range.closed(2, 4),
                    Range.closed(3, 4),
                    Range.atLeast(7)),
                ranges);
    }
}
