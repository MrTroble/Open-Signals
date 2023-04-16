package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import net.minecraft.core.BlockPos;

public class ReadBuffer implements BufferFactory {

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

    public void putByte(final Byte b) {
    }

    public void putInt(final int i) {
    }

    public void putBlockPos(final BlockPos pos) {
    }

    public void resetBuilder() {
    }

    public ByteBuffer getBuildedBuffer() {
        return null;
    }

    public ByteBuffer build() {
        return null;
    }
}