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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.Test;

public class DelayedOutputStreamTest {

    private final int[] CONTENT_LENGTHS = new int[] { 0, 1, 1023, 1024, 1025, 2048, 2049, 40000, 65536, 65537 };

    @Test
    public void writeByte() throws Exception {
        IntStream.of(CONTENT_LENGTHS).forEach(cl -> writeByte(cl, 1024));
    }

    private void writeByte(final int contentLength, final int limit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] original = new byte[contentLength];
        ThreadLocalRandom.current().nextBytes(original);

        try {
            TriggeredSupplier<OutputStream> streamSupplier = new TriggeredSupplier<>(() -> baos);
            DelayedOutputStream dos = new DelayedOutputStream(limit, streamSupplier);
            for (int i = 0; i < contentLength; i++) {
                dos.write(original[i]);
            }
            dos.close();
            byte[] captured;
            if (contentLength > limit) {
                captured = baos.toByteArray();
                assertThat(dos.isInMemory(), is(false));
                assertThat(streamSupplier.isTriggered(), is(true));
            } else {
                captured = dos.getInMemoryBytes();
                assertThat(dos.isInMemory(), is(true));
                assertThat(streamSupplier.isTriggered(), is(false));
            }
            assertThat(captured, is(original));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void writeByteArr() throws Exception {
        IntStream.of(CONTENT_LENGTHS).forEach(cl -> writeByteArr(cl, 1024));
    }

    private void writeByteArr(final int contentLength, final int limit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] original = new byte[contentLength];
        ThreadLocalRandom.current().nextBytes(original);
        TriggeredSupplier<OutputStream> streamSupplier = new TriggeredSupplier<>(() -> baos);
        DelayedOutputStream dos = new DelayedOutputStream(limit, streamSupplier);
        byte[] buf = new byte[128];
        try {
            for (int i = 0; i < contentLength; i += buf.length) {
                int len = Math.min(contentLength - i, buf.length);
                System.arraycopy(original, i, buf, 0, len);
                dos.write(buf, 0, len);
            }
            dos.close();
            byte[] captured;
            if (contentLength > limit) {
                captured = baos.toByteArray();
                assertThat(dos.isInMemory(), is(false));
                assertThat(streamSupplier.isTriggered(), is(true));
            } else {
                captured = dos.getInMemoryBytes();
                assertThat(dos.isInMemory(), is(true));
                assertThat(streamSupplier.isTriggered(), is(false));
            }
            assertThat(captured, is(original));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class TriggeredSupplier<T> implements Supplier<T> {
        private final Supplier<T> supplier;

        private T value;

        public TriggeredSupplier(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public boolean isTriggered() {
            return value != null;
        }

        @Override
        public T get() {
            if (value == null) {
                value = supplier.get();
            }
            return value;
        }
    }
}
