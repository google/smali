/*
 * Copyright 2018, Google LLC
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

package org.jf.dexlib2.builder;

import com.google.common.collect.Sets;
import org.jf.dexlib2.builder.debug.BuilderLineNumber;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LocatedItemsTest {

    private List<BuilderDebugItem> createItems(int count) {
        List<BuilderDebugItem> items = new ArrayList<>();
        for(int i = 0; i < count; ++i) {
            items.add(new BuilderLineNumber(i));
        }
        return items;
    }

    private void doTestMergeIntoKeepsOrderOfDebugItems(int countLocation1, int countLocation2) {
        MethodLocation location1 = new MethodLocation(null, 123, 1);
        MethodLocation location2 = new MethodLocation(null, 456, 2);

        List<BuilderDebugItem> items1 = createItems(countLocation1);
        List<BuilderDebugItem> items2 = createItems(countLocation2);
        location1.getDebugItems().addAll(items1);
        location2.getDebugItems().addAll(items2);

        location1.mergeInto(location2);

        Assert.assertEquals(Sets.newHashSet(), location1.getDebugItems());
        // items1 appear BEFORE items2
        List<BuilderDebugItem> expectedItems = new ArrayList<>(items1);
        expectedItems.addAll(items2);
        Assert.assertEquals(expectedItems, new ArrayList<>(location2.getDebugItems()));
    }

    @Test
    public void testMergeIntoKeepsOrderOfDebugItems() {
        doTestMergeIntoKeepsOrderOfDebugItems(2, 2);
        doTestMergeIntoKeepsOrderOfDebugItems(0, 0);
        doTestMergeIntoKeepsOrderOfDebugItems(0, 2);
        doTestMergeIntoKeepsOrderOfDebugItems(2, 0);
    }
}
