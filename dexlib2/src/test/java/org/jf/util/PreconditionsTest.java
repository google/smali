/*
 * Copyright 2019, Google LLC
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

package org.jf.util;

import com.google.common.collect.ImmutableList;
import org.jf.dexlib2.util.Preconditions;
import org.junit.Assert;
import org.junit.Test;

public class PreconditionsTest {

  private void verifyArrayPayloadElementIsValid(int elementWidth, long value) {
    Preconditions.checkArrayPayloadElements(elementWidth, ImmutableList.of(value));
  }

  private void verifyArrayPayloadElementIsInvalid(int elementWidth, long value) {
    try {
      Preconditions.checkArrayPayloadElements(elementWidth, ImmutableList.of(value));
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // expected exception
    }
  }

  @Test
  public void checkArrayPayloadElements() {
    verifyArrayPayloadElementIsValid(8, Long.MAX_VALUE);
    verifyArrayPayloadElementIsValid(8, Long.MIN_VALUE);
    verifyArrayPayloadElementIsValid(4, Integer.MAX_VALUE);
    verifyArrayPayloadElementIsValid(4, Integer.MIN_VALUE);
    verifyArrayPayloadElementIsValid(2, Short.MAX_VALUE);
    verifyArrayPayloadElementIsValid(2, Short.MIN_VALUE);
    verifyArrayPayloadElementIsValid(2, Character.MAX_VALUE);
    verifyArrayPayloadElementIsValid(2, Character.MIN_VALUE);
    verifyArrayPayloadElementIsValid(1, Byte.MAX_VALUE);
    verifyArrayPayloadElementIsValid(1, Byte.MIN_VALUE);

    verifyArrayPayloadElementIsInvalid(4, ((long) Integer.MAX_VALUE) + 1);
    verifyArrayPayloadElementIsInvalid(4, ((long) Integer.MIN_VALUE) - 1);
    verifyArrayPayloadElementIsInvalid(2, ((long) Short.MIN_VALUE) - 1);
    //Since short and character have the same size, but different ranges
    // and cannot be distinguished here, the valid interval is
    //[Short.MIN_VALUE, Character.MAX_VALUE], i.e. [-32768, 65535]
    verifyArrayPayloadElementIsInvalid(2, ((long) Character.MAX_VALUE) + 1);
    verifyArrayPayloadElementIsInvalid(2, ((long) Short.MIN_VALUE) - 1);
    verifyArrayPayloadElementIsInvalid(1, ((long) Byte.MAX_VALUE) + 1);
    verifyArrayPayloadElementIsInvalid(1, ((long) Byte.MIN_VALUE) - 1);
  }
}
