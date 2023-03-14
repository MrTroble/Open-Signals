package com.troblecodings.signals.signalbox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SubsidiarySignalParser;
import com.troblecodings.signals.core.BufferBuilder;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.properties.ConfigProperty;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.core.BlockPos;

public class SignalBoxGrid implements INetworkSavable {

    private static final String NODE_LIST = "nodeList";

    public final Map<Point, SignalBoxPathway> clientPathways = new HashMap<>();
    protected final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    private final Map<Point, Map<ModeSet, SubsidiaryEntry>> enabledSubsidiaryTypes = new HashMap<>();
    protected final SignalBoxFactory factory;
    private SignalBoxTileEntity tile;

    public SignalBoxGrid() {
        this.factory = SignalBoxFactory.getFactory();
    }

    public void setTile(final SignalBoxTileEntity tile) {
        this.tile = tile;
    }

    public void resetPathway(final Point p1) {
        SignalBoxHandler.resetPathway(tile.getBlockPos(), p1);
        enabledSubsidiaryTypes.remove(p1);
    }

    public boolean requestWay(final Point p1, final Point p2) {
        return SignalBoxHandler.requestPathway(tile.getBlockPos(), p1, p2, modeGrid);
    }

    public void resetAllPathways() {
        SignalBoxHandler.resetAllPathways(tile.getBlockPos());
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putList(NODE_LIST, modeGrid.values().stream().map(node -> {
            final NBTWrapper nodeTag = new NBTWrapper();
            node.write(nodeTag);
            return nodeTag;
        })::iterator);
    }

    @Override
    public void read(final NBTWrapper tag) {
        modeGrid.clear();
        tag.getList(NODE_LIST).forEach(comp -> {
            final SignalBoxNode node = new SignalBoxNode();
            node.read(comp);
            node.post();
            modeGrid.put(node.getPoint(), node);
        });
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabledSubsidiaryTypes, modeGrid, tile);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalBoxGrid other = (SignalBoxGrid) obj;
        return Objects.equals(enabledSubsidiaryTypes, other.enabledSubsidiaryTypes)
                && Objects.equals(modeGrid, other.modeGrid) && Objects.equals(tile, other.tile);
    }

    @Override
    public String toString() {
        return "SignalBoxGrid [modeGrid=" + modeGrid.entrySet().stream()
                .map(entry -> entry.toString()).collect(Collectors.joining("\n")) + "]";
    }

    public boolean isEmpty() {
        return this.modeGrid.isEmpty();
    }

    public SignalBoxNode getNode(final Point point) {
        return modeGrid.get(point);
    }

    public List<SignalBoxNode> getNodes() {
        return ImmutableList.copyOf(this.modeGrid.values());
    }

    public Map<Point, SignalBoxNode> getModeGrid() {
        return modeGrid;
    }

    public void putNode(final Point point, final SignalBoxNode node) {
        modeGrid.put(point, node);
    }

    public SignalBoxNode removeNode(final Point point) {
        return modeGrid.remove(point);
    }

    public SignalBoxNode computeIfAbsent(final Point point,
            final Function<? super Point, ? extends SignalBoxNode> funtion) {
        return modeGrid.computeIfAbsent(point, funtion);
    }

    @Override
    public void readNetwork(final ByteBuffer buffer) {
        enabledSubsidiaryTypes.clear();
        final int size = buffer.getInt();
        for (int i = 0; i < size; i++) {
            final Point point = new Point(buffer);
            final SignalBoxNode node = modeGrid.computeIfAbsent(point,
                    _u -> new SignalBoxNode(point));
            final int enabledSubsidariesSize = Byte.toUnsignedInt(buffer.get());
            if (enabledSubsidariesSize != 0) {
                for (int j = 0; j < enabledSubsidariesSize; j++) {
                    final Map<ModeSet, SubsidiaryEntry> allTypes = enabledSubsidiaryTypes
                            .computeIfAbsent(point, _u -> new HashMap<>());
                    final ModeSet mode = new ModeSet(buffer);
                    final SubsidiaryEntry type = SubsidiaryEntry.of(buffer);
                    allTypes.put(mode, type);
                    enabledSubsidiaryTypes.put(point, allTypes);
                }
            }
            node.readNetwork(buffer);
        }
    }

    public void readUpdateNetwork(final ByteBuffer buffer, final boolean override) {
        final int size = buffer.getInt();
        final List<SignalBoxNode> allNodesForPathway = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Point point = new Point(buffer);
            final SignalBoxNode node;
            if (override) {
                modeGrid.remove(point);
                node = new SignalBoxNode(point);
            } else {
                node = modeGrid.computeIfAbsent(point, _u -> new SignalBoxNode(point));
            }
            node.readUpdateNetwork(buffer);
            allNodesForPathway.add(node);
            modeGrid.put(point, node);
        }
        if (tile != null && !tile.getLevel().isClientSide())
            return;
        final SignalBoxPathway pathway = new SignalBoxPathway(modeGrid, allNodesForPathway,
                PathType.NORMAL);
        clientPathways.put(pathway.getFirstPoint(), pathway);
    }

    public void writeToBuffer(final BufferBuilder buffer) {
        buffer.putInt(modeGrid.size());
        modeGrid.forEach((point, node) -> {
            point.writeToBuffer(buffer);
            final Map<ModeSet, SubsidiaryEntry> enabledSubsidiaries = enabledSubsidiaryTypes
                    .get(point);
            if (enabledSubsidiaries == null) {
                buffer.putByte((byte) 0);
            } else {
                buffer.putByte((byte) enabledSubsidiaries.size());
                enabledSubsidiaries.forEach((mode, state) -> {
                    mode.writeToBuffer(buffer);
                    state.writeNetwork(buffer);
                });
            }
            node.writeToBuffer(buffer);
        });
    }

    public void writeUpdateToBuffer(final BufferBuilder buffer) {
        buffer.putInt(modeGrid.size());
        modeGrid.forEach((point, node) -> {
            point.writeToBuffer(buffer);
            node.writeUpdateBuffer(buffer);
        });
    }

    @Override
    public void writeNetwork(final ByteBuffer buffer) {
    }

    public void updateSubsidiarySignal(final Point point, final ModeSet mode,
            final SubsidiaryEntry entry) {
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return;
        final Optional<BlockPos> pos = node.getOption(mode).get().getEntry(PathEntryType.SIGNAL);
        if (pos.isEmpty())
            return;
        final Signal signal = tile.getSignal(pos.get());
        if (!entry.state) {
            if (!enabledSubsidiaryTypes.containsKey(point))
                return;
            SignalConfig.reset(new SignalStateInfo(tile.getLevel(), pos.get(), signal));
            final Map<ModeSet, SubsidiaryEntry> states = enabledSubsidiaryTypes.get(point);
            states.remove(mode);
            if (states.isEmpty()) {
                enabledSubsidiaryTypes.remove(point);
            }
            return;
        }
        final Map<ModeSet, SubsidiaryEntry> states = enabledSubsidiaryTypes.computeIfAbsent(point,
                _u -> new HashMap<>());
        final Map<SubsidiaryState, ConfigProperty> configs = SubsidiarySignalParser.SUBSIDIARY_SIGNALS
                .get(signal);
        if (configs == null)
            return;
        final ConfigProperty properties = configs.get(entry.enumValue);
        if (properties == null)
            return;
        final SignalStateInfo info = new SignalStateInfo(tile.getLevel(), pos.get(), signal);
        final Map<SEProperty, String> oldProperties = SignalStateHandler.getStates(info);
        SignalStateHandler.setStates(info, properties.values.entrySet().stream()
                .filter(propertyEntry -> oldProperties.containsKey(propertyEntry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)));
        states.put(mode, entry);
        enabledSubsidiaryTypes.put(point, states);
    }

    public boolean getSubsidiaryState(final Point point, final ModeSet mode,
            final SubsidiaryState type) {
        final Map<ModeSet, SubsidiaryEntry> states = enabledSubsidiaryTypes.get(point);
        if (states == null)
            return false;
        final SubsidiaryEntry entry = states.get(mode);
        if (entry == null)
            return false;
        if (entry.enumValue.equals(type))
            return entry.state;
        return false;
    }

    public void setClientState(final Point point, final ModeSet mode, final SubsidiaryEntry entry) {
        if (!entry.state) {
            enabledSubsidiaryTypes.remove(point);
            return;
        }
        final Map<ModeSet, SubsidiaryEntry> states = enabledSubsidiaryTypes.computeIfAbsent(point,
                _u -> new HashMap<>());
        states.put(mode, entry);
        enabledSubsidiaryTypes.put(point, states);
    }

    public Map<Point, Map<ModeSet, SubsidiaryEntry>> getAllSubsidiaries() {
        return ImmutableMap.copyOf(enabledSubsidiaryTypes);
    }
}