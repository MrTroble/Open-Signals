package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.BufferFactory;

public class ByteEntry extends IPathEntry<Byte> {

    private int value;

    @Override
    public void readNetwork(final BufferFactory buffer) {
        value = buffer.getByteAsInt();
    }

    @Override
    public void writeNetwork(final BufferFactory buffer) {
        buffer.putByte((byte) value);
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putInteger(getName(), value);
    }

    @Override
    public void read(final NBTWrapper tag) {
        value = tag.getInteger(getName());
    }

    @Override
    public Byte getValue() {
        return (byte) value;
    }

    @Override
    public void setValue(final Byte value) {
        this.value = value;
    }
}