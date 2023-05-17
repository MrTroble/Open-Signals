package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import net.minecraft.util.math.BlockPos;

public class ReadBuffer {

    private final ByteBuffer readBuffer;

    public ReadBuffer(final ByteBuffer buffer) {
        this.readBuffer = buffer;
    }

    public byte getByte() {
        return readBuffer.get();
    }

    public int getInt() {
        return readBuffer.getInt();
    }

    public int getByteAsInt() {
        return Byte.toUnsignedInt(readBuffer.get());
    }

    public BlockPos getBlockPos() {
        return new BlockPos(readBuffer.getInt(), readBuffer.getInt(), readBuffer.getInt());
    }
}