package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.CombinedRedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SubsidiarySignalParser;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;
import com.troblecodings.signals.signalbox.config.ResetInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.core.BlockPos;

public class SignalBoxGrid implements INetworkSavable {

    private static final String NODE_LIST = "nodeList";
    private static final String SUBSIDIARY_LIST = "subsidiaryList";
    private static final String SUBSIDIARY_COUNTER = "subsidiaryCounter";
    private static final String PATHWAY_LIST = "pathwayList";
    private static final String NEXT_PATHWAYS = "nextPathways";
    private static final String START_POINT = "startPoint";
    private static final String END_POINT = "endPoint";

    protected final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    protected final List<Map.Entry<Point, Point>> nextPathways = new ArrayList<>();
    protected final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    protected final SignalBoxFactory factory;
    private final Map<Point, Map<ModeSet, SubsidiaryEntry>> enabledSubsidiaryTypes = new HashMap<>();
    private int counter;
    private SignalBoxTileEntity tile;

    public SignalBoxGrid() {
        this.factory = SignalBoxFactory.getFactory();
    }

    public void setTile(final SignalBoxTileEntity tile) {
        this.tile = tile;
        startsToPath.values().forEach(pw -> pw.setTile(tile));
    }

    public void onLoad() {
        startsToPath.values().forEach(pw -> pw.linkPathways());
    }

    public void updatePathwayToAutomatic(final Point point) {
        final SignalBoxPathway pathway = startsToPath.get(point);
        if (pathway == null) {
            OpenSignalsMain.getLogger().warn("No pathway to update automatic at [" + point + "]!");
            return;
        }
        pathway.updatePathwayToAutomatic();
    }

    private void onWayAdd(final SignalBoxPathway pathway) {
        startsToPath.put(pathway.getFirstPoint(), pathway);
        endsToPath.put(pathway.getLastPoint(), pathway);
        updatePrevious(pathway);
    }

    public List<MainSignalIdentifier> getGreenSignals() {
        final List<MainSignalIdentifier> returnList = new ArrayList<>();
        startsToPath.values().forEach(pathway -> returnList.addAll(pathway.getGreenSignals()));
        return returnList;
    }

    public boolean resetPathway(final Point p1) {
        enabledSubsidiaryTypes.remove(p1);
        if (startsToPath.isEmpty())
            return false;
        final SignalBoxPathway pathway = startsToPath.get(p1);
        if (pathway == null) {
            OpenSignalsMain.getLogger().warn("No Pathway to reset on [" + p1 + "]!");
            return false;
        }
        resetPathway(pathway);
        updateToNet(pathway);
        tryNextPathways();
        return true;
    }

    protected void resetPathway(final SignalBoxPathway pathway) {
        pathway.resetPathway();
        updatePrevious(pathway);
        this.startsToPath.remove(pathway.getFirstPoint());
        this.endsToPath.remove(pathway.getLastPoint());
    }

    private void updateToNet(final SignalBoxPathway pathway) {
        if (tile == null || !tile.isBlocked())
            return;
        final List<SignalBoxNode> nodes = pathway.getListOfNodes();
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_PW_UPDATE);
        buffer.putInt(nodes.size());
        nodes.forEach(node -> {
            node.getPoint().writeNetwork(buffer);
            node.writeUpdateNetwork(buffer);
        });
        OpenSignalsMain.network.sendTo(tile.get(0).getPlayer(), buffer);
    }

    public boolean requestWay(final Point p1, final Point p2) {
        if (startsToPath.containsKey(p1) || endsToPath.containsKey(p2))
            return false;
        final Optional<SignalBoxPathway> ways = SignalBoxUtil.requestWay(modeGrid, p1, p2);
        ways.ifPresent(way -> {
            way.setTile(tile);
            way.deactivateAllOutputsOnPathway();
            way.setUpdater(pathway -> {
                updatePrevious(pathway);
                updateToNet(pathway);
            });
            way.setSignalBoxGrid(this);
            way.setPathStatus(EnumPathUsage.SELECTED);
            way.updatePathwaySignals();
            this.onWayAdd(way);
            updateToNet(way);
        });
        return ways.isPresent();
    }

    private void updatePrevious(final SignalBoxPathway pathway) {
        SignalBoxPathway previousPath = pathway;
        int count = 0;
        while ((previousPath = endsToPath.get(previousPath.getFirstPoint())) != null) {
            if (count > endsToPath.size()) {
                OpenSignalsMain.getLogger().error("Detected signalpath cycle, aborting!");
                startsToPath.values().forEach(path -> {
                    path.resetPathway();
                    updateToNet(path);
                });
                this.clearPaths();
                break;
            }
            previousPath.setSignals();
            count++;
        }
        if (count == 0) {
            if (OpenSignalsMain.isDebug())
                OpenSignalsMain.getLogger().debug("Could not find previous! " + pathway);
        }
    }

    public void resetAllPathways() {
        this.startsToPath.values().forEach(pathway -> pathway.resetPathway());
        clearPaths();
    }

    private void clearPaths() {
        startsToPath.clear();
        endsToPath.clear();
        nextPathways.clear();
    }

    public List<Point> getAllInConnections() {
        return modeGrid.values().stream().filter(SignalBoxNode::containsInConnection)
                .map(SignalBoxNode::getPoint).collect(Collectors.toUnmodifiableList());
    }

    public List<Point> getValidStarts() {
        return modeGrid.values().stream().filter(SignalBoxNode::isValidStart)
                .map(SignalBoxNode::getPoint).collect(Collectors.toUnmodifiableList());
    }

    public List<Point> getValidEnds() {
        return modeGrid.values().stream().filter(SignalBoxNode::isValidEnd)
                .map(SignalBoxNode::getPoint).collect(Collectors.toUnmodifiableList());
    }

    public void updateInput(final RedstoneUpdatePacket update) {
        final List<SignalBoxPathway> nodeCopy = ImmutableList.copyOf(startsToPath.values());
        if (update.block instanceof CombinedRedstoneInput) {
            if (update.state) {
                tryBlock(nodeCopy, update.pos);
            } else {
                tryReset(nodeCopy, update.pos);
            }
        } else {
            tryBlock(nodeCopy, update.pos);
            tryReset(nodeCopy, update.pos);
        }
    }

    private void tryBlock(final List<SignalBoxPathway> pathways, final BlockPos pos) {
        pathways.forEach(pathway -> {
            if (pathway.tryBlock(pos)) {
                updatePrevious(pathway);
                updateToNet(pathway);
            }
        });
    }

    private void tryReset(final List<SignalBoxPathway> pathways, final BlockPos pos) {
        pathways.forEach(pathway -> {
            final Point first = pathway.getFirstPoint();
            final Optional<Point> optPoint = pathway.tryReset(pos);
            if (optPoint.isPresent()) {
                if (pathway.isEmptyOrBroken()) {
                    resetPathway(pathway);
                    updateToNet(pathway);
                    pathway.checkReRequest();
                } else {
                    updateToNet(pathway);
                    pathway.compact(optPoint.get());
                    this.startsToPath.remove(first);
                    this.startsToPath.put(pathway.getFirstPoint(), pathway);
                }
            }
        });
        tryNextPathways();
    }

    private void tryNextPathways() {
        nextPathways.removeIf(entry -> {
            final boolean bool = requestWay(entry.getKey(), entry.getValue());
            if (bool) {
                if (tile == null || !tile.isBlocked())
                    return bool;
                final WriteBuffer buffer = new WriteBuffer();
                buffer.putEnumValue(SignalBoxNetwork.REMOVE_SAVEDPW);
                entry.getKey().writeNetwork(buffer);
                entry.getValue().writeNetwork(buffer);
                OpenSignalsMain.network.sendTo(tile.get(0).getPlayer(), buffer);
            }
            return bool;
        });
        if (startsToPath.isEmpty())
            nextPathways.clear();
    }

    public List<Map.Entry<Point, Point>> getNextPathways() {
        return ImmutableList.copyOf(nextPathways);
    }

    public boolean addNextPathway(final Point start, final Point end) {
        final Map.Entry<Point, Point> entry = Maps.immutableEntry(start, end);
        if (!nextPathways.contains(entry)) {
            nextPathways.add(entry);
            return true;
        }
        return false;
    }

    public SignalBoxPathway getPathwayByLastPoint(final Point end) {
        return endsToPath.get(end);
    }

    public void updateTrainNumber(final SignalBoxNode node, final TrainNumber number) {
        startsToPath.values().forEach(pathway -> pathway.checkTrainNumberUpdate(number, node));
    }

    public void removeNextPathway(final Point start, final Point end) {
        nextPathways.remove(Maps.immutableEntry(start, end));
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putList(NODE_LIST,
                modeGrid.values().stream().filter(node -> !node.isEmpty()).map(node -> {
                    final NBTWrapper nodeTag = new NBTWrapper();
                    node.write(nodeTag);
                    final Map<ModeSet, SubsidiaryEntry> subsidiaries = enabledSubsidiaryTypes
                            .get(node.getPoint());
                    if (subsidiaries == null)
                        return nodeTag;
                    nodeTag.putList(SUBSIDIARY_LIST, subsidiaries.entrySet().stream().map(entry -> {
                        final NBTWrapper subsidiaryTag = new NBTWrapper();
                        entry.getKey().write(subsidiaryTag);
                        entry.getValue().writeNBT(subsidiaryTag);
                        return subsidiaryTag;
                    })::iterator);
                    return nodeTag;
                })::iterator);
        tag.putInteger(SUBSIDIARY_COUNTER, counter);
    }

    public void writePathways(final NBTWrapper tag) {
        tag.putList(PATHWAY_LIST,
                startsToPath.values().stream().filter(pw -> !pw.isEmptyOrBroken()).map(pathway -> {
                    final NBTWrapper path = new NBTWrapper();
                    pathway.write(path);
                    return path;
                })::iterator);
        tag.putList(NEXT_PATHWAYS, nextPathways.stream().map(entry -> {
            final NBTWrapper wrapper = new NBTWrapper();
            final NBTWrapper start = new NBTWrapper();
            entry.getKey().write(start);
            final NBTWrapper end = new NBTWrapper();
            entry.getValue().write(end);
            wrapper.putWrapper(START_POINT, start);
            wrapper.putWrapper(END_POINT, end);
            return wrapper;
        })::iterator);
    }

    @Override
    public void read(final NBTWrapper tag) {
        modeGrid.clear();
        enabledSubsidiaryTypes.clear();
        tag.getList(NODE_LIST).forEach(comp -> {
            final SignalBoxNode node = new SignalBoxNode();
            node.read(comp);
            modeGrid.put(node.getPoint(), node);
            final List<NBTWrapper> subsidiaryTags = comp.getList(SUBSIDIARY_LIST);
            if (subsidiaryTags == null)
                return;
            final Map<ModeSet, SubsidiaryEntry> states = new HashMap<>();
            subsidiaryTags.forEach(subsidiaryTag -> {
                final ModeSet mode = new ModeSet(subsidiaryTag);
                states.put(mode, SubsidiaryEntry.of(subsidiaryTag));
            });
            enabledSubsidiaryTypes.put(node.getPoint(), states);
        });
        counter = tag.getInteger(SUBSIDIARY_COUNTER);
    }

    public void readPathways(final NBTWrapper tag) {
        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        if (!tag.contains(PATHWAY_LIST))
            return;
        clearPaths();
        tag.getList(PATHWAY_LIST).forEach(comp -> {
            final SignalBoxPathway pathway = factory.getPathway(modeGrid);
            pathway.setUpdater(way -> {
                updatePrevious(way);
                updateToNet(way);
            });
            pathway.setSignalBoxGrid(this);
            pathway.setTile(tile);
            pathway.read(comp);
            if (pathway.isEmptyOrBroken()) {
                OpenSignalsMain.getLogger()
                        .error("Remove empty or broken pathway, try to recover!");
                return;
            }
            onWayAdd(pathway);
            pathway.readLinkedPathways(comp);
        });
        tag.getList(NEXT_PATHWAYS).forEach(comp -> {
            final Point start = new Point();
            start.read(comp.getWrapper(START_POINT));
            final Point end = new Point();
            end.read(comp.getWrapper(END_POINT));
            nextPathways.add(Maps.immutableEntry(start, end));
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

    public SignalBoxNode getNode(final Point point) {
        return modeGrid.get(point);
    }

    public List<SignalBoxNode> getNodes() {
        return ImmutableList.copyOf(this.modeGrid.values());
    }

    public int getCurrentCounter() {
        return counter;
    }

    public void countOne() {
        if (counter < 9999) {
            counter++;
        } else {
            counter = 0;
        }
    }

    public void setCurrentCounter(final int counter) {
        if (counter < 9999) {
            this.counter = counter;
        } else {
            this.counter = 0;
        }
    }

    protected Map<Point, SignalBoxNode> getModeGrid() {
        return modeGrid;
    }

    public void putNode(final Point point, final SignalBoxNode node) {
        modeGrid.put(point, node);
    }

    public void putAllNodes(final Map<Point, SignalBoxNode> nodes) {
        modeGrid.putAll(nodes);
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        modeGrid.clear();
        enabledSubsidiaryTypes.clear();
        final int size = buffer.getInt();
        for (int i = 0; i < size; i++) {
            final Point point = Point.of(buffer);
            final SignalBoxNode node = new SignalBoxNode(point);
            final int enabledSubsidariesSize = buffer.getByteToUnsignedInt();
            if (enabledSubsidariesSize != 0) {
                for (int j = 0; j < enabledSubsidariesSize; j++) {
                    final Map<ModeSet, SubsidiaryEntry> allTypes = enabledSubsidiaryTypes
                            .computeIfAbsent(point, _u -> new HashMap<>());
                    final ModeSet mode = ModeSet.of(buffer);
                    final SubsidiaryEntry type = SubsidiaryEntry.of(buffer);
                    allTypes.put(mode, type);
                    enabledSubsidiaryTypes.put(point, allTypes);
                }
            }
            node.readNetwork(buffer);
            modeGrid.put(point, node);
        }
        counter = buffer.getInt();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putInt(modeGrid.size());
        modeGrid.forEach((point, node) -> {
            point.writeNetwork(buffer);
            final Map<ModeSet, SubsidiaryEntry> enabledSubsidiaries = enabledSubsidiaryTypes
                    .get(point);
            if (enabledSubsidiaries == null) {
                buffer.putByte((byte) 0);
            } else {
                buffer.putByte((byte) enabledSubsidiaries.size());
                enabledSubsidiaries.forEach((mode, state) -> {
                    mode.writeNetwork(buffer);
                    state.writeNetwork(buffer);
                });
            }
            node.writeNetwork(buffer);
        });
        buffer.putInt(counter);
    }

    @SuppressWarnings("resource")
    public List<SignalBoxNode> readUpdateNetwork(final ReadBuffer buffer, final boolean override) {
        final int size = buffer.getInt();
        final List<SignalBoxNode> allNodesForPathway = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Point point = Point.of(buffer);
            SignalBoxNode node;
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
        if (!tile.getLevel().isClientSide)
            return new ArrayList<>();
        return allNodesForPathway;
    }

    public BlockPos updateManuellRSOutput(final Point point, final ModeSet mode,
            final boolean state) {
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return null;
        final PathOptionEntry entry = node.getOption(mode).get();
        final Optional<BlockPos> outputPos = entry.getEntry(PathEntryType.OUTPUT);
        final Optional<EnumPathUsage> usage = entry.getEntry(PathEntryType.PATHUSAGE);
        if (outputPos.isEmpty() || (usage.isPresent() && !usage.get().equals(EnumPathUsage.FREE)))
            return null;
        if (state) {
            node.addManuellOutput(mode);
        } else {
            node.removeManuellOutput(mode);
        }
        return outputPos.get();
    }

    public void updateSubsidiarySignal(final Point point, final ModeSet mode,
            final SubsidiaryEntry entry) {
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return;
        final Optional<BlockPos> pos = node.getOption(mode).get().getEntry(PathEntryType.SIGNAL);
        if (pos.isEmpty())
            return;
        final Signal signal = SignalBoxHandler
                .getSignal(new StateInfo(tile.getLevel(), tile.getBlockPos()), pos.get());
        if (!entry.state) {
            if (!enabledSubsidiaryTypes.containsKey(point))
                return;
            SignalConfig.reset(
                    new ResetInfo(new SignalStateInfo(tile.getLevel(), pos.get(), signal), false));
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
        SignalStateHandler.runTaskWhenSignalLoaded(info, (stateInfo, oldProperties, _u) -> {
            SignalStateHandler.setStates(info, properties.state.entrySet().stream()
                    .filter(propertyEntry -> oldProperties.containsKey(propertyEntry.getKey()))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)));
        });
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