package com.troblecodings.signals.signalbox.entrys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.enums.EnumPathUsage;

import net.minecraft.core.BlockPos;

public final class PathEntryType<T> {

    private static final Map<String, PathEntryType<?>> NAME_TO_TYPE = new HashMap<>();
    public static final List<PathEntryType<?>> ALL_ENTRIES = new ArrayList<>();

    private final Class<? extends IPathEntry<T>> entryClass;
    private final String name;
    private final int entryID;

    private PathEntryType(final Class<? extends IPathEntry<T>> entryClass, final String name) {
        this.entryClass = entryClass;
        this.name = name;
        this.entryID = ALL_ENTRIES.size();
        NAME_TO_TYPE.put(name, this);
        ALL_ENTRIES.add(this);
    }

    public static final PathEntryType<BlockPos> SIGNAL = new PathEntryType<>(BlockposEntry.class,
            "signal");
    public static final PathEntryType<BlockPos> BLOCKING = new PathEntryType<>(BlockposEntry.class,
            "blocking");
    public static final PathEntryType<BlockPos> RESETING = new PathEntryType<>(BlockposEntry.class,
            "reseting");
    public static final PathEntryType<BlockPos> OUTPUT = new PathEntryType<>(BlockposEntry.class,
            "output");
    public static final PathEntryType<Integer> SPEED = new PathEntryType<>(IntegerEntry.class,
            "speed");

    public static final PathEntryType<String> ZS2 = new PathEntryType<>(ZS2Entry.class, "zs2");

    private static final class EnumPathUsageEntry extends EnumEntry<EnumPathUsage> {

        public EnumPathUsageEntry() {
            super(EnumPathUsage.class);
        }
    }

    public static final PathEntryType<EnumPathUsage> PATHUSAGE = new PathEntryType<>(
            EnumPathUsageEntry.class, "pathusage");

    /**
     * Creates a new value holder
     *
     * @return new IPathEntry with the given name
     */
    public IPathEntry<T> newValue() {
        try {
            @SuppressWarnings("deprecation")
            final IPathEntry<T> t = this.entryClass.newInstance();
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
    public Class<? extends IPathEntry<T>> getEntryClass() {
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

    public static PathEntryType<?> getType(final String name) {
        return NAME_TO_TYPE.get(name);
    }

    public static List<PathEntryType<?>> typeList() {
        return ImmutableList.copyOf(NAME_TO_TYPE.values());
    }

    public int getID() {
        return entryID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final PathEntryType<?> other = (PathEntryType<?>) obj;
        return Objects.equals(entryClass, other.entryClass) && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}