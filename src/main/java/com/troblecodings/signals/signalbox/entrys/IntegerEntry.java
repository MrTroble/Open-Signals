package com.troblecodings.signals.signalbox.entrys;

import java.util.function.IntConsumer;

import net.minecraft.nbt.NBTTagCompound;

public class IntegerEntry extends IPathEntry<Integer> implements IntConsumer {

    private int value = -1;

    @Override
    public void write(final NBTTagCompound tag) {
        tag.setInteger(getName(), value);
    }

    @Override
    public void read(final NBTTagCompound tag) {
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

}
