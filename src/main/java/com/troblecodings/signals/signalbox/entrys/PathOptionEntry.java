package com.troblecodings.signals.signalbox.entrys;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.Observable;
import com.troblecodings.signals.core.Observer;

public class PathOptionEntry implements INetworkSavable, Observable {

    private final Map<PathEntryType<?>, IPathEntry<?>> pathEntrys = new HashMap<>();
    private final Map<PathEntryType<?>, IPathEntry<?>> removedEntrys = new HashMap<>(2);

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getEntry(final PathEntryType<T> type) {
        if (!pathEntrys.containsKey(type))
            return Optional.empty();
        return Optional.of((T) pathEntrys.get(type).getValue());
    }

    @SuppressWarnings("unchecked")
    public <T> void setEntry(final PathEntryType<T> type, final T value) {
        if (value == null) {
            removedEntrys.put(type, pathEntrys.remove(type));
            return;
        } else if (removedEntrys.containsKey(type)) {
            removedEntrys.remove(type);
        }
        final IPathEntry<T> pathEntry = (IPathEntry<T>) pathEntrys.computeIfAbsent(type,
                pType -> pType.newValue());
        pathEntry.setValue(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathEntrys);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final PathOptionEntry other = (PathOptionEntry) obj;
        return Objects.equals(pathEntrys, other.pathEntrys);
    }

    @Override
    public String toString() {
        return "PathOptionEntry [pathEntrys=" + pathEntrys + "]";
    }

    @Override
    public void write(final NBTWrapper tag) {
        pathEntrys.forEach((type, option) -> {
            final NBTWrapper entry = new NBTWrapper();
            option.write(entry);
            tag.putWrapper(type.getName(), entry);
        });
    }

    @Override
    public void read(final NBTWrapper tag) {
        // TODO new sync system
    }

    @Override
    public void readNetwork(final ByteBuffer buffer) {
    }

    @Override
    public void writeNetwork(final ByteBuffer buffer) {
        pathEntrys.forEach((type, entry) -> {
            entry.writeNetwork(buffer);
        });
        removedEntrys.forEach((type, entry) -> {
            entry.writeNetwork(buffer);
        });
    }

    @Override
    public void addListener(final Observer observer) {
        pathEntrys.values().forEach(entry -> entry.addListener(observer));
        removedEntrys.values().forEach(entry -> entry.addListener(observer));
    }

    @Override
    public void removeListener(final Observer observer) {
        pathEntrys.values().forEach(entry -> entry.removeListener(observer));
        removedEntrys.values().forEach(entry -> entry.removeListener(observer));
    }

}
