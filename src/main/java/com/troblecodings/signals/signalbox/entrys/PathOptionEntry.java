package com.troblecodings.signals.signalbox.entrys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.core.interfaces.INetworkSaveable;
import com.troblecodings.core.interfaces.ISaveable;
import com.troblecodings.signals.core.NetworkBufferWrappers;
import com.troblecodings.signals.network.PathOptionEntryNetwork;

public class PathOptionEntry implements INetworkSaveable, ISaveable {

    private PathOptionEntryNetwork network = new PathOptionEntryNetwork();
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
        final IPathEntry<T> pathEntry =
                (IPathEntry<T>) pathEntrys.computeIfAbsent(type, pType -> pType.newValue());
        final T oldValue = pathEntry.getValue();
        pathEntry.setValue(value);
        if (!value.equals(oldValue)) {
            network.sendEntryAdd(type, pathEntry);
        }
    }

    public void addEntry(final PathEntryType<?> entryType, final IPathEntry<?> entry) {
        if (entry == null) {
            pathEntrys.remove(entryType);
            return;
        }
        pathEntrys.put(entryType, entry);
    }

    public void removeEntryNoNetwork(final PathEntryType<?> type) {
        pathEntrys.remove(type);
    }

    public void removeEntry(final PathEntryType<?> type) {
        pathEntrys.remove(type);
        network.sendEntryRemove(type);
    }

    public boolean containsEntry(final PathEntryType<?> type) {
        return pathEntrys.containsKey(type);
    }

    public void setUpNetwork(final PathOptionEntryNetwork network) {
        this.network = network;
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
        final List<PathEntryType<?>> tagSet =
                tag.keySet().stream().map(PathEntryType::getType).collect(Collectors.toList());
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
    public void readNetwork(final ReadBuffer buffer) {
        pathEntrys.putAll(buffer.getMapWithCombinedValueFunc(
                NetworkBufferWrappers.PATHENTRYTYPE_FUNCTION, (buf, type) -> {
                    final IPathEntry<?> entry =
                            pathEntrys.computeIfAbsent(type, _u -> type.newValue());
                    entry.readNetwork(buffer);
                    return entry;
                }));
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putMap(pathEntrys, NetworkBufferWrappers.PATHENTRYTYPE_CONSUMER,
                WriteBuffer.getINetworkSaveableConsumer());
    }
}