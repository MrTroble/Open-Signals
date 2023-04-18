package com.troblecodings.signals.signalbox.entrys;

import java.util.function.IntConsumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.WriteBuffer;

public class EnumEntry<T extends Enum<T>> extends IPathEntry<T>
        implements IIntegerable<T>, IntConsumer {

    private T enumValue;
    private final Class<T> enumClass;

    public EnumEntry(final Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTWrapper tag) {
        tag.putString(getName(), this.enumValue.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTWrapper tag) {
        this.enumValue = Enum.valueOf(this.enumClass, tag.getString(getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue() {
        return this.enumValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final T value) {
        this.enumValue = value;
    }

    @Override
    public T getObjFromID(final int obj) {
        return enumClass.getEnumConstants()[obj];
    }

    @Override
    public int count() {
        return enumClass.getEnumConstants().length;
    }

    @Override
    public void accept(final int value) {
        setValue(getObjFromID(value));
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        this.enumValue = getObjFromID(buffer.getInt());

    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putInt(this.enumValue.ordinal());
    }
}