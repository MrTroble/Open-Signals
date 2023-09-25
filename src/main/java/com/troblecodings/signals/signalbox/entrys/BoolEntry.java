package com.troblecodings.signals.signalbox.entrys;

import java.util.function.IntConsumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;

public class BoolEntry extends IPathEntry<Boolean> implements IIntegerable<Boolean>, IntConsumer {

    private boolean value = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTWrapper tag) {
        tag.putBoolean(getName(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTWrapper tag) {
        this.value = tag.getBoolean(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final Boolean value) {
        this.value = value.booleanValue();
    }

    @Override
    public Boolean getObjFromID(final int obj) {
        return obj == 0 ? false : true;
    }

    @Override
    public int count() {
        return 2;
    }

    @Override
    public void accept(final int value) {
        this.setValue(getObjFromID(value));
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        value = buffer.getByte() != 0;
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) (value ? 1 : 0));
    }
}