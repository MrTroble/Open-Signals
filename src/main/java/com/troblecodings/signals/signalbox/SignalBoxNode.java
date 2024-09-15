package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.signalbox.SignalBoxUtil.PathIdentifier;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class SignalBoxNode implements INetworkSavable, Iterable<ModeSet> {

    private final HashMap<Path, ModeSet> possibleConnections = new HashMap<>();
    private final HashMap<ModeSet, PathOptionEntry> possibleModes = new HashMap<>();
    private final List<ModeSet> manuellEnabledOutputs = new ArrayList<>();
    private final Point point;
    private boolean isAutoPoint = false;
    private String customText = "";

    public SignalBoxNode() {
        this(new Point());
    }

    public SignalBoxNode(final Point point) {
        this.point = Objects.requireNonNull(point);
    }

    public void add(final ModeSet modeSet) {
        possibleModes.put(modeSet, SignalBoxFactory.getFactory().getEntry());
    }

    public <T> void addAndSetEntry(final ModeSet mode, final PathEntryType<T> entry, final T type) {
        final PathOptionEntry optionEntry = possibleModes.computeIfAbsent(mode,
                _u -> SignalBoxFactory.getFactory().getEntry());
        optionEntry.setEntry(entry, type);
    }

    public boolean has(final ModeSet modeSet) {
        return possibleModes.containsKey(modeSet);
    }

    public void addManuellOutput(final ModeSet mode) {
        if (!manuellEnabledOutputs.contains(mode))
            manuellEnabledOutputs.add(mode);
    }

    public void removeManuellOutput(final ModeSet mode) {
        manuellEnabledOutputs.remove(mode);
    }

    public List<BlockPos> clearAllManuellOutputs() {
        final List<BlockPos> returnList = new ArrayList<>();
        manuellEnabledOutputs.forEach(mode -> returnList
                .add(possibleModes.get(mode).getEntry(PathEntryType.OUTPUT).get()));
        manuellEnabledOutputs.clear();
        return returnList;
    }

    public List<ModeSet> getManuellEnabledOutputs() {
        return ImmutableList.copyOf(manuellEnabledOutputs);
    }

    public void remove(final ModeSet modeSet) {
        possibleModes.remove(modeSet);
    }

    public void setAutoPoint(final boolean isAutoPoint) {
        this.isAutoPoint = isAutoPoint;
    }

    public boolean isAutoPoint() {
        return isAutoPoint;
    }

    public void post() {
        possibleConnections.clear();
        for (final Map.Entry<ModeSet, PathOptionEntry> entry : possibleModes.entrySet()) {
            final ModeSet mode = entry.getKey();
            if (mode.mode.equals(EnumGuiMode.SH2)) {
                possibleConnections.clear();
                return;
            }
            final Point p1 = new Point(this.point);
            final Point p2 = new Point(this.point);
            switch (mode.mode) {
                case CORNER:
                    switch (mode.rotation) {
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
                            break;
                    }
                    break;
                case STRAIGHT:
                case END:
                case IN_CONNECTION:
                case OUT_CONNECTION:
                case ARROW:
                    switch (mode.rotation) {
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
                            break;
                    }
                    break;
                default:
                    continue;
            }
            final Path path = new Path(p1, p2);
            possibleConnections.put(path, mode);
            possibleConnections.put(path.getInverse(), mode);
        }
    }

    public Point getPoint() {
        return point;
    }

    private static final String POINT_LIST = "pointList";
    private static final String ENABLED_OUTPUTS = "enabledOutputs";
    private static final String IS_AUTO_POINT = "isAutoPoint";
    private static final String CUSTOM_NAME = "customTextName";

    @Override
    public void write(final NBTWrapper compound) {
        compound.putList(POINT_LIST, possibleModes.entrySet().stream().map((entry) -> {
            final NBTWrapper wrapper = new NBTWrapper();
            entry.getKey().write(wrapper);
            entry.getValue().write(wrapper);
            return wrapper;
        })::iterator);
        final List<NBTWrapper> enabledOutputs = new ArrayList<>();
        manuellEnabledOutputs.forEach(mode -> {
            final NBTWrapper wrapper = new NBTWrapper();
            mode.write(wrapper);
            enabledOutputs.add(wrapper);
        });
        compound.putList(ENABLED_OUTPUTS, enabledOutputs);
        this.point.write(compound);
        compound.putBoolean(IS_AUTO_POINT, isAutoPoint);
        compound.putString(CUSTOM_NAME, customText);
    }

    @Override
    public void read(final NBTWrapper compound) {
        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        compound.getList(POINT_LIST).forEach(tag -> {
            final PathOptionEntry entry = factory.getEntry();
            entry.read(tag);
            possibleModes.put(new ModeSet(tag), entry);
        });
        compound.getList(ENABLED_OUTPUTS).forEach(tag -> {
            final ModeSet modeSet = new ModeSet(tag);
            if (!manuellEnabledOutputs.contains(modeSet))
                manuellEnabledOutputs.add(modeSet);
        });
        this.point.read(compound);
        this.isAutoPoint = compound.getBoolean(IS_AUTO_POINT);
        this.customText = compound.getString(CUSTOM_NAME);
        post();
    }

    public Optional<PathOptionEntry> getOption(final Path path) {
        return getOption(Optional.ofNullable(possibleConnections.get(path)));
    }

    public ModeSet getMode(final Path path) {
        return possibleConnections.get(path);
    }

    public Optional<PathOptionEntry> getOption(final ModeSet mode) {
        return Optional.ofNullable(possibleModes.get(mode));
    }

    public Optional<PathOptionEntry> getOption(final Optional<ModeSet> mode) {
        return mode.flatMap(this::getOption);
    }

    public List<PathType> getPossibleTypes(final SignalBoxNode other) {
        final List<PathType> possibleTypes = new ArrayList<>();
        if (other == null || other.getPoint().equals(this.getPoint()))
            return possibleTypes;
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
                possibleTypes.add(type);
        }
        return possibleTypes;
    }

    public PathwayRequestResult canMakePath(final Path path, final PathType type) {
        final ModeSet modeSet = this.possibleConnections.get(path);
        if (modeSet == null)
            return PathwayRequestResult.NO_PATH;
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(path.point1.delta(this.point));
        for (final EnumGuiMode mode : type.getModes()) {
            final ModeSet possibleOverStepping = new ModeSet(mode, rotation);
            if (this.possibleModes.containsKey(possibleOverStepping)) {
                final PathOptionEntry option = possibleModes.get(possibleOverStepping);
                if (option.getEntry(PathEntryType.CAN_BE_OVERSTPEPPED).orElse(false)) {
                    continue;
                }
                return PathwayRequestResult.OVERSTEPPING; // Found another signal on the path that
                                                          // is not the target
            }
        }
        return PathwayRequestResult.PASS;
    }

    public boolean isUsed() {
        for (final Point point : Arrays.asList(this.point.delta(new Point(1, 0)),
                this.point.delta(new Point(-1, 0)), this.point.delta(new Point(0, 1)),
                this.point.delta(new Point(0, -1)))) {
            if (isUsedInDirection(point)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUsedInDirection(final Point point) {
        for (final Path path : possibleConnections.keySet()) {
            if (!(path.point1.equals(point) || path.point2.equals(point))) {
                continue;
            }
            final ModeSet mode = getMode(path);
            if (mode == null) {
                continue;
            }
            if (!getOption(mode).orElse(new PathOptionEntry()).getEntry(PathEntryType.PATHUSAGE)
                    .orElse(EnumPathUsage.FREE).equals(EnumPathUsage.FREE)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsManuellOutput(final ModeSet mode) {
        return manuellEnabledOutputs.contains(mode);
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
        if ((obj == null) || (getClass() != obj.getClass()))
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
                .anyMatch(modeSet -> modeSet.mode.getModeType().isValidStart());
    }

    public boolean isValidEnd() {
        return this.possibleModes.keySet().stream()
                .anyMatch(modeSet -> modeSet.mode.getModeType().isValidEnd());
    }

    public boolean containsInConnection() {
        return this.possibleModes.keySet().stream()
                .anyMatch(modeSet -> modeSet.mode.equals(EnumGuiMode.IN_CONNECTION));
    }

    public boolean containsOutConnection() {
        return this.possibleModes.keySet().stream()
                .anyMatch(modeSet -> modeSet.mode.equals(EnumGuiMode.OUT_CONNECTION));
    }

    public List<PathIdentifier> toPathIdentifier() {
        return possibleConnections.entrySet().stream()
                .map(entry -> new PathIdentifier(entry.getKey(), point, entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<ModeSet> iterator() {
        return this.possibleModes.keySet().iterator();
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        possibleModes.clear();
        manuellEnabledOutputs.clear();
        final int size = buffer.getByteToUnsignedInt();
        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        for (int i = 0; i < size; i++) {
            final ModeSet mode = ModeSet.of(buffer);
            final PathOptionEntry entry = factory.getEntry();
            entry.readNetwork(buffer);
            possibleModes.put(mode, entry);
        }
        final int outputsSize = buffer.getByteToUnsignedInt();
        for (int i = 0; i < outputsSize; i++) {
            final ModeSet modeSet = ModeSet.of(buffer);
            if (!manuellEnabledOutputs.contains(modeSet))
                manuellEnabledOutputs.add(modeSet);
        }
        this.isAutoPoint = buffer.getBoolean();
        this.customText = buffer.getString();
        post();
    }

    public void readUpdateNetwork(final ReadBuffer buffer) {
        final int size = buffer.getByteToUnsignedInt();
        for (int i = 0; i < size; i++) {
            final ModeSet mode = ModeSet.of(buffer);
            final PathOptionEntry entry = possibleModes.computeIfAbsent(mode,
                    _u -> SignalBoxFactory.getFactory().getEntry());
            entry.readNetwork(buffer);
            possibleModes.put(mode, entry);
        }
        final int outputsSize = buffer.getByteToUnsignedInt();
        if (outputsSize == 0)
            manuellEnabledOutputs.clear();
        for (int i = 0; i < outputsSize; i++) {
            final ModeSet modeSet = ModeSet.of(buffer);
            if (!manuellEnabledOutputs.contains(modeSet))
                manuellEnabledOutputs.add(modeSet);
        }
        this.isAutoPoint = buffer.getBoolean();
        this.customText = buffer.getString();
        post();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) possibleModes.size());
        possibleModes.forEach((mode, entry) -> {
            mode.writeNetwork(buffer);
            entry.writeNetwork(buffer);
        });
        buffer.putByte((byte) manuellEnabledOutputs.size());
        manuellEnabledOutputs.forEach(mode -> mode.writeNetwork(buffer));
        buffer.putBoolean(isAutoPoint);
        buffer.putString(customText);
    }

    public void writeUpdateNetwork(final WriteBuffer buffer) {
        int size = 0;
        for (final PathOptionEntry entry : possibleModes.values()) {
            if (entry.containsEntry(PathEntryType.PATHUSAGE)
                    || entry.containsEntry(PathEntryType.TRAINNUMBER))
                size++;
        }
        buffer.putByte((byte) size);
        possibleModes.forEach((mode, entry) -> {
            if (entry.containsEntry(PathEntryType.PATHUSAGE)
                    || entry.containsEntry(PathEntryType.TRAINNUMBER)) {
                mode.writeNetwork(buffer);
                entry.writeUpdateNetwork(buffer);
            }
        });
        buffer.putByte((byte) 0);
        buffer.putBoolean(isAutoPoint);
        buffer.putString(customText);
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(final String text) {
        this.customText = text;
    }

    public Map<ModeSet, PathOptionEntry> getModes() {
        return ImmutableMap.copyOf(possibleModes);
    }
}