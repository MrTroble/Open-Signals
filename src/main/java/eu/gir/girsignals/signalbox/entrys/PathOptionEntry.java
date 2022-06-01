package eu.gir.girsignals.signalbox.entrys;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PathOptionEntry {

    private final Map<PathEntryType<?>, IPathEntry<?>> pathEntrys = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends IPathEntry<?>> Optional<T> getEntry(final PathEntryType<T> type) {
        if (pathEntrys.containsKey(type))
            return Optional.empty();
        return Optional.of((T) pathEntrys.get(type));
    }

    public <T extends IPathEntry<?>> void setEntry(final PathEntryType<T> type, final T value) {
        pathEntrys.put(type, value);
    }
}
