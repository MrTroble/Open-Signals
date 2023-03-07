package com.troblecodings.signals.signalbox.entrys;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.BufferBuilder;

public class PathOptionEntry implements INetworkSavable {

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

    public boolean containsEntry(final PathEntryType<?> type) {
        return pathEntrys.containsKey(type);
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
        final List<PathEntryType<?>> tagSet = tag.keySet().stream().map(PathEntryType::getType)
                .collect(Collectors.toList());
        tagSet.forEach(entry -> {
            if (entry != null) {
                if (tag.contains(entry.getName())) {
                    final IPathEntry<?> path = entry.newValue();
                    path.read(tag.getWrapper(entry.getName()));
                    pathEntrys.put(entry, path);
                } else {
                    pathEntrys.remove(entry);
                }
            }
        });
    }

    @Override
    public void readNetwork(final ByteBuffer buffer) {
        final int size = Byte.toUnsignedInt(buffer.get());
        for (int i = 0; i < size; i++) {
            final PathEntryType<?> type = PathEntryType.ALL_ENTRIES
                    .get(Byte.toUnsignedInt(buffer.get()));
            final IPathEntry<?> entry = pathEntrys.computeIfAbsent(type, _u -> type.newValue());
            entry.readNetwork(buffer);
            pathEntrys.put(type, entry);
        }
    }

    public void writeToBuffer(final BufferBuilder buffer) {
        buffer.putByte((byte) pathEntrys.size());
        pathEntrys.forEach((type, entry) -> {
            buffer.putByte((byte) type.getID());
            entry.writeToBuffer(buffer);
        });
    }

    @Override
    public void writeNetwork(final ByteBuffer buffer) {
        buffer.put((byte) pathEntrys.size());
        pathEntrys.forEach((type, entry) -> {
            buffer.put((byte) type.getID());
            entry.writeNetwork(buffer);
        });
    }

    public void writeUpdateBuffer(final BufferBuilder builder) {
        builder.putByte((byte) 1);
        builder.putByte((byte) PathEntryType.PATHUSAGE.getID());
        pathEntrys.get(PathEntryType.PATHUSAGE).writeToBuffer(builder);
    }
}