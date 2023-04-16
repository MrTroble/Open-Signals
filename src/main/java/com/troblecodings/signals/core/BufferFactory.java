package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import net.minecraft.core.BlockPos;

public interface BufferFactory {

    /**
     * Get and remove the next byte from ByteBuffer.
     * 
     * @return The next byte removed from the ByteBuffer.
     */
    public byte getByte();

    /**
     * Get and remove the next int from ByteBuffer.
     * 
     * @return The next int from the ByteBuffer.
     */
    public int getInt();

    /**
     * Get and remove the next byte and convert it to an int.
     * 
     * @return The next byte converted to an int.
     */

    public int getByteAsInt();

    /**
     * Gets and removes the next 3 ints and convert it in to a BlockPos.
     * 
     * @return the BlockPos represented by the 3 ints.
     */

    public BlockPos getBlockPos();

    /**
     * Puts a byte on a ByteBuffer.
     * 
     * @param b The byte to put.
     */

    public void putByte(final Byte b);

    /**
     * Puts an int on the ByteBuffer.
     * 
     * @param i The int to put.
     */

    public void putInt(final int i);

    /**
     * Takes the x, y, and z int and puts them in this order on the ByteBuffer.
     * 
     * @param pos The BlockPos to put.
     */

    public void putBlockPos(final BlockPos pos);

    /**
     * Resets the ByteBuffer.
     */
    public void resetBuilder();

    /**
     * Build and return the current ByteBuffer represented by the bytes and ints
     * added.
     * 
     * @return The builed ByteBuffer.
     */

    public ByteBuffer getBuildedBuffer();

    /**
     * Build the current ByteBuffer represented by the bytes and ints added.
     * 
     * @return The builed ByteBuffer.
     */
    public ByteBuffer build();
}