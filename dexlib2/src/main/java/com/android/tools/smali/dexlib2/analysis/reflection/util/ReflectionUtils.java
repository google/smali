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

package com.android.tools.smali.dexlib2.analysis.reflection.util;

import static java.util.Collections.unmodifiableMap;

import java.util.Map;
import java.util.HashMap;

public class ReflectionUtils {

    private static Map<String, String> primitiveMap;
    
    static {
        Map<String, String> temp = new HashMap<>();
            temp.put("boolean", "Z");
            temp.put("int", "I");
            temp.put("long", "J");
            temp.put("double", "D");
            temp.put("void", "V");
            temp.put("float", "F");
            temp.put("char", "C");
            temp.put("short", "S");
            temp.put("byte", "B");
            primitiveMap = unmodifiableMap(temp);
    }

    private static Map<String, String> primitiveMapInverse = getInverse();

    private static Map<String, String> getInverse() {
        Map<String, String> temp = new HashMap<>();
        for (Map.Entry<String, String> entry : primitiveMap.entrySet()) {
            temp.put(entry.getValue(), entry.getKey());
        }
        return unmodifiableMap(temp);
    }


    public static String javaToDexName(String javaName) {
        if (javaName.charAt(0) == '[') {
            return javaName.replace('.', '/');
        }

        if (primitiveMap.containsKey(javaName)) {
            return primitiveMap.get(javaName);
        }

        return 'L' + javaName.replace('.', '/') + ';';
    }

    public static String dexToJavaName(String dexName) {
        if (dexName.charAt(0) == '[') {
            return dexName.replace('/', '.');
        }

        if (primitiveMapInverse.containsKey(dexName)) {
            return primitiveMapInverse.get(dexName);
        }

        return dexName.replace('/', '.').substring(1, dexName.length()-1);
    }
}
