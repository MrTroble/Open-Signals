package eu.gir.girsignals.signalbox.entrys;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;

public class PathOptionEntry implements ISaveable {

    private final Map<PathEntryType<?>, IPathEntry<?>> pathEntrys = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getEntry(final PathEntryType<T> type) {
        if (!pathEntrys.containsKey(type))
            return Optional.empty();
        return Optional.of((T) pathEntrys.get(type));
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
    public void write(final NBTTagCompound tag) {
        pathEntrys.forEach((type, option) -> {
            final NBTTagCompound entry = new NBTTagCompound();
            option.write(entry);
            tag.setTag(type.getName(), entry);
        });
    }

    @Override
    public void read(final NBTTagCompound tag) {
        tag.getKeySet().forEach(name -> {
            final PathEntryType<?> entry = PathEntryType.getType(name);
            if (entry != null) {
                final IPathEntry<?> path = entry.newValue();
                path.read(tag);
                pathEntrys.put(entry, path);
            }
        });
    }

}
