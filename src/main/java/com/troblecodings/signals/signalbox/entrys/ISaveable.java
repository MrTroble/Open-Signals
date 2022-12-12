package com.troblecodings.signals.signalbox.entrys;

import net.minecraft.nbt.CompoundTag;

public interface ISaveable {

    /**
     * Save the this element to a NBT Compound
     * 
     * @param tag the tag to write to
     */
    void write(CompoundTag tag);

    /**
     * Reads this element from a given NBT Compound
     * 
     * @param tag the tag to read from
     */
    void read(CompoundTag tag);

}
