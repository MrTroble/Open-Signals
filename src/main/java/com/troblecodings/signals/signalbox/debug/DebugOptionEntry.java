package com.troblecodings.signals.signalbox.debug;

import java.util.Optional;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

public class DebugOptionEntry extends PathOptionEntry {

    @Override
    public <T> void setEntry(final PathEntryType<T> type, final T value) {
        OpenSignalsMain.getLogger().debug(type);
        OpenSignalsMain.getLogger().debug(value);
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
    public void read(final NBTWrapper tag) {
        OpenSignalsMain.getLogger().debug("R:" + tag);
        super.read(tag);
    }

    @Override
    public void write(final NBTWrapper tag) {
        super.write(tag);
        OpenSignalsMain.getLogger().debug("W:" + tag);
    }
}