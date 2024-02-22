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

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class ImmutableConverter<ImmutableItem, Item> {
    protected abstract boolean isImmutable(@Nonnull Item item);
    @Nonnull protected abstract ImmutableItem makeImmutable(@Nonnull Item item);

    @Nonnull
    public List<ImmutableItem> toList(@Nullable final Iterable<? extends Item> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        boolean needsCopy = false;
        if (iterable instanceof List) {
            for (Item element: iterable) {
                if (!isImmutable(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            return unmodifiableList((List<ImmutableItem>)iterable);
        }

        final Iterator<? extends Item> iter = iterable.iterator();

        ArrayList<ImmutableItem> list = new ArrayList<ImmutableItem>();
        while (iter.hasNext()) {
            list.add(makeImmutable(iter.next()));
        }

        return unmodifiableList(list);
    }

    @Nonnull
    public Set<ImmutableItem> toSet(@Nullable final Iterable<? extends Item> iterable) {
        if (iterable == null) {
            return Collections.emptySet();
        }

        boolean needsCopy = false;
        if (iterable instanceof Set) {
            for (Item element: iterable) {
                if (!isImmutable(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            return unmodifiableSet((Set<ImmutableItem>)iterable);
        }

        final Iterator<? extends Item> iter = iterable.iterator();

        HashSet<ImmutableItem> set = new HashSet<ImmutableItem>();
        while (iter.hasNext()) {
            set.add(makeImmutable(iter.next()));
        }
        return unmodifiableSet(set);
    }

    @Nonnull
    public SortedSet<ImmutableItem> toSortedSet(@Nonnull Comparator<? super ImmutableItem> comparator,
                                                         @Nullable final Iterable<? extends Item> iterable) {
        if (iterable == null) {
            return Collections.emptySortedSet();
        }

        boolean needsCopy = false;
        if (iterable instanceof SortedSet &&
                ((SortedSet)iterable).comparator().equals(comparator)) {
            for (Item element: iterable) {
                if (!isImmutable(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            return unmodifiableSortedSet((SortedSet<ImmutableItem>)iterable);
        }

        final Iterator<? extends Item> iter = iterable.iterator();

        TreeSet<ImmutableItem> treeSet = new TreeSet<ImmutableItem>(comparator);
        while (iter.hasNext()) {
            treeSet.add(makeImmutable(iter.next()));
        }
        return unmodifiableSortedSet(treeSet);
    }
}
