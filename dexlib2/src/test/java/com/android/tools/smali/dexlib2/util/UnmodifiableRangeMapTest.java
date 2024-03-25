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
import static org.junit.Assert.assertNull;

import com.android.tools.smali.util.Range;
import com.android.tools.smali.util.UnmodifiableRangeMap;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class UnmodifiableRangeMapTest {

  @Test
  public void testEmpty() {
    UnmodifiableRangeMap<Integer, String> map = UnmodifiableRangeMap.of();
    Assert.assertNull(map.get(0));
    Assert.assertNull(map.getEntry(0));
  }

  @Test
  public void testBuilder() {
    UnmodifiableRangeMap<Integer, String> rangeMap =
        UnmodifiableRangeMap.<Integer, String>builder()
            .put(Range.closed(1, 3), "a")
            .put(Range.closed(4, 6), "b")
            .build();
    assertEquals("a", rangeMap.get(2));
    assertEquals("b", rangeMap.get(5));
    assertNull(rangeMap.get(0));
    assertNull(rangeMap.get(7));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderEmptyRange() {
    UnmodifiableRangeMap.<Integer, String>builder().put(Range.open(1, 1), "a").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderOverlappingRanges() {
    UnmodifiableRangeMap.<Integer, String>builder()
        .put(Range.closed(1, 3), "a")
        .put(Range.closed(2, 4), "b")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderEdgeOverlappingRanges() {
    UnmodifiableRangeMap.<Integer, String>builder()
        .put(Range.closed(1, 3), "a")
        .put(Range.closed(3, 7), "b")
        .build();
  }

  @Test
  public void testGet_oddSizeMap() {
    UnmodifiableRangeMap<Integer, String> rangeMap =
        UnmodifiableRangeMap.<Integer, String>builder()
            .put(Range.closed(1, 3), "a")
            .put(Range.closed(4, 6), "b")
            .put(Range.closed(11, 20), "c")
            .build();
    assertEquals("a", rangeMap.get(2));
    assertEquals("c", rangeMap.get(15));
    assertNull(rangeMap.get(0));
    assertNull(rangeMap.get(7));
  }

  @Test
  public void testGet_evenSizeMap() {
    UnmodifiableRangeMap<Integer, String> rangeMap =
        UnmodifiableRangeMap.<Integer, String>builder()
            .put(Range.closed(1, 3), "a")
            .put(Range.closed(4, 6), "b")
            .put(Range.closed(11, 20), "c")
            .put(Range.closed(21, 30), "d")
            .build();
    assertEquals("a", rangeMap.get(2));
    assertEquals("b", rangeMap.get(6));
    assertEquals("c", rangeMap.get(15));
    assertEquals("d", rangeMap.get(23));
    assertNull(rangeMap.get(0));
    assertNull(rangeMap.get(7));
  }

  @Test
  public void testEntry() {
    UnmodifiableRangeMap<Integer, String> rangeMap =
        UnmodifiableRangeMap.<Integer, String>builder()
            .put(Range.closed(1, 3), "a")
            .put(Range.closed(4, 6), "b")
            .put(Range.closed(10, 15), "c")
            .build();
    Map.Entry<Range<Integer>, String> entry = rangeMap.getEntry(2);
    assertEquals(Range.closed(1, 3), entry.getKey());
    assertEquals("a", entry.getValue());
  }
}