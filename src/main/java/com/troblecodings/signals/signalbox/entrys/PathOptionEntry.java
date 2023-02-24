package com.troblecodings.signals.signalbox.entrys;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.Observable;
import com.troblecodings.signals.core.Observer;

public class PathOptionEntry implements INetworkSavable, Observable {

    private final Map<PathEntryType<?>, IPathEntry<?>> pathEntrys = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getEntry(final PathEntryType<T> type) {
        if (!pathEntrys.containsKey(type))
            return Optional.empty();
        return Optional.of((T) pathEntrys.get(type).getValue());
    }

    @SuppressWarnings("unchecked")
    public <T> void setEntry(final PathEntryType<T> type, final T value) {
        if (value == null) {
            pathEntrys.remove(type);
            return;
        }
        final IPathEntry<T> pathEntry = (IPathEntry<T>) pathEntrys.computeIfAbsent(type,
                pType -> pType.newValue());
        pathEntry.setValue(value);
    }

    public void removeEntry(final PathEntryType<?> type) {
        pathEntrys.remove(type);
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
    }

    @Override
    public void readNetwork(final ByteBuffer buffer) {
        final int size = Byte.toUnsignedInt(buffer.get());
        for (int i = 0; i < size; i++) {
            final PathEntryType<?> type = PathEntryType.ALL_ENTRIES
                    .get(Byte.toUnsignedInt(buffer.get()));
            final IPathEntry<?> entry = type.newValue();
            entry.readNetwork(buffer);
            pathEntrys.put(type, entry);
        }
    }

    @Override
    public void writeNetwork(final ByteBuffer buffer) {
        buffer.put((byte) pathEntrys.size());
        pathEntrys.forEach((type, entry) -> {
            buffer.put((byte) type.getID());
            entry.writeNetwork(buffer);
        });
    }

    public void writePathWayUpdateToNetwork(final ByteBuffer buffer) {
        buffer.put((byte) PathEntryType.PATHUSAGE.getID());
        pathEntrys.get(PathEntryType.PATHUSAGE).writeNetwork(buffer);
    }

    public int getBufferSizeForPathWayUpdate() {
        return pathEntrys.get(PathEntryType.OUTPUT).getMinBufferSize() + 1;
    }

    public int getBufferSize() {
        final AtomicReference<Integer> size = new AtomicReference<>();
        size.set(pathEntrys.keySet().size() + 1);
        pathEntrys.values().forEach(value -> {
            size.set(size.get() + value.getMinBufferSize());
        });
        return size.get();
    }

    @Override
    public void addListener(final Observer observer) {
        pathEntrys.values().forEach(entry -> entry.addListener(observer));
    }

    @Override
    public void removeListener(final Observer observer) {
        pathEntrys.values().forEach(entry -> entry.removeListener(observer));
    }
}