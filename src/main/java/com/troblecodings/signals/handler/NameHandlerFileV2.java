package com.troblecodings.signals.handler;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import net.minecraft.core.BlockPos;

public class NameHandlerFileV2 extends SignalStateFileV2 {

    public NameHandlerFileV2(final Path path) {
        super(path);
    }

    public synchronized SignalStatePosV2 createState(final BlockPos pos, final String name) {
        if (name.length() > 128)
            throw new IllegalArgumentException("Max Name length is 128!");
        try {
            return create(pos, name.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException e) {
        }
        return null;
    }

    public synchronized void writeString(final SignalStatePosV2 pos, final String name) {
        if (name.length() > 128)
            throw new IllegalArgumentException("Max Name length is 128!");
        try {
            write(pos, ByteBuffer.allocate(STATE_BLOCK_SIZE).put(name.getBytes("UTF-8")));
        } catch (final UnsupportedEncodingException e) {
        }
    }

    public synchronized String getString(final BlockPos pos) {
        return getString(this.find(pos));
    }

    public synchronized String getString(final SignalStatePosV2 pos) {
        if (pos == null)
            return "";
        try {
            return new String(read(pos).array(), "UTF-8").trim();
        } catch (final UnsupportedEncodingException e) {
        }
        return "";
    }

}
