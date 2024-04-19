
/*
 * Copyright 2024, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google LLC nor the names of its
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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import javax.annotation.Nonnull;

/**
 * Utility methods for working with {@link InputStream}. Based on guava ByteStreams.
 */
public final class InputStreamUtil {

    private static final int BUFFER_SIZE = 8192;

    /** Max array length on JVM. */
    private static final int MAX_ARRAY_LEN = Integer.MAX_VALUE - 8;

    /** Large enough to never need to expand, given the geometric progression of buffer sizes. */
    private static final int TO_BYTE_ARRAY_DEQUE_SIZE = 20;

    /**
     * Reads all bytes from an input stream into a byte array. Does not close the stream.
     *
     * @param in the input stream to read from
     * @return a byte array containing all the bytes from the stream
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        int totalLen = 0;
        ArrayDeque<byte[]> bufs = new ArrayDeque<byte[]>(TO_BYTE_ARRAY_DEQUE_SIZE);

        // Roughly size to match what has been read already. Some file systems, such as procfs,
        // return 0
        // as their length. These files are very small, so it's wasteful to allocate an 8KB buffer.
        int initialBufferSize = min(BUFFER_SIZE, max(128, Integer.highestOneBit(totalLen) * 2));
        // Starting with an 8k buffer, double the size of each successive buffer. Smaller buffers
        // quadruple in size until they reach 8k, to minimize the number of small reads for longer
        // streams. Buffers are retained in a deque so that there's no copying between buffers while
        // reading and so all of the bytes in each new allocated buffer are available for reading
        // from
        // the stream.
        for (int bufSize = initialBufferSize; totalLen < MAX_ARRAY_LEN; bufSize = saturatedMultiply(
                bufSize, bufSize < 4096 ? 4 : 2)) {
            byte[] buf = new byte[min(bufSize, MAX_ARRAY_LEN - totalLen)];
            bufs.add(buf);
            int off = 0;
            while (off < buf.length) {
                // always OK to fill buf; its size plus the rest of bufs is never more than
                // MAX_ARRAY_LEN
                int r = in.read(buf, off, buf.length - off);
                if (r == -1) {
                    return combineBuffers(bufs, totalLen);
                }
                off += r;
                totalLen += r;
            }
        }

        // read MAX_ARRAY_LEN bytes without seeing end of stream
        if (in.read() == -1) {
            // oh, there's the end of the stream
            return combineBuffers(bufs, MAX_ARRAY_LEN);
        } else {
            throw new OutOfMemoryError("input is too large to fit in a byte array");
        }
    }

    private static int saturatedMultiply(int a, int b) {
        long value = (long) a * b;
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private static byte[] combineBuffers(Queue<byte[]> bufs, int totalLen) {
        if (bufs.isEmpty()) {
            return new byte[0];
        }
        byte[] result = bufs.remove();
        if (result.length == totalLen) {
            return result;
        }
        int remaining = totalLen - result.length;
        result = Arrays.copyOf(result, totalLen);
        while (remaining > 0) {
            byte[] buf = bufs.remove();
            int bytesToCopy = min(remaining, buf.length);
            int resultOffset = totalLen - remaining;
            System.arraycopy(buf, 0, result, resultOffset, bytesToCopy);
            remaining -= bytesToCopy;
        }
        return result;
    }

    /**
     * Discards {@code n} bytes of data from the input stream. This method will block until the full
     * amount has been skipped. Does not close the stream.
     *
     * @param in the input stream to read from
     * @param n the number of bytes to skip
     * @throws EOFException if this stream reaches the end before skipping all the bytes
     * @throws IOException if an I/O error occurs, or the stream does not support skipping
     */
    public static void skipFully(InputStream in, long n) throws IOException {
        long skipped = skipUpTo(in, n);
        if (skipped < n) {
            throw new EOFException(
                    "reached end of stream after skipping " + skipped + " bytes; " + n
                            + " bytes expected");
        }
    }

