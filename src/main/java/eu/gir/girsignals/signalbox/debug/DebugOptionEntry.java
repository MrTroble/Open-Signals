package eu.gir.girsignals.signalbox.debug;

import java.util.Optional;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;

public class DebugOptionEntry extends PathOptionEntry {

    @Override
    public <T> void setEntry(final PathEntryType<T> type, final T value) {
        GirsignalsMain.log.debug(type);
        GirsignalsMain.log.debug(value);
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

}
