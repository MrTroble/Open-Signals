package com.troblecodings.signals.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;

public class BufferFactory {

    private List<Byte> allBytes;
    private ByteBuffer buildedBuffer;
    private ByteBuffer readBuffer;

    /**
     * This constructor is used for write to a Buffer
     */

    public BufferFactory() {
        allBytes = new ArrayList<>();
    }

    /**
     * This constructor is used to read from a Buffer
     * 
     * @param buffer - The ByteBuffer to read from
     */

    public BufferFactory(final ByteBuffer buffer) {
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
        allBytes.add(b);
    }

    public void putInt(final int i) {
        for (final Byte b : ByteBuffer.allocate(4).putInt(i).array()) {
            putByte(b);
        }
    }

    public void putBlockPos(final BlockPos pos) {
        putInt(pos.getX());
        putInt(pos.getY());
        putInt(pos.getZ());
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