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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.core.interfaces.INetworkSaveable;
import com.troblecodings.core.interfaces.ISaveable;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.network.PathOptionEntryNetwork;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.SignalBoxUtil.PathIdentifier;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class SignalBoxNode implements INetworkSaveable, ISaveable, Iterable<ModeSet> {

    private final HashMap<Path, ModeSet> possibleConnections = new HashMap<>();
    private final HashMap<ModeSet, PathOptionEntry> possibleModes = new HashMap<>();
    private final HashMap<ModeSet, SignalState> signalStates = new HashMap<>();
    private final HashMap<ModeSet, SubsidiaryState> enabledSubsidiaryStates = new HashMap<>();
    private final List<ModeSet> manuellEnabledOutputs = new ArrayList<>();
    private final Point point;
    private final SignalBoxNetworkHandler network;
    private final SignalBoxFactory factory = SignalBoxFactory.getFactory();
    private boolean isAutoPoint = false;
    private String customText = "";

    public SignalBoxNode(final SignalBoxNetworkHandler network) {
        this(new Point(), network);
    }

    public SignalBoxNode(final Point point, final SignalBoxNetworkHandler network) {
        this.point = Objects.requireNonNull(point);
        this.network = Objects.requireNonNull(network);
    }

    public void add(final ModeSet modeSet) {
        final ModeIdentifier ident = new ModeIdentifier(point, modeSet);
        final PathOptionEntry entry = factory.getEntry();
        entry.setUpNetwork(new PathOptionEntryNetwork().setUpNetwork(network, ident));
        possibleModes.put(modeSet, entry);
        network.sendModeAdd(ident);
    }

    public boolean has(final ModeSet modeSet) {
        return possibleModes.containsKey(modeSet);
    }

    public void remove(final ModeSet modeSet) {
        possibleModes.remove(modeSet);
        network.sendModeRemove(new ModeIdentifier(point, modeSet));
    }

    public <T> void addAndSetEntry(final ModeSet mode, final PathEntryType<T> entry, final T type) {
        final PathOptionEntry optionEntry =
                possibleModes.computeIfAbsent(mode, _u -> factory.getEntry());
        optionEntry.setUpNetwork(new PathOptionEntryNetwork().setUpNetwork(network,
                new ModeIdentifier(point, mode)));
        optionEntry.setEntry(entry, type);
    }

    public void updateState(final ModeSet modeSet, final SignalState state) {
        updateStateNoNetwork(modeSet, state);
        network.sendUpdateSignalStates(this);
    }

    public void updateStateNoNetwork(final ModeSet modeSet, final SignalState state) {
        if (state == SignalState.RED) {
            this.signalStates.remove(modeSet);
            this.enabledSubsidiaryStates.remove(modeSet);
        } else {
            this.signalStates.put(modeSet, state);
        }
    }

    public SignalState getState(final ModeSet modeSet) {
        return this.signalStates.getOrDefault(modeSet, SignalState.RED);
    }

    public void setSubsidiaryState(final ModeSet mode, final SubsidiaryState entry) {
        enabledSubsidiaryStates.put(mode, entry);
    }

    public void removeSubsidiaryState(final ModeSet mode) {
        enabledSubsidiaryStates.remove(mode);
    }

    public SubsidiaryState getSubsidiaryState(final ModeSet mode) {
        return enabledSubsidiaryStates.get(mode);
    }

    public Map<ModeSet, SubsidiaryState> getSubsidiaryStates() {
        return ImmutableMap.copyOf(enabledSubsidiaryStates);
    }

    public void addManuellOutput(final ModeSet mode) {
        if (!manuellEnabledOutputs.contains(mode)) {
            manuellEnabledOutputs.add(mode);
            network.sendManuellOutputAdd(point, mode);
        }
    }

    public void removeManuellOutput(final ModeSet mode) {
        manuellEnabledOutputs.remove(mode);
        network.sendManuellOutputRemove(point, mode);
    }

    public List<BlockPos> clearAllManuellOutputs() {
        final List<BlockPos> returnList = new ArrayList<>();
        manuellEnabledOutputs.forEach(mode -> {
            network.sendManuellOutputRemove(point, mode);
            returnList.add(possibleModes.get(mode).getEntry(PathEntryType.OUTPUT).get());
        });
        manuellEnabledOutputs.clear();
        return returnList;
    }

    public void setAutoPointFromNetwork(final boolean isAutoPoint) {
        this.isAutoPoint = isAutoPoint;
    }

    public void setAutoPoint(final boolean isAutoPoint) {
        this.isAutoPoint = isAutoPoint;
        network.sendAutoPoint(point, isAutoPoint);
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
            final Point p3 = new Point(this.point);
            final Point p4 = new Point(this.point);
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
                case CROSSING:
                    switch (mode.rotation) {
                        case NONE:
                        case CLOCKWISE_180:
                            p1.translate(1, 0);
                            p2.translate(-1, 0);
                            p3.translate(0, 1);
                            p4.translate(0, -1);
                            break;
                        case CLOCKWISE_90:
                        case COUNTERCLOCKWISE_90:
                            p1.translate(0, 1);
                            p2.translate(0, -1);
                            p3.translate(1, 0);
                            p4.translate(-1, 0);
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
            if (mode.mode.equals(EnumGuiMode.CROSSING)) {
                final Path secondPath = new Path(p3, p4);
                possibleConnections.put(secondPath, mode);
                possibleConnections.put(secondPath.getInverse(), mode);
            }
        }
    }

    public Point getPoint() {
        return point;
    }

    private static final String POINT_LIST = "pointList";
    private static final String ENABLED_OUTPUTS = "enabledOutputs";
    private static final String IS_AUTO_POINT = "isAutoPoint";
    private static final String CUSTOM_NAME = "customTextName";
    private static final String SIGNAL_STATE = "signalstate";
    private static final String SUBSIDIARY_ENTRY = "subsidiaryEntry";

    @Override
    public void write(final NBTWrapper compound) {
        compound.putList(POINT_LIST, possibleModes.entrySet().stream().map((entry) -> {
            final NBTWrapper wrapper = new NBTWrapper();
            entry.getKey().write(wrapper);
            entry.getValue().write(wrapper);
            if (manuellEnabledOutputs.contains(entry.getKey())) {
                wrapper.putBoolean(ENABLED_OUTPUTS, true);
            }
            final SignalState state = signalStates.getOrDefault(entry.getKey(), SignalState.RED);
            if (!state.equals(SignalState.RED)) {
                wrapper.putString(SIGNAL_STATE, state.name());
            }
            final SubsidiaryState subsidiary = enabledSubsidiaryStates.get(entry.getKey());
            if (subsidiary != null) {
                final NBTWrapper subsidiaryWrapper = new NBTWrapper();
                subsidiary.writeNBT(subsidiaryWrapper);
                wrapper.putWrapper(SUBSIDIARY_ENTRY, subsidiaryWrapper);
            }
            return wrapper;
        })::iterator);
        this.point.write(compound);
        if (isAutoPoint) {
            compound.putBoolean(IS_AUTO_POINT, isAutoPoint);
        }
        if (!customText.isEmpty()) {
            compound.putString(CUSTOM_NAME, customText);
        }
    }

    @Override
    public void read(final NBTWrapper compound) {
        final boolean oldOutputSystem = compound.contains(ENABLED_OUTPUTS);
        compound.getList(POINT_LIST).forEach(tag -> {
            final ModeSet mode = new ModeSet(tag);
            final PathOptionEntry entry = factory.getEntry();
            entry.setUpNetwork(new PathOptionEntryNetwork().setUpNetwork(network,
                    new ModeIdentifier(point, mode)));
            entry.read(tag);
            possibleModes.put(mode, entry);
            if (!oldOutputSystem)
                if (tag.getBoolean(ENABLED_OUTPUTS)) {
                    if (!manuellEnabledOutputs.contains(mode)) {
                        manuellEnabledOutputs.add(mode);
                    }
                }
            final String stateName = tag.getString(SIGNAL_STATE);
            if (stateName != null && !stateName.isEmpty()) {
                final SignalState state = SignalState.valueOf(stateName);
                if (!state.equals(SignalState.RED)) {
                    signalStates.put(mode, state);
                }
            }
            if (tag.contains(SUBSIDIARY_ENTRY)) {
                final NBTWrapper subsidiaryWrapper = tag.getWrapper(SUBSIDIARY_ENTRY);
                enabledSubsidiaryStates.put(mode, SubsidiaryState.of(subsidiaryWrapper));
            }
        });
        if (oldOutputSystem) {
            compound.getList(ENABLED_OUTPUTS).forEach(tag -> {
                final ModeSet modeSet = new ModeSet(tag);
                if (!manuellEnabledOutputs.contains(modeSet)) {
                    manuellEnabledOutputs.add(modeSet);
                }
            });
        }
        this.point.read(compound);
        if (compound.contains(IS_AUTO_POINT)) {
            this.isAutoPoint = compound.getBoolean(IS_AUTO_POINT);
        }
        if (compound.contains(CUSTOM_NAME)) {
            this.customText = compound.getString(CUSTOM_NAME);
        }
        post();
    }

    public Optional<PathOptionEntry> getOption(final Path path) {
        return getOption(Optional.ofNullable(possibleConnections.get(path)));
    }

    public ModeSet getMode(final Path path) {
        return possibleConnections.get(path);
    }

    public PathOptionEntry getOrCreateOption(final ModeSet mode) {
        final PathOptionEntry entry = possibleModes.computeIfAbsent(mode, _u -> factory.getEntry());
        entry.setUpNetwork(new PathOptionEntryNetwork().setUpNetwork(network,
                new ModeIdentifier(point, mode)));
        return entry;
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
            final boolean thisContains =
                    Arrays.stream(type.getModes()).anyMatch(thisMode::contains);
            final boolean otherContains =
                    Arrays.stream(type.getModes()).anyMatch(otherMode::contains);
            if (thisContains && otherContains) {
                possibleTypes.add(type);
            }
        }
        return possibleTypes;
    }

    public PathwayRequestMode canMakePath(final Path path, final PathType type) {
        final ModeSet modeSet = this.possibleConnections.get(path);
        if (modeSet == null)
            return PathwayRequestMode.NO_PATH;
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(path.point1.delta(this.point));
        for (final EnumGuiMode mode : type.getModes()) {
            final ModeSet possibleOverStepping = new ModeSet(mode, rotation);
            if (this.possibleModes.containsKey(possibleOverStepping)) {
                final PathOptionEntry option = possibleModes.get(possibleOverStepping);
                if (option.getEntry(PathEntryType.CAN_BE_OVERSTPEPPED).orElse(false)) {
                    continue;
                }
                return PathwayRequestMode.OVERSTEPPING; // Found another signal on the path that
                                                        // is not the target
            }
        }
        return PathwayRequestMode.PASS;
    }

    public boolean isUsed() {
        for (final Point point : Arrays.asList(this.point.delta(new Point(1, 0)),
                this.point.delta(new Point(-1, 0)), this.point.delta(new Point(0, 1)),
                this.point.delta(new Point(0, -1)))) {
            if (isUsedInDirection(point, null))
                return true;
        }
        return false;
    }

    public boolean isUsedInDirection(final Point point, @Nullable final EnumPathUsage exclude) {
        for (final Path path : possibleConnections.keySet()) {
            if (!path.point1.equals(point) && !path.point2.equals(point)) {
                continue;
            }
            final ModeSet mode = getMode(path);
            if (mode == null) {
                continue;
            }
            final EnumPathUsage usage = getOption(mode).orElse(factory.getEntry())
                    .getEntry(PathEntryType.PATHUSAGE).orElse(EnumPathUsage.FREE);
            if (!(usage.equals(exclude) || usage.equals(EnumPathUsage.FREE)))
                return true;
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

    public List<PathIdentifier> getStartIdentifiers() {
        final List<PathIdentifier> starts = new ArrayList<>();
        final Set<Path> paths = possibleConnections.keySet();
        possibleModes.keySet().forEach(mode -> {
            if (!mode.mode.getModeType().isValidStart())
                return;
            final Point delta = SignalBoxUtil.getDeltaFromRotation(mode.rotation);
            final Point start = new Point(point);
            start.translate(delta.getX(), delta.getY());
            for (final Path path : paths) {
                if (path.point2.equals(start)) {
                    starts.add(new PathIdentifier(path, this.point, possibleConnections.get(path)));
                }
            }
        });
        return starts;
    }

    @Override
    public Iterator<ModeSet> iterator() {
        return this.possibleModes.keySet().iterator();
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        possibleModes.clear();
        manuellEnabledOutputs.clear();
        buffer.getMapWithCombinedValueFunc(ReadBuffer.getINetworkSaveableFunction(ModeSet.class),
                (buf, mode) -> {
                    final PathOptionEntry entry = factory.getEntry();
                    entry.setUpNetwork(new PathOptionEntryNetwork().setUpNetwork(network,
                            new ModeIdentifier(point, mode)));
                    entry.readNetwork(buf);
                    return entry;
                }).forEach((mode, entry) -> possibleModes.put(mode, entry));
        buffer.getList(ReadBuffer.getINetworkSaveableFunction(ModeSet.class)).forEach(mode -> {
            if (!manuellEnabledOutputs.contains(mode)) {
                manuellEnabledOutputs.add(mode);
            }
        });
        this.isAutoPoint = buffer.getBoolean();
        this.customText = buffer.getString();
        signalStates.clear();
        enabledSubsidiaryStates.clear();
        buffer.getMap(ReadBuffer.getINetworkSaveableFunction(ModeSet.class),
                ReadBuffer.getEnumFunction(SignalState.class))
                .forEach((mode, state) -> signalStates.put(mode, state));
        buffer.getMap(ReadBuffer.getINetworkSaveableFunction(ModeSet.class),
                (buf) -> SubsidiaryState.of(buf))
                .forEach((mode, entry) -> enabledSubsidiaryStates.put(mode, entry));
        post();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putMap(possibleModes, WriteBuffer.getINetworkSaveableConsumer(),
                (buf, entry) -> entry.writeNetwork(buf));
        buffer.putISaveableList(manuellEnabledOutputs);
        buffer.putBoolean(isAutoPoint);
        buffer.putString(customText);
        buffer.putMap(signalStates, WriteBuffer.getINetworkSaveableConsumer(),
                WriteBuffer.getEnumConsumer());
        buffer.putMap(enabledSubsidiaryStates, WriteBuffer.getINetworkSaveableConsumer(),
                (buf, entry) -> entry.writeNetwork(buf));
    }

    public void applyModeNetworkChanges(final ModeSet mode) {
        if (!has(mode)) {
            final PathOptionEntry entry = factory.getEntry();
            entry.setUpNetwork(new PathOptionEntryNetwork().setUpNetwork(network,
                    new ModeIdentifier(point, mode)));
            possibleModes.put(mode, entry);
        } else {
            possibleModes.remove(mode);
        }
    }

    public void writeSignalStates(final WriteBuffer buffer) {
        buffer.putMap(signalStates, WriteBuffer.getINetworkSaveableConsumer(),
                WriteBuffer.getEnumConsumer());
        buffer.putMap(enabledSubsidiaryStates, WriteBuffer.getINetworkSaveableConsumer(),
                (buf, state) -> state.writeNetwork(buf));
    }

    public void handleManuellEnabledOutputUpdate(final ModeSet mode, final boolean state) {
        if (state) {
            if (!manuellEnabledOutputs.contains(mode)) {
                manuellEnabledOutputs.add(mode);
            }
        } else {
            manuellEnabledOutputs.remove(mode);
        }
    }

    public void readSignalStates(final ReadBuffer buffer) {
        signalStates.clear();
        enabledSubsidiaryStates.clear();
        signalStates.putAll(buffer.getMap(ReadBuffer.getINetworkSaveableFunction(ModeSet.class),
                ReadBuffer.getEnumFunction(SignalState.class)));
        enabledSubsidiaryStates
                .putAll(buffer.getMap(ReadBuffer.getINetworkSaveableFunction(ModeSet.class),
                        buf -> SubsidiaryState.of(buf)));
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