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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ArraySortedSetTest {
    @Test
    public void testOf() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(), new Integer[] {
                1, 2, 3
        });
        Assert.assertEquals(set.size(), 3);
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(2));
        Assert.assertTrue(set.contains(3));
        Assert.assertFalse(set.contains(4));
    }

    @Test
    public void testOfCollection() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(), list);
        Assert.assertEquals(set.size(), 3);
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(2));
        Assert.assertTrue(set.contains(3));
        Assert.assertFalse(set.contains(4));
    }

    @Test(expected = AssertionError.class)
    public void testOfUnsorted() {
        ArraySortedSet.of(Comparator.naturalOrder(), new Integer[] {
                3, 1, 2
        });
    }

    @Test(expected = AssertionError.class)
    public void testOfCollectionUnsorted() {
        ArraySortedSet.of(Comparator.naturalOrder(), Arrays.asList(3, 1, 2));
    }

    @Test
    public void testIterator() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Iterator<Integer> it = set.iterator();
        Assert.assertEquals(it.next(), (Integer) 1);
        Assert.assertEquals(it.next(), (Integer) 2);
        Assert.assertEquals(it.next(), (Integer) 3);
    }

    @Test
    public void testToArray() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertArrayEquals(set.toArray(), new Integer[] {
                1, 2, 3
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAdd() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        set.add(4);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        set.remove(2);
    }

    @Test
    public void testContainsAll() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertTrue(set.containsAll(Arrays.asList(1, 2)));
        Assert.assertTrue(set.containsAll(Arrays.asList(1, 2, 3)));
        Assert.assertFalse(set.containsAll(Arrays.asList(1, 2, 3, 4)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAll() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        set.addAll(Arrays.asList(4));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        set.removeAll(Arrays.asList(2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        set.clear();
    }

    @Test
    public void testComparator() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertEquals(set.comparator(), Comparator.naturalOrder());
    }

    @Test
    public void testFirst() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertEquals(set.first(), (Integer) 1);
    }

    @Test
    public void testLast() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertEquals(set.last(), (Integer) 3);
    }

    @Test
    public void testHashCode() {
        ArraySortedSet<Integer> set = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertEquals(set.hashCode(), 6);
    }

    @Test
    public void testEquals() {
        ArraySortedSet<Integer> set1 = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        ArraySortedSet<Integer> set2 = ArraySortedSet.of(Comparator.naturalOrder(),
                Arrays.asList(1, 2, 3));
        Assert.assertTrue(set1.equals(set2));
    }
}
