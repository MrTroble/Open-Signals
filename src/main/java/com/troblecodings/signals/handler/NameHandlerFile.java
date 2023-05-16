package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import net.minecraft.core.BlockPos;

public class NameHandlerFile extends SignalStateFile {

    public NameHandlerFile(final Path path) {
        super(path);
    }

    public synchronized SignalStatePos createState(final BlockPos pos, final String name) {
        if (name.length() > 128)
            throw new IllegalArgumentException("Max Name length is 128!");
        return create(pos, name.getBytes());
    }

    public synchronized void writeString(final SignalStatePos pos, final String name) {
        if (name.length() > 128)
            throw new IllegalArgumentException("Max Name length is 128!");
        write(pos, ByteBuffer.allocate(STATE_BLOCK_SIZE).put(name.getBytes()));
    }

    public synchronized String getString(final BlockPos pos) {
        return getString(this.find(pos));
    }

    public synchronized String getString(final SignalStatePos pos) {
        return new String(read(pos).array()).trim();
    }
}