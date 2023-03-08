package com.troblecodings.signals.signalbox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SubsidiarySignalParser;
import com.troblecodings.signals.core.BufferBuilder;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.enums.SubsidiaryType;
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
    private static final String PATHWAY_LIST = "pathwayList";

    public final Map<Point, SignalBoxPathway> clientPathways = new HashMap<>();
    protected final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    private final Map<Point, Map<ModeSet, SubsidiaryEntry>> enabledSubsidiaryTypes = new HashMap<>();
    protected final Consumer<NBTWrapper> sendToAll;
    protected final SignalBoxFactory factory;
    private SignalBoxTileEntity tile;

    public SignalBoxGrid(final Consumer<NBTWrapper> sendToAll) {
        this.sendToAll = sendToAll;
        this.factory = SignalBoxFactory.getFactory();
    }

    public void setTile(final SignalBoxTileEntity tile) {
        this.tile = tile;
    }

    public void resetAllPathways() {
        this.startsToPath.values().forEach(pathway -> {
            pathway.resetPathway();
        });
        clearPaths();
    }

    public void resetPathway(final Point p1) {
        if (startsToPath.isEmpty())
            return;
        final SignalBoxPathway pathway = startsToPath.get(p1);
        if (pathway == null) {
            OpenSignalsMain.log.warn("Signalboxpath is null, this should not be the case!");
            return;
        }
        resetPathway(pathway);
        updateToNet(pathway);
    }

    protected void resetPathway(final SignalBoxPathway pathway) {
        pathway.setWorld(tile.getLevel());
        pathway.resetPathway();
        updatePrevious(pathway);
        this.startsToPath.remove(pathway.getFirstPoint());
        this.endsToPath.remove(pathway.getLastPoint());
    }

    public boolean requestWay(final Point p1, final Point p2) {
        if (startsToPath.containsKey(p1) || endsToPath.containsKey(p2))
            return false;
        if (enabledSubsidiaryTypes.containsKey(p1)) {
            OpenSignalsMain.getLogger()
                    .warn("Pathway can't not be set because subsidiary Signals are enabled!");
            return false;
        }
        final Optional<SignalBoxPathway> ways = SignalBoxUtil.requestWay(modeGrid, p1, p2);
        ways.ifPresent(way -> {
            way.setWorld(tile.getLevel());
            way.setPathStatus(EnumPathUsage.SELECTED);
            way.updatePathwaySignals();
            this.onWayAdd(way);
            updateToNet(way);
        });
        return ways.isPresent();
    }

    protected void updatePrevious(final SignalBoxPathway pathway) {
        SignalBoxPathway previousPath = pathway;
        int count = 0;
        while ((previousPath = endsToPath.get(previousPath.getFirstPoint())) != null) {
            if (count > endsToPath.size()) {
                OpenSignalsMain.getLogger().error("Detected signalpath cycle, aborting!");
                startsToPath.values().forEach(path -> path.resetPathway());
                this.clearPaths();
                break;
            }
            previousPath.updatePathwaySignals();
            count++;
        }
        if (count == 0) {
            OpenSignalsMain.getLogger().debug("Could not find previous! " + pathway);
        }
    }

    protected void onWayAdd(final SignalBoxPathway pathway) {
        startsToPath.put(pathway.getFirstPoint(), pathway);
        endsToPath.put(pathway.getLastPoint(), pathway);
        updatePrevious(pathway);
    }

    protected void updateToNet(final SignalBoxPathway pathway) {
        if (tile == null) {
            OpenSignalsMain.getLogger().warn("SignalBoxTile is null. This shouldn't be the case!");
            pathway.resetPathway();
            startsToPath.remove(pathway.getFirstPoint());
            endsToPath.remove(pathway.getLastPoint());
            return;
        }
        if (tile == null || !tile.isBlocked()) {
            return;
        }
        final List<SignalBoxNode> nodes = pathway.getListOfNodes();
        final BufferBuilder buffer = new BufferBuilder();
        buffer.putByte((byte) SignalBoxNetwork.SEND_PW_UPDATE.ordinal());
        buffer.putInt(nodes.size());
        nodes.forEach(node -> {
            node.getPoint().writeToBuffer(buffer);
            node.writeUpdateBuffer(buffer);
        });
        OpenSignalsMain.network.sendTo(tile.get(0).getPlayer(), buffer.build());
    }

    public void setPowered(final BlockPos pos) {
        final List<SignalBoxPathway> nodeCopy = ImmutableList.copyOf(startsToPath.values());
        nodeCopy.forEach(pathway -> {
            if (pathway.tryBlock(pos)) {
                updateToNet(pathway);
                updatePrevious(pathway);
            }
        });
        nodeCopy.forEach(pathway -> {
            final Point first = pathway.getFirstPoint();
            final Optional<Point> optPoint = pathway.tryReset(pos);
            if (optPoint.isPresent()) {
                if (pathway.isEmptyOrBroken()) {
                    resetPathway(pathway);
                    updateToNet(pathway);
                } else {
                    updateToNet(pathway);
                    pathway.compact(optPoint.get());
                    this.startsToPath.remove(first);
                    this.startsToPath.put(pathway.getFirstPoint(), pathway);
                }
            }
        });
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putList(NODE_LIST, modeGrid.values().stream().map(node -> {
            final NBTWrapper nodeTag = new NBTWrapper();
            node.write(nodeTag);
            return nodeTag;
        })::iterator);
        tag.putList(PATHWAY_LIST,
                startsToPath.values().stream().filter(pw -> !pw.isEmptyOrBroken()).map(pathway -> {
                    final NBTWrapper path = new NBTWrapper();
                    pathway.write(path);
                    return path;
                })::iterator);
    }

    protected void clearPaths() {
        startsToPath.clear();
        endsToPath.clear();
    }

    @Override
    public void read(final NBTWrapper tag) {
        clearPaths();
        modeGrid.clear();
        tag.getList(NODE_LIST).forEach(comp -> {
            final SignalBoxNode node = new SignalBoxNode();
            node.read(comp);
            node.post();
            modeGrid.put(node.getPoint(), node);
        });
        tag.getList(PATHWAY_LIST).forEach(comp -> {
            final SignalBoxPathway pathway = factory.getPathway(this.modeGrid);
            pathway.read(comp);
            if (pathway.isEmptyOrBroken()) {
                OpenSignalsMain.log.error("Remove empty or broken pathway, try to recover!");
                return;
            }
            if (tile != null)
                pathway.setWorld(tile.getLevel());
            onWayAdd(pathway);
        });
    }

    @Override
    public int hashCode() {
        return Objects.hash(endsToPath, modeGrid, startsToPath);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalBoxGrid other = (SignalBoxGrid) obj;
        return Objects.equals(endsToPath, other.endsToPath)
                && Objects.equals(modeGrid, other.modeGrid)
                && Objects.equals(startsToPath, other.startsToPath);
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
        return ImmutableMap.copyOf(modeGrid);
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
                    final SubsidiaryType type = SubsidiaryType.of(buffer);
                    final boolean state = buffer.get() == 1 ? true : false;
                    allTypes.put(mode, new SubsidiaryEntry(type, state));
                    enabledSubsidiaryTypes.put(point, allTypes);
                }
            }
            node.readNetwork(buffer);
        }
    }

    public void readUpdateNetwork(final ByteBuffer buffer) {
        final int size = buffer.getInt();
        final List<SignalBoxNode> allNodesForPathway = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Point point = new Point(buffer);
            final SignalBoxNode node = modeGrid.computeIfAbsent(point,
                    _u -> new SignalBoxNode(point));
            node.readNetwork(buffer);
            allNodesForPathway.add(node);
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
                    state.type.writeNetwork(buffer);
                    buffer.putByte((byte) (state.state ? 1 : 0));
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

    public void updateSubsidiarySignal(final boolean state, final ModeSet mode, final Point point,
            final SubsidiaryType type) {
        if (startsToPath.containsKey(point)) {
            OpenSignalsMain.getLogger().warn(
                    "Signal at Node [" + point + "] can't be set because it is part of a pathway!");
            return;
        }
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return;
        final Optional<BlockPos> pos = node.getOption(mode).get().getEntry(PathEntryType.SIGNAL);
        if (pos.isEmpty())
            return;
        final Signal signal = tile.getSignal(pos.get());
        if (!state) {
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
        final Map<SubsidiaryType, ConfigProperty> configs = SubsidiarySignalParser.SUBSIDIARY_SIGNALS
                .get(signal);
        if (configs == null)
            return;
        final ConfigProperty properties = configs.get(type);
        if (properties == null)
            return;
        final SignalStateInfo info = new SignalStateInfo(tile.getLevel(), pos.get(), signal);
        SignalConfig.reset(info);
        SignalStateHandler.getStates(info).keySet().forEach(property -> {
            if (!properties.values.containsKey(property)) {
                properties.values.remove(property);
            }
        });
        SignalStateHandler.setStates(info, properties.values);
        states.put(mode, new SubsidiaryEntry(type, state));
        enabledSubsidiaryTypes.put(point, states);
    }

    public boolean getSubsidiaryState(final Point point, final ModeSet mode,
            final SubsidiaryType type) {
        final Map<ModeSet, SubsidiaryEntry> states = enabledSubsidiaryTypes.get(point);
        if (states == null)
            return false;
        final SubsidiaryEntry entry = states.get(mode);
        if (entry == null)
            return false;
        if (entry.type.equals(type))
            return entry.state;
        return false;
    }

    public void setClientState(final Point point, final ModeSet mode, final boolean state,
            final SubsidiaryType type) {
        final Map<ModeSet, SubsidiaryEntry> states = enabledSubsidiaryTypes.computeIfAbsent(point,
                _u -> new HashMap<>());
        states.put(mode, new SubsidiaryEntry(type, state));
        enabledSubsidiaryTypes.put(point, states);
    }

    public Map<Point, Map<ModeSet, SubsidiaryEntry>> getAllSubsidiaries() {
        return ImmutableMap.copyOf(enabledSubsidiaryTypes);
    }
}