package eu.gir.girsignals.signalbox.entrys;

import net.minecraft.nbt.NBTTagCompound;

public interface INetworkSavable extends ISaveable {

    /**
     * Write the values from this entry to the given network tag if and only if the
     * value did change since the last write
     * 
     * @param tag the network tag to write to
     */
    public void writeEntryNetwork(final NBTTagCompound tag);

    public void readEntryNetwork(final NBTTagCompound tag);
}
