package eu.gir.girsignals.signalbox.entrys;

import net.minecraft.nbt.NBTTagCompound;

public abstract class IPathEntry<T> implements ISaveable {

    protected boolean isDirty = false;

    private String name = "defaultEntry";

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the value to hold
     * 
     * @return the value
     */
    public abstract T getValue();

    /**
     * Sets the value of this entry
     * 
     * @param the value to set
     */
    public abstract void setValue(T value);

    public void writeEntryNetwork(final NBTTagCompound tag) {
        if (this.isDirty) {
            this.write(tag);
        }
        this.isDirty = true;
    }
}
