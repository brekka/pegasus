/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Buffers a fixed number of written bytes in memory before switching to a different OutputStream provided by the
 * caller, if that limit is exceeded. For scenarios where a different strategy can be used if the content is small.
 *
 * This class is NOT thread safe. Only use with a single thread.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DelayedOutputStream extends OutputStream {

    private final Supplier<OutputStream> largeStreamSupplier;

    private ByteBuffer buffer;
    private OutputStream largeStream;
    private boolean closed;

    /**
     * @param largeStreamSupplier
     *            supply the {@link OutputStream} to use if the amount of content written to this stream exceeds the
     *            in-memory byte limit
     * @param byteLimit
     *            the maximum number of content bytes that can be buffered in memory before switching to the supplied
     *            stream.
     * @throws IllegalArgumentException
     *             if byte limit is not a positive non-zero integer or stream supplier not set
     */
    public DelayedOutputStream(final int byteLimit, final Supplier<OutputStream> largeStreamSupplier) {
        if (byteLimit < 1) {
            throw new IllegalArgumentException("byte limit must be a positive integer greater than zero");
        }
        Objects.requireNonNull(largeStreamSupplier, "large stream supplier must be set");
        this.buffer = ByteBuffer.allocate(byteLimit);
        this.largeStreamSupplier = largeStreamSupplier;
    }

    @Override
    public void write(final int b) throws IOException {
        if (buffer != null && buffer.remaining() < 1) {
            activateLarge();
        }
        if (largeStream == null) {
            buffer.put((byte) b);
        } else {
            largeStream.write(b);
        }
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (buffer != null && buffer.remaining() < len) {
            activateLarge();
        }
        if (largeStream == null) {
            buffer.put(b, off, len);
        } else {
            largeStream.write(b, off, len);
        }
    }

    @Override
    public void flush() throws IOException {
        if (largeStream != null)  {
            largeStream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        if (largeStream != null)  {
            largeStream.close();
        }
        closed = true;
    }

    /**
     * Did the content fit in memory? Must only be called once the stream is closed.
     *
     * @return true if the content is in memory and no 'large' stream was allocated.
     * @throws IllegalStateException
     *             if the stream is not yet closed
     */
    public boolean isInMemory() {
        if (!closed) {
            throw new IllegalStateException(
                    "Cannot determine whether contents are only in memory, stream is not yet closed");
        }
        return buffer != null;
    }

    /**
     * Return the content help in memory. Only call this method if {@link #isInMemory()} returns true.
     *
     * @return the content held in memory.
     * @throws IllegalStateException
     *             if the data is not held in memory or the stream is not yet closed
     */
    public byte[] getInMemoryBytes() {
        if (!isInMemory()) {
            throw new IllegalStateException("Content exceeded limit, see supplied OutputStream");
        }
        buffer.flip();
        byte[] allocated = new byte[buffer.limit()];
        System.arraycopy(buffer.array(), 0, allocated, 0, buffer.limit());
        return allocated;
    }

    /**
     * Flip this stream over to the supplied stream, writing the contents of the buffer
     */
    private void activateLarge() throws IOException {
        largeStream = largeStreamSupplier.get();
        buffer.flip();
        largeStream.write(buffer.array(), 0, buffer.limit());
        buffer = null;
    }
}
