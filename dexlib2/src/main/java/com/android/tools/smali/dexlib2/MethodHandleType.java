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

package com.android.tools.smali.dexlib2;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

import com.android.tools.smali.util.ExceptionWithContext;

import javax.annotation.Nonnull;

public class MethodHandleType {
    public static final int STATIC_PUT = 0;
    public static final int STATIC_GET = 1;
    public static final int INSTANCE_PUT = 2;
    public static final int INSTANCE_GET = 3;
    public static final int INVOKE_STATIC = 4;
    public static final int INVOKE_INSTANCE = 5;
    public static final int INVOKE_CONSTRUCTOR = 6;
    public static final int INVOKE_DIRECT = 7;
    public static final int INVOKE_INTERFACE = 8;

    private static final Map<Integer, String> methodHandleTypeNames = unmodifiableMap(Map.of(
            STATIC_PUT, "static-put",
            STATIC_GET, "static-get",
            INSTANCE_PUT, "instance-put",
            INSTANCE_GET, "instance-get",
            INVOKE_STATIC, "invoke-static",
            INVOKE_INSTANCE, "invoke-instance",
            INVOKE_CONSTRUCTOR, "invoke-constructor",
            INVOKE_DIRECT, "invoke-direct",
            INVOKE_INTERFACE, "invoke-interface"));

    private static final Map<String, Integer> inverse = getInverse();

    private static Map<String, Integer> getInverse() {
        HashMap<String, Integer> namesToTypes = new HashMap<>();
        for (Map.Entry<Integer, String> entry : methodHandleTypeNames.entrySet()) {
            namesToTypes.put(entry.getValue(), entry.getKey());
        }
        return unmodifiableMap(namesToTypes);
    }

    @Nonnull public static String toString(int methodHandleType) {
        String val = methodHandleTypeNames.get(methodHandleType);
        if (val == null) {
            throw new InvalidMethodHandleTypeException(methodHandleType);
        }
        return val;
    }

    public static int getMethodHandleType(String methodHandleType) {
        Integer ret = inverse.get(methodHandleType);
        if (ret == null) {
            throw new ExceptionWithContext("Invalid method handle type: %s", methodHandleType);
        }
        return ret;
    }

    public static class InvalidMethodHandleTypeException extends ExceptionWithContext {
        private final int methodHandleType;

        public InvalidMethodHandleTypeException(int methodHandleType) {
            super("Invalid method handle type: %d", methodHandleType);
            this.methodHandleType = methodHandleType;
        }

        public InvalidMethodHandleTypeException(int methodHandleType, String message, Object... formatArgs) {
            super(message, formatArgs);
            this.methodHandleType = methodHandleType;
        }

        public int getMethodHandleType() {
            return methodHandleType;
        }
    }
}
