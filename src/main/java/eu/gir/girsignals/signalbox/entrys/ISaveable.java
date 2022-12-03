package eu.gir.girsignals.signalbox.entrys;

import net.minecraft.nbt.NBTTagCompound;

public interface ISaveable {

    /**
     * Save the this element to a NBT Compound
     * 
     * @param tag the tag to write to
     */
    void write(NBTTagCompound tag);

    /**
     * Reads this element from a given NBT Compound
     * 
     * @param tag the tag to read from
     */
    void read(NBTTagCompound tag);

}
