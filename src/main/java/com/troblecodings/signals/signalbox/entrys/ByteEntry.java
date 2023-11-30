package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;

public class ByteEntry extends IPathEntry<Byte> {

    private int value;

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        value = buffer.getByteToUnsignedInt();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
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