package eu.gir.girsignals.signalbox.entrys;

import net.minecraft.nbt.NBTTagCompound;

public class EnumEntry<T extends Enum<T>> extends IPathEntry<T> {

    private T enumValue;
    private final Class<T> enumClass;

    public EnumEntry(final Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTTagCompound tag) {
        tag.setString(getName(), this.enumValue.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTTagCompound tag) {
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
        this.isDirty = true;
    }

}
