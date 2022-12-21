package com.troblecodings.signals.signalbox.entrys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;

public class PathOptionEntry implements INetworkSavable {

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
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PathOptionEntry other = (PathOptionEntry) obj;
        return Objects.equals(pathEntrys, other.pathEntrys);
    }

    @Override
    public String toString() {
        return "PathOptionEntry [pathEntrys=" + pathEntrys + "]";
    }

    @Override
    public void write(final CompoundTag tag) {
        pathEntrys.forEach((type, option) -> {
            final CompoundTag entry = new CompoundTag();
            option.write(entry);
            tag.put(type.getName(), entry);
        });
    }

    @Override
    public void read(final CompoundTag tag) {
        final List<PathEntryType<?>> tagSet = tag.getKeySet().stream().map(PathEntryType::getType)
                .collect(Collectors.toList());
        tagSet.forEach(entry -> {
            if (entry != null) {
                if (tag.hasKey(entry.getName(), 10)) {
                    final IPathEntry<?> path = entry.newValue();
                    path.read(tag.getCompound(entry.getName()));
                    pathEntrys.put(entry, path);
                } else {
                    pathEntrys.remove(entry);
                }
            }
        });
    }

    @Override
    public void writeEntryNetwork(final CompoundTag tag, final boolean writeAll) {
        pathEntrys.forEach((type, option) -> {
            final CompoundTag entry = new CompoundTag();
            option.writeEntryNetwork(entry, writeAll);
            if (entry.size() > 0)
                tag.put(type.getName(), entry);
        });
        removedEntrys.keySet().removeIf(type -> {
            tag.putBoolean(type.getName(), false);
            return true;
        });
    }

    @Override
    public void readEntryNetwork(final CompoundTag tag) {
        this.read(tag);
    }

}
