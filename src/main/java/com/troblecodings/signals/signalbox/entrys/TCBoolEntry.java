package com.troblecodings.signals.signalbox.entrys;

import java.util.function.IntConsumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.TCBoolean;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;

public class TCBoolEntry extends IPathEntry<TCBoolean>
        implements IIntegerable<TCBoolean>, IntConsumer {

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
    public TCBoolean getValue() {
        return TCBoolean.valueOf(this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final TCBoolean value) {
        this.value = value.booleanValue();
    }

    @Override
    public TCBoolean getObjFromID(final int obj) {
        return obj == 0 ? TCBoolean.valueOf(false) : TCBoolean.valueOf(true);
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
    public TCBoolean getDefaultValue() {
        return TCBoolean.FALSE;
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        value = buffer.getBoolean();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putBoolean(value);
    }

}
