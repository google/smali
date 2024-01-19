/*
 * Copyright 2013, Google LLC
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

package com.android.tools.smali.dexlib2.dexbacked.raw.util;

import com.android.tools.smali.dexlib2.dexbacked.raw.AnnotationDirectoryItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.AnnotationItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.AnnotationSetItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.AnnotationSetRefList;
import com.android.tools.smali.dexlib2.dexbacked.raw.CallSiteIdItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.CdexDebugOffsetTable;
import com.android.tools.smali.dexlib2.dexbacked.raw.ClassDataItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.ClassDefItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.CodeItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.DebugInfoItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.EncodedArrayItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.FieldIdItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.HeaderItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.HiddenApiClassDataItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.ItemType;
import com.android.tools.smali.dexlib2.dexbacked.raw.MapItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.MethodHandleItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.MethodIdItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.ProtoIdItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.SectionAnnotator;
import com.android.tools.smali.dexlib2.dexbacked.raw.StringDataItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.StringIdItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.TypeIdItem;
import com.android.tools.smali.dexlib2.dexbacked.raw.TypeListItem;
import com.android.tools.smali.dexlib2.util.AnnotatedBytes;

import com.android.tools.smali.dexlib2.dexbacked.CDexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DexAnnotator extends AnnotatedBytes {
    @Nonnull public final DexBackedDexFile dexFile;

    private final Map<Integer, SectionAnnotator> annotators = new HashMap<>();
    private static final Map<Integer, Integer> sectionAnnotationOrder = new HashMap<>();

    static {
        int[] sectionOrder = new int[] {
                ItemType.MAP_LIST,

                ItemType.HEADER_ITEM,
                ItemType.STRING_ID_ITEM,
                ItemType.TYPE_ID_ITEM,
                ItemType.PROTO_ID_ITEM,
                ItemType.FIELD_ID_ITEM,
                ItemType.METHOD_ID_ITEM,
                ItemType.CALL_SITE_ID_ITEM,
                ItemType.METHOD_HANDLE_ITEM,

                // these need to be ordered like this, so the item identities can be propagated
                ItemType.CLASS_DEF_ITEM,
                ItemType.CLASS_DATA_ITEM,
                ItemType.CODE_ITEM,
                ItemType.DEBUG_INFO_ITEM,

                ItemType.TYPE_LIST,
                ItemType.ANNOTATION_SET_REF_LIST,
                ItemType.ANNOTATION_SET_ITEM,
                ItemType.STRING_DATA_ITEM,
                ItemType.ANNOTATION_ITEM,
                ItemType.ENCODED_ARRAY_ITEM,
                ItemType.ANNOTATION_DIRECTORY_ITEM,

                ItemType.HIDDENAPI_CLASS_DATA_ITEM
        };

        for (int i=0; i<sectionOrder.length; i++) {
            sectionAnnotationOrder.put(sectionOrder[i], i);
        }
    }

    public DexAnnotator(@Nonnull DexBackedDexFile dexFile, int width) {
        super(width);

        this.dexFile = dexFile;

        for (MapItem mapItem: dexFile.getMapItems()) {
            switch (mapItem.getType()) {
                case ItemType.HEADER_ITEM:
                    annotators.put(mapItem.getType(), HeaderItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.STRING_ID_ITEM:
                    annotators.put(mapItem.getType(), StringIdItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.TYPE_ID_ITEM:
                    annotators.put(mapItem.getType(), TypeIdItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.PROTO_ID_ITEM:
                    annotators.put(mapItem.getType(), ProtoIdItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.FIELD_ID_ITEM:
                    annotators.put(mapItem.getType(), FieldIdItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.METHOD_ID_ITEM:
                    annotators.put(mapItem.getType(), MethodIdItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.CLASS_DEF_ITEM:
                    annotators.put(mapItem.getType(), ClassDefItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.MAP_LIST:
                    annotators.put(mapItem.getType(), MapItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.TYPE_LIST:
                    annotators.put(mapItem.getType(), TypeListItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.ANNOTATION_SET_REF_LIST:
                    annotators.put(mapItem.getType(), AnnotationSetRefList.makeAnnotator(this, mapItem));
                    break;
                case ItemType.ANNOTATION_SET_ITEM:
                    annotators.put(mapItem.getType(), AnnotationSetItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.CLASS_DATA_ITEM:
                    annotators.put(mapItem.getType(), ClassDataItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.CODE_ITEM:
                    annotators.put(mapItem.getType(), CodeItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.STRING_DATA_ITEM:
                    annotators.put(mapItem.getType(), StringDataItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.DEBUG_INFO_ITEM:
                    annotators.put(mapItem.getType(), DebugInfoItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.ANNOTATION_ITEM:
                    annotators.put(mapItem.getType(), AnnotationItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.ENCODED_ARRAY_ITEM:
                    annotators.put(mapItem.getType(), EncodedArrayItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.ANNOTATION_DIRECTORY_ITEM:
                    annotators.put(mapItem.getType(), AnnotationDirectoryItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.CALL_SITE_ID_ITEM:
                    annotators.put(mapItem.getType(), CallSiteIdItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.METHOD_HANDLE_ITEM:
                    annotators.put(mapItem.getType(), MethodHandleItem.makeAnnotator(this, mapItem));
                    break;
                case ItemType.HIDDENAPI_CLASS_DATA_ITEM:
                    annotators.put(mapItem.getType(), HiddenApiClassDataItem.makeAnnotator(this, mapItem));
                    break;
                default:
                    throw new RuntimeException(String.format("Unrecognized item type: 0x%x", mapItem.getType()));
            }
        }
    }

    public void writeAnnotations(Writer out) throws IOException {
        List<MapItem> mapItems = dexFile.getMapItems();
        // sort the map items based on the order defined by sectionAnnotationOrder
        Comparator<MapItem> comparator = new Comparator<MapItem>() {
            @Override public int compare(MapItem o1, MapItem o2) {
                return Integer.compare(sectionAnnotationOrder.get(o1.getType()), sectionAnnotationOrder.get(o2.getType()));
            }
        };

        MapItem[] mapItemsArray = mapItems.toArray(new MapItem[mapItems.size()]);
        Arrays.sort(mapItemsArray, comparator);

        try {
            // Need to annotate the debug info offset table first, to propagate the debug info identities
            if (dexFile instanceof CDexBackedDexFile) {
                moveTo(dexFile.getBaseDataOffset() + ((CDexBackedDexFile) dexFile).getDebugInfoOffsetsPos());
                CdexDebugOffsetTable.annotate(this, dexFile.getBuffer());
            }

            for (MapItem mapItem: mapItemsArray) {
                try {
                    SectionAnnotator annotator = annotators.get(mapItem.getType());
                    annotator.annotateSection(this);
                } catch (Exception ex) {
                    System.err.println(String.format("There was an error while dumping the %s section",
                            ItemType.getItemTypeName(mapItem.getType())));
                    ex.printStackTrace(System.err);
                }
            }
        } finally {
            writeAnnotations(out, dexFile.getBuffer().getBuf(), dexFile.getBuffer().getBaseOffset());
        }
    }

    @Nullable
    public SectionAnnotator getAnnotator(int itemType) {
        return annotators.get(itemType);
    }
}