    /**
     * Discards up to {@code n} bytes of data from the input stream. This method will block until
     * either the full amount has been skipped or until the end of the stream is reached, whichever
     * happens first. Returns the total number of bytes skipped.
     */
    static long skipUpTo(InputStream in, long n) throws IOException {
        long totalSkipped = 0;
        // A buffer is allocated if skipSafely does not skip any bytes.
        byte[] buf = null;

        while (totalSkipped < n) {
            long remaining = n - totalSkipped;
            long skipped = skipSafely(in, remaining);

            if (skipped == 0) {
                // Do a buffered read since skipSafely could return 0 repeatedly, for example if
                // in.available() always returns 0 (the default).
                int skip = (int) Math.min(remaining, BUFFER_SIZE);
                if (buf == null) {
                    // Allocate a buffer bounded by the maximum size that can be requested, for
                    // example an array of BUFFER_SIZE is unnecessary when the value of remaining
                    // is smaller.
                    buf = new byte[skip];
                }
                if ((skipped = in.read(buf, 0, skip)) == -1) {
                    // Reached EOF
                    break;
                }
            }

            totalSkipped += skipped;
        }

        return totalSkipped;
    }

    /**
     * Attempts to skip up to {@code n} bytes from the given input stream, but not more than {@code
     * in.available()} bytes. This prevents {@code FileInputStream} from skipping more bytes than
     * actually remain in the file, something that it {@linkplain java.io.FileInputStream#skip(long)
     * specifies} it can do in its Javadoc despite the fact that it is violating the contract of
     * {@code InputStream.skip()}.
     */
    private static long skipSafely(InputStream in, long n) throws IOException {
        int available = in.available();
        return available == 0 ? 0 : in.skip(Math.min(available, n));
    }

    /**
     * Attempts to read enough bytes from the stream to fill the given byte array, with the same
     * behavior as {@link DataInput#readFully(byte[])}. Does not close the stream.
     *
     * @param in the input stream to read from.
     * @param b the buffer into which the data is read.
     * @throws EOFException if this stream reaches the end before reading all the bytes.
     * @throws IOException if an I/O error occurs.
     */
    public static void readFully(@Nonnull
    InputStream in, @Nonnull
    byte[] b) throws IOException {
        int read = read(in, b, 0, b.length);
        if (read != b.length) {
            throw new EOFException(
                    "reached end of stream after reading " + read + " bytes; " + b.length
                            + " bytes expected");
        }
    }

    /**
     * Reads some bytes from an input stream and stores them into the buffer array {@code b}. This
     * method blocks until {@code len} bytes of input data have been read into the array, or end of
     * file is detected. The number of bytes read is returned, possibly zero. Does not close the
     * stream.
     * <p>
     * A caller can detect EOF if the number of bytes read is less than {@code len}. All subsequent
     * calls on the same stream will return zero.
     * <p>
     * If {@code b} is null, a {@code NullPointerException} is thrown. If {@code off} is negative,
     * or {@code len} is negative, or {@code off+len} is greater than the length of the array {@code
     * b}, then an {@code IndexOutOfBoundsException} is thrown. If {@code len} is zero, then no
     * bytes are read. Otherwise, the first byte read is stored into element {@code b[off]}, the
     * next one into {@code b[off+1]}, and so on. The number of bytes read is, at most, equal to
     * {@code len}.
     *
     * @param in the input stream to read from
     * @param b the buffer into which the data is read
     * @param off an int specifying the offset into the data
     * @param len an int specifying the number of bytes to read
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     * @throws IndexOutOfBoundsException if {@code off} is negative, if {@code len} is negative, or
     *             if {@code off + len} is greater than {@code b.length}
     */
    public static int read(@Nonnull
    InputStream in, @Nonnull
    byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("trying to read invalid offset/length range");
        }

        int total = 0;
        while (total < len) {
            int result = in.read(b, off + total, len - total);
            if (result == -1) {
                break;
            }
            total += result;
        }
        return total;
    }

    /**
     * Copies all bytes from the input stream to the output stream. Does not close or flush either
     * stream.
     *
     * @param from the input stream to read from
     * @param to the output stream to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream from, OutputStream to) throws IOException {
        if (from == null || to == null) {
            throw new NullPointerException();
        }
        byte[] buf = new byte[BUFFER_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }
}
