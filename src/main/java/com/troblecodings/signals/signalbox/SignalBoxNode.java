package com.troblecodings.signals.signalbox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;

public class SignalBoxNode implements INetworkSavable, Iterable<ModeSet> {

    public static final Set<EnumGuiMode> VALID_MODES = ImmutableSet.of(EnumGuiMode.HP,
            EnumGuiMode.RS, EnumGuiMode.RA10, EnumGuiMode.END);

    private final HashMap<Path, ModeSet> possibleConnections = new HashMap<>();
    private final HashMap<ModeSet, PathOptionEntry> possibleModes = new HashMap<>();
    private final Point point;
    private String identifier;

    public SignalBoxNode() {
        this(new Point());
    }

    public SignalBoxNode(final Point point) {
        this.point = Objects.requireNonNull(point);
        this.identifier = point.getX() + "." + point.getY();
    }

    public void add(final ModeSet modeSet) {
        possibleModes.put(modeSet, SignalBoxFactory.getFactory().getEntry());
    }

    public boolean has(final ModeSet modeSet) {
        return possibleModes.containsKey(modeSet);
    }

    public void remove(final ModeSet modeSet) {
        possibleModes.remove(modeSet);
    }

    public void post() {
        possibleConnections.clear();
        possibleModes.forEach((e, i) -> {
            final Point p1 = new Point(this.point);
            final Point p2 = new Point(this.point);
            switch (e.mode) {
                case CORNER:
                    switch (e.rotation) {
                        case NONE:
                            p1.translate(0, 1);
                            p2.translate(-1, 0);
                            break;
                        case CLOCKWISE_90:
                            p1.translate(0, -1);
                            p2.translate(-1, 0);
                            break;
                        case CLOCKWISE_180:
                            p1.translate(0, -1);
                            p2.translate(1, 0);
                            break;
                        case COUNTERCLOCKWISE_90:
                            p1.translate(0, 1);
                            p2.translate(1, 0);
                            break;
                        default:
                            return;
                    }
                    break;
                case STRAIGHT:
                case END:
                    switch (e.rotation) {
                        case NONE:
                        case CLOCKWISE_180:
                            p1.translate(1, 0);
                            p2.translate(-1, 0);
                            break;
                        case CLOCKWISE_90:
                        case COUNTERCLOCKWISE_90:
                            p1.translate(0, 1);
                            p2.translate(0, -1);
                            break;
                        default:
                            return;
                    }
                    break;
                default:
                    return;
            }
            final Path path = new Path(p1, p2);
            possibleConnections.put(path, e);
            possibleConnections.put(path.getInverse(), e);
        });
    }

    public Point getPoint() {
        return point;
    }

    private static final String POINT_LIST = "pointList";

    @Override
    public void write(final NBTTagCompound compound) {
        final NBTTagList pointList = new NBTTagList();
        possibleModes.forEach((mode, option) -> {
            final NBTTagCompound entry = new NBTTagCompound();
            mode.write(entry);
            option.write(entry);
            pointList.appendTag(entry);
        });
        compound.setTag(POINT_LIST, pointList);
        this.point.write(compound);
    }

    @Override
    public void read(final NBTTagCompound compound) {
        final NBTTagList pointList = (NBTTagList) compound.getTag(POINT_LIST);
        if (pointList == null)
            return;
        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        pointList.forEach(e -> {
            final NBTTagCompound tag = (NBTTagCompound) e;
            final PathOptionEntry entry = factory.getEntry();
            entry.read(tag);
            possibleModes.put(new ModeSet(tag), entry);
        });
        this.point.read(compound);
        this.identifier = point.getX() + "." + point.getY();
        this.post();
    }

    public Optional<PathOptionEntry> getOption(final Path path) {
        return getOption(Optional.ofNullable(possibleConnections.get(path)));
    }

    public Optional<PathOptionEntry> getOption(final ModeSet mode) {
        return Optional.ofNullable(possibleModes.get(mode));
    }

    public Optional<PathOptionEntry> getOption(final Optional<ModeSet> mode) {
        return mode.flatMap(this::getOption);
    }

    public PathType getPathType(final SignalBoxNode other) {
        if (other == null || other.getPoint().equals(this.getPoint()))
            return PathType.NONE;
        final Set<EnumGuiMode> thisMode = this.possibleModes.keySet().stream()
                .map(mode -> mode.mode).collect(Collectors.toSet());

        final Set<EnumGuiMode> otherMode = other.possibleModes.keySet().stream()
                .map(mode -> mode.mode).collect(Collectors.toSet());
        for (final PathType type : PathType.values()) {
            final boolean thisContains = Arrays.stream(type.getModes())
                    .anyMatch(thisMode::contains);
            final boolean otherContains = Arrays.stream(type.getModes())
                    .anyMatch(otherMode::contains);
            if (thisContains && otherContains)
                return type;
        }
        return PathType.NONE;
    }

    public boolean canMakePath(final Path path, final PathType type) {
        final ModeSet modeSet = this.possibleConnections.get(path);
        if (modeSet == null)
            return false;
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(path.point1.delta(this.point));
        for (final EnumGuiMode mode : type.getModes()) {
            final ModeSet possibleOverStepping = new ModeSet(mode, rotation);
            if (this.possibleModes.containsKey(possibleOverStepping))
                return false; // Found another signal on the path that is not the target
        }
        return true;
    }

    public boolean isEmpty() {
        return this.possibleModes.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, possibleModes);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalBoxNode other = (SignalBoxNode) obj;
        return Objects.equals(point, other.point)
                && Objects.equals(possibleModes, other.possibleModes);
    }

    @Override
    public String toString() {
        return "SignalBoxNode [point=" + point + ", possibleConnections=" + possibleConnections
                + ", possibleModes=" + possibleModes + "]";
    }

    public boolean isValidStart() {
        return this.possibleModes.keySet().stream()
                .anyMatch(modeSet -> VALID_MODES.contains(modeSet.mode));
    }

    public Set<Path> connections() {
        return ImmutableSet.copyOf(this.possibleConnections.keySet());
    }

    @Override
    public Iterator<ModeSet> iterator() {
        return this.possibleModes.keySet().iterator();
    }

    @Override
    public void writeEntryNetwork(final NBTTagCompound tag, final boolean writeAll) {
        if (this.isEmpty())
            return;
        final NBTTagList pointList = new NBTTagList();
        this.possibleModes.forEach((modeset, option) -> {
            final NBTTagCompound compound = new NBTTagCompound();
            option.writeEntryNetwork(compound, writeAll);
            modeset.write(compound);
            pointList.appendTag(compound);
        });
        tag.setTag(this.identifier, pointList);
    }

    @Override
    public void readEntryNetwork(final NBTTagCompound tag) {
        final NBTTagList points = (NBTTagList) tag.getTag(this.identifier);
        if (points == null)
            return;
        if (points.hasNoTags()) {
            this.possibleModes.clear();
            return;
        }
        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        final Set<ModeSet> modeSets = new HashSet<>();
        points.forEach(nbt -> {
            final NBTTagCompound compound = (NBTTagCompound) nbt;
            final ModeSet set = new ModeSet(compound);
            modeSets.add(set);
            final PathOptionEntry entry = this.possibleModes.computeIfAbsent(set,
                    _u -> factory.getEntry());
            entry.readEntryNetwork(compound);
        });
        this.possibleModes.keySet().removeIf(mode -> !modeSets.contains(mode));
        this.post();
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

}