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

package com.android.tools.smali.dexlib2.builder;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class LocatedItems<T extends ItemWithLocation> {
    // We end up creating and keeping around a *lot* of MethodLocation objects
    // when building a new dex file, so it's worth the trouble of lazily creating
    // the labels and debugItems lists only when they are needed
    @Nullable
    private List<T> items = null;

    @Nonnull
    private List<T> getItems() {
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    public Set<T> getModifiableItems(MethodLocation newItemsLocation) {
        return new AbstractSet<T>() {
            @Nonnull
            @Override
            public Iterator<T> iterator() {
                final Iterator<T> it = getItems().iterator();

                return new Iterator<T>() {
                    private @Nullable
                    T currentItem = null;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public T next() {
                        currentItem = it.next();
                        return currentItem;
                    }

                    @Override
                    public void remove() {
                        if (currentItem != null) {
                            currentItem.setLocation(null);
                        }
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return getItems().size();
            }

            @Override
            public boolean add(@Nonnull T item) {
                if (item.isPlaced()) {
                    throw new IllegalArgumentException(getAddLocatedItemError());
                }
                item.setLocation(newItemsLocation);
                addItem(item);
                return true;
            }
        };
    }

    private void addItem(@Nonnull T item) {
        if (items == null) {
            items = new ArrayList<>(1);
        }
        items.add(item);
    }

    protected abstract String getAddLocatedItemError();

    public void mergeItemsIntoNext(@Nonnull MethodLocation nextLocation, LocatedItems<T> otherLocatedItems) {
        if (otherLocatedItems == this) {
            return;
        }
        if (items != null) {
            for (T item : items) {
                item.setLocation(nextLocation);
            }
            List<T> mergedItems = items;
            mergedItems.addAll(otherLocatedItems.getItems());
            otherLocatedItems.items = mergedItems;
            items = null;
        }
    }
}
