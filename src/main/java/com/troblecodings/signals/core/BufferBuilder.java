package com.troblecodings.signals.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BufferBuilder {

    private final List<Byte> allBytes;
    private ByteBuffer readyBuffer;

    public BufferBuilder() {
        allBytes = new ArrayList<>();
    }

    public void putByte(final Byte b) {
        allBytes.add(b);
    }

    public void putInt(final int i) {
        for (final Byte b : ByteBuffer.allocate(4).putInt(i).array()) {
            allBytes.add(b);
        }
    }

    public void resetBuilder() {
        allBytes.clear();
    }

    public ByteBuffer getReadyBuffer() {
        if (readyBuffer == null)
            return build();
        return readyBuffer;
    }

    public ByteBuffer build() {
        readyBuffer = ByteBuffer.allocate(allBytes.size());
        allBytes.forEach(b -> readyBuffer.put(b));
        resetBuilder();
        return readyBuffer;
    }
}