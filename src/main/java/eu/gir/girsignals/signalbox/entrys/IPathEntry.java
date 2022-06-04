package eu.gir.girsignals.signalbox.entrys;

import java.util.Objects;

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

    /**
     * Write the values from this entry to the given network tag if and only if the
     * value did change since the last write
     * 
     * @param tag the network tag to write to
     */
    public void writeEntryNetwork(final NBTTagCompound tag) {
        if (this.isDirty) {
            this.write(tag);
            this.isDirty = true;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, this.getValue());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IPathEntry<?> other = (IPathEntry<?>) obj;
        return Objects.equals(name, other.name) && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public String toString() {
        return "IPathEntry [isDirty=" + isDirty + ", name=" + name + ", value=" + this.getValue()
                + "]";
    }

}
