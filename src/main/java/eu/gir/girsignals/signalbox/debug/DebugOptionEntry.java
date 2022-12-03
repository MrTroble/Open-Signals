package eu.gir.girsignals.signalbox.debug;

import java.util.Optional;

import eu.gir.girsignals.SignalsMain;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;

public class DebugOptionEntry extends PathOptionEntry {

    @Override
    public <T> void setEntry(final PathEntryType<T> type, final T value) {
        SignalsMain.log.debug(type);
        SignalsMain.log.debug(value);
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
        SignalsMain.log.debug("R:" + tag);
        super.read(tag);
    }

    @Override
    public void write(final NBTTagCompound tag) {
        super.write(tag);
        SignalsMain.log.debug("W:" + tag);
    }
}
