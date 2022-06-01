package eu.gir.girsignals.signalbox.entrys;

import net.minecraft.nbt.NBTTagCompound;

public class BoolEntry extends IPathEntry<Boolean> {

    private boolean value = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTTagCompound tag) {
        tag.setBoolean(getName(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTTagCompound tag) {
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

}
