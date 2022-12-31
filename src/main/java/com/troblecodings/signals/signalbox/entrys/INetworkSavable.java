package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;

public interface INetworkSavable extends ISaveable {

    /**
     * Write the values from this entry to the given network tag if and only if the
     * value did change since the last write
     * 
     * @param tag      , the network tag to write to
     * @param writeAll , whether to write all or only dirty values
     */
    public void writeEntryNetwork(final NBTWrapper tag, boolean writeAll);

    /**
     * Reads from the network to this entry
     * 
     * @param tag , the network tag to read from
     */
    public void readEntryNetwork(final NBTWrapper tag);
}
