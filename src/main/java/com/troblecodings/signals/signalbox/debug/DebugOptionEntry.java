package com.troblecodings.signals.signalbox.debug;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.nbt.NBTTagCompound;

public class DebugOptionEntry extends PathOptionEntry {

    @Override
    public <T> void setEntry(final PathEntryType<T> type, final T value) {
        OpenSignalsMain.log.debug(type);
        OpenSignalsMain.log.debug(value);
        super.setEntry(type, value);
    }

    @Override
    public <T> Optional<T> getEntry(final PathEntryType<T> type) {
        final Optional<T> entry = super.getEntry(type);
        if (entry.filter(n -> n.equals(EnumPathUsage.SELECTED)).isPresent()) {
            return entry;
        }
        return entry;
    }

    @Override
    public void read(final NBTTagCompound tag) {
        OpenSignalsMain.log.debug("R:" + tag);
        super.read(tag);
    }

    @Override
    public void write(final NBTTagCompound tag) {
        super.write(tag);
        OpenSignalsMain.log.debug("W:" + tag);
    }
}
