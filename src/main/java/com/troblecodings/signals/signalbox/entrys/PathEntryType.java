package com.troblecodings.signals.signalbox.entrys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.util.math.BlockPos;

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

    public static final PathEntryType<Byte> ZS2 = new PathEntryType<>(ByteEntry.class, "zs2");

    public static final PathEntryType<Boolean> SIGNAL_REPEATER = new PathEntryType<>(
            BoolEntry.class, "signal_repeater");

    public static final PathEntryType<Integer> DELAY = new PathEntryType<>(IntegerEntry.class,
            "delay");

    public static final PathEntryType<Point> POINT = new PathEntryType<>(PointEntry.class,
            "pointentry");

    public static final PathEntryType<BlockPos> SIGNALBOX = new PathEntryType<>(BlockposEntry.class,
            "signalbox");

    public static final PathEntryType<Integer> PATHWAY_COSTS = new PathEntryType<>(
            IntegerEntry.class, "pathway_costs");

    public static final PathEntryType<Boolean> CAN_BE_OVERSTPEPPED = new PathEntryType<>(
            BoolEntry.class, "can_be_overstepped");

    public static final PathEntryType<List<PosIdentifier>> PRESIGNALS = new PathEntryType<>(
            ListBlockPosEntry.class, "listblockposentry");

    public static final PathEntryType<Point> PROTECTIONWAY_END = new PathEntryType<>(
            PointEntry.class, "protectionWayEnd");

    public static final PathEntryType<BlockPos> PROTECTIONWAY_RESET = new PathEntryType<>(
            BlockposEntry.class, "protectionway_reset");

    public static final PathEntryType<ModeIdentifier> CONNECTED_TRAINNUMBER = new PathEntryType<>(
            ModeIdentifierEntry.class, "connected_point");

    public static final PathEntryType<TrainNumber> TRAINNUMBER = new PathEntryType<>(
            TrainNumberEntry.class, "trainnumber");

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