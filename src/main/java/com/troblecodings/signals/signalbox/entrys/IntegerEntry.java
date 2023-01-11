package com.troblecodings.signals.signalbox.entrys;

import java.nio.ByteBuffer;
import java.util.function.IntConsumer;

import com.troblecodings.core.NBTWrapper;

public class IntegerEntry extends IPathEntry<Integer> implements IntConsumer {

    private int value = -1;

    @Override
    public void write(final NBTWrapper tag) {
        tag.putInteger(getName(), value);
    }

    @Override
    public void read(final NBTWrapper tag) {
        this.value = tag.getInteger(getName());
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(final Integer value) {
        this.value = value;
        this.isDirty = true;
    }

    @Override
    public void accept(final int value) {
        this.setValue(value);
    }
    
    @Override
    public void readNetwork(ByteBuffer buffer) {
        buffer.putInt(value);
    }
    
    @Override
    public void writeNetwork(ByteBuffer buffer) {
        this.value = buffer.getInt();
    }

}
