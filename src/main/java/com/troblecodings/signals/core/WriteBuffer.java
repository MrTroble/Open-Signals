package com.troblecodings.signals.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;

public class WriteBuffer {

    private final List<Byte> allBytes;
    private ByteBuffer buildedBuffer;

    public WriteBuffer() {
        this.allBytes = new ArrayList<>();
    }

    public void putByte(final Byte b) {
        allBytes.add(b);
    }

    public void putInt(final int i) {
        for (final Byte b : ByteBuffer.allocate(4).putInt(i).array()) {
            putByte(b);
        }
    }

    public void putBlockPos(final BlockPos pos) {
        for (final Byte b : ByteBuffer.allocate(4).putInt(pos.getX()).putInt(pos.getY())
                .putInt(pos.getZ()).array()) {
            putByte(b);
        }
    }

    public void resetBuilder() {
        allBytes.clear();
    }

    public ByteBuffer getBuildedBuffer() {
        if (buildedBuffer == null)
            return build();
        return buildedBuffer;
    }

    public ByteBuffer build() {
        buildedBuffer = ByteBuffer.allocate(allBytes.size());
        allBytes.forEach(b -> buildedBuffer.put(b));
        resetBuilder();
        return buildedBuffer;
    }

}
