package eu.gir.girsignals.signalbox.entrys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import eu.gir.girsignals.signalbox.EnumGuiMode;

public class PathEntryType<T extends IPathEntry<?>> {

    private static final Map<String, PathEntryType<?>> NAME_TO_TYPE = new HashMap<>();

    private final Class<T> entryClass;
    private final String name;

    private PathEntryType(final Class<T> entryClass, final String name) {
        this.entryClass = entryClass;
        this.name = name;
        NAME_TO_TYPE.put(name, this);
    }

    public static final PathEntryType<BlockposEntry> SIGNAL = new PathEntryType<>(
            BlockposEntry.class, "signal");
    public static final PathEntryType<BlockposEntry> BLOCKING = new PathEntryType<>(
            BlockposEntry.class, "blocking");
    public static final PathEntryType<BlockposEntry> RESETING = new PathEntryType<>(
            BlockposEntry.class, "reseting");
    public static final PathEntryType<BlockposEntry> OUTPUT = new PathEntryType<>(
            BlockposEntry.class, "output");
    public static final PathEntryType<IntegerEntry> SPEED = new PathEntryType<>(IntegerEntry.class,
            "speed");
    @SuppressWarnings("rawtypes")
    public static final PathEntryType<EnumEntry> PATHUSAGE = new PathEntryType<EnumEntry>(
            EnumEntry.class, "pathusage") {
        @Override
        public EnumEntry<EnumGuiMode> newValue() {
            return new EnumEntry<>(EnumGuiMode.class);
        }
    };

    /**
     * Creates a new value holder
     * 
     * @return new IPathEntry with the given name
     */
    public T newValue() {
        try {
            final T t = this.entryClass.newInstance();
            t.setName(name);
            return t;
        } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the class type of the given entry
     * 
     * @return the entryClass
     */
    public Class<T> getEntryClass() {
        return entryClass;
    }

    /**
     * Gets the type name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the option value from a path option entry of the given type
     * 
     * @param <R>   the type to return
     * @param type  the path entry type
     * @param entry the entry
     * @return the option of the entry
     */
    public static <R> Optional<R> of(final PathEntryType<IPathEntry<R>> type,
            final PathOptionEntry entry) {
        final IPathEntry<R> pathEntry = entry.getEntry(type).orElse(null);
        if (pathEntry == null)
            return Optional.empty();
        return Optional.of(pathEntry.getValue());
    }

    public static PathEntryType<?> getType(final String name) {
        return NAME_TO_TYPE.get(name);
    }

    public static List<PathEntryType<?>> typeList() {
        return ImmutableList.copyOf(NAME_TO_TYPE.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PathEntryType<?> other = (PathEntryType<?>) obj;
        return Objects.equals(entryClass, other.entryClass) && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "PathEntryType [entryClass=" + entryClass + ", name=" + name + "]";
    }

}
