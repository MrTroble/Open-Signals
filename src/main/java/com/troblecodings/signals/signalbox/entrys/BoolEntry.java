package com.troblecodings.signals.signalbox.entrys;

import java.util.function.IntConsumer;

import com.troblecodings.guilib.ecs.interfaces.IIntegerable;

import net.minecraft.nbt.CompoundTag;

public class BoolEntry extends IPathEntry<Boolean> implements IIntegerable<Boolean>, IntConsumer {

    private boolean value = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final CompoundTag tag) {
        tag.putBoolean(getName(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final CompoundTag tag) {
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
        this.isDirty = true;
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

}
