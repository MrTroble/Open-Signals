package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;

public interface ISaveable {

    /**
     * Save the this element to a NBT Compound
     * 
     * @param tag the tag to write to
     */
    void write(NBTWrapper tag);

    /**
     * Reads this element from a given NBT Compound
     * 
     * @param tag the tag to read from
     */
    void read(NBTWrapper tag);

}
