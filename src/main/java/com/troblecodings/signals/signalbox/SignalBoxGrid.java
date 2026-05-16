package com.troblecodings.signals.signalbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.core.interfaces.INetworkSaveable;
import com.troblecodings.core.interfaces.ISaveable;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.CombinedRedstoneInput;
import com.troblecodings.signals.core.NetworkBufferWrappers;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.guis.ContainerSignalBox;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.util.math.BlockPos;

public class SignalBoxGrid implements INetworkSaveable, ISaveable {

    private static final String NODE_LIST = "nodeList";
    private static final String SUBSIDIARY_LIST = "subsidiaryList";
    private static final String SUBSIDIARY_COUNTER = "subsidiaryCounter";
    private static final String PATHWAY_LIST = "pathwayList";
    private static final String NEXT_PATHWAYS = "nextPathways";
    private static final String START_POINT = "startPoint";
    private static final String END_POINT = "endPoint";
    private static final String PATH_TYPE = "pathType";

    private static final int MAX_COUNTS = 9999;

    protected final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    protected final Map<Map.Entry<Point, Point>, PathType> nextPathways = new HashMap<>();
    protected final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    protected final SignalBoxFactory factory;
    protected SignalBoxTileEntity tile;
    private int counter;
    private final SignalBoxNetworkHandler network = new SignalBoxNetworkHandler();

    public SignalBoxGrid() {
        this(SignalBoxFactory.getFactory());
    }

    public SignalBoxGrid(final SignalBoxFactory factory) {
        this.factory = factory;
    }

    public void setTile(final SignalBoxTileEntity tile) {
        this.tile = tile;
        startsToPath.values().forEach(pw -> pw.setTile(tile));
    }

    public void onLoad() {
        startsToPath.values().forEach(pw -> pw.onLoad());
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

    public boolean resetPathway(final Point p1) {
        if (startsToPath.isEmpty())
            return false;
        final SignalBoxPathway pathway = startsToPath.get(p1);
        if (pathway == null) {
            if (checkManuellResetOfProtectionWay(p1)) {
                tryNextPathways();
                return true;
            }
            OpenSignalsMain.getLogger().warn("No Pathway to reset on [" + p1 + "]!");
            return false;
        }
        resetPathway(pathway);
        tryNextPathways();
        return true;
    }

    private boolean checkManuellResetOfProtectionWay(final Point p1) {
        final SignalBoxPathway pathway = endsToPath.get(p1);
        if (pathway == null)
            return false;
        return pathway.directResetOfProtectionWay();
    }

    protected void resetPathway(final SignalBoxPathway pathway) {
        pathway.resetPathway();
        updatePrevious(pathway);
        this.startsToPath.remove(pathway.getFirstPoint());
        this.endsToPath.remove(pathway.getLastPoint());
        pathway.postReset();
    }

    public void updateMode(final Point point, final ModeSet mode) {
        final SignalBoxNode node =
                this.modeGrid.computeIfAbsent(point, p -> new SignalBoxNode(p, network));
        if (!node.has(mode)) {
            node.add(mode);
        } else {
            node.remove(mode);
        }
        node.post();
    }

    public void setUpNetwork(final ContainerSignalBox container) {
        network.setUpNetwork(container);
    }

    public void removeNetwork() {
        network.removeNetwork();
    }

    public SignalBoxNetworkHandler getNetwork() {
        return network;
    }

    public PathwayRequestResult requestWay(final Point p1, final Point p2, final PathType type) {
        final PathwayRequestResult result = SignalBoxUtil.requestPathway(this, p1, p2, type);
        if (!result.wasSuccesfull()) {
            if (result.getMode().equals(PathwayRequestMode.PASS))
                return PathwayRequestResult.getByMode(PathwayRequestMode.NO_PATH);
            return result;
        }
        final PathwayData data = result.getPathwayData();
        if (checkPathwayData(data))
            return PathwayRequestResult.getByMode(PathwayRequestMode.ALREADY_USED);
        if (data.isEmpty())
            return PathwayRequestResult.getByMode(PathwayRequestMode.NO_PATH);
        addPathway(data);
        return result;
    }

    private boolean checkPathwayData(final PathwayData data) {
        return startsToPath.containsKey(data.getFirstPoint())
                || endsToPath.containsKey(data.getLastPoint());
    }

    protected void addPathway(final PathwayData data) {
        final SignalBoxPathway way = data.createPathway();
        way.setTile(tile);
        way.deactivateAllOutputsOnPathway();
        way.setSignalBoxGrid(this);
        way.setUpPathwayStatus();
        way.updatePathwaySignals();
        onWayAdd(way);
    }

    protected void updatePrevious(final SignalBoxPathway pathway) {
        SignalBoxPathway previousPath = pathway;
        int count = 0;
        while ((previousPath = endsToPath.get(previousPath.getFirstPoint())) != null) {
            if (count > endsToPath.size()) {
                break;
            }
            previousPath.setSignals();
            count++;
        }
        if (count == 0) {
            if (OpenSignalsMain.isDebug()) {
                OpenSignalsMain.getLogger().debug("Could not find previous! " + pathway);
            }
        }
    }

    public void resetAllPathways() {
        ImmutableSet.copyOf(this.startsToPath.values()).forEach(this::resetPathway);
        clearPaths();
    }

    public void resetAllSignals() {
        this.startsToPath.values().forEach(pathway -> pathway.resetAllSignals());
    }

    private void clearPaths() {
        startsToPath.clear();
        endsToPath.clear();
        nextPathways.clear();
    }

    public List<Point> getAllInConnections() {
        return modeGrid.values().stream().filter(SignalBoxNode::containsInConnection)
                .map(SignalBoxNode::getPoint).collect(Collectors.toList());
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
        tile.setChanged();
    }

    private void tryBlock(final List<SignalBoxPathway> pathways, final BlockPos pos) {
        pathways.forEach(pathway -> {
            if (pathway.tryBlock(pos)) {
                updatePrevious(pathway);
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
                    pathway.checkReRequest();
                } else {
                    pathway.compact(optPoint.get());
                    this.startsToPath.remove(first);
                    this.startsToPath.put(pathway.getFirstPoint(), pathway);
                }
            }
            pathway.checkResetOfProtectionWay(pos);
        });
        tryNextPathways();
    }

    private boolean executingTryNextPWs = false;

    private void tryNextPathways() {
        if (executingTryNextPWs)
            return;
        executingTryNextPWs = true;
        ImmutableMap.copyOf(nextPathways).forEach((pointEntry, type) -> {
            final PathwayRequestResult request =
                    requestWay(pointEntry.getKey(), pointEntry.getValue(), type);
            if (request.wasSuccesfull()) {
                nextPathways.remove(pointEntry);
                network.sendRemoveSavedPathway(pointEntry.getKey(), pointEntry.getValue());
            }
        });
        executingTryNextPWs = false;
    }

    public Map<Map.Entry<Point, Point>, PathType> getNextPathways() {
        return ImmutableMap.copyOf(nextPathways);
    }

    public boolean addNextPathway(final Point start, final Point end, final PathType type) {
        final Map.Entry<Point, Point> entry = Maps.immutableEntry(start, end);
        if (!nextPathways.containsKey(entry)) {
            final SignalBoxPathway pw = startsToPath.get(start);
            if (pw != null && pw.isInterSignalBoxPathway())
                return false;
            nextPathways.put(entry, type);
            return true;
        }
        return false;
    }

    public SignalBoxPathway getPathwayByStartPoint(final Point start) {
        return startsToPath.get(start);
    }

    public SignalBoxPathway getPathwayByLastPoint(final Point end) {
        return endsToPath.get(end);
    }

    public void updateTrainNumber(final Point point, final TrainNumber number) {
        final SignalBoxNode node =
                modeGrid.computeIfAbsent(point, p -> new SignalBoxNode(p, network));
        startsToPath.values().forEach(pathway -> pathway.checkTrainNumberUpdate(number, node));
        tile.setChanged();
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
                    return nodeTag;
                })::iterator);
        tag.putInteger(SUBSIDIARY_COUNTER, counter);
    }

    public void writePathways(final NBTWrapper tag) {
        if (!startsToPath.isEmpty()) {
            tag.putList(PATHWAY_LIST, startsToPath.values().stream()
                    .filter(pw -> !pw.isEmptyOrBroken()).map(pathway -> {
                        final NBTWrapper path = new NBTWrapper();
                        pathway.write(path);
                        return path;
                    })::iterator);
        }
        if (!nextPathways.isEmpty()) {
            tag.putList(NEXT_PATHWAYS, nextPathways.entrySet().stream().map(entry -> {
                final NBTWrapper wrapper = new NBTWrapper();
                final NBTWrapper start = new NBTWrapper();
                entry.getKey().getKey().write(start);
                final NBTWrapper end = new NBTWrapper();
                entry.getKey().getValue().write(end);
                wrapper.putWrapper(START_POINT, start);
                wrapper.putWrapper(END_POINT, end);
                wrapper.putString(PATH_TYPE, entry.getValue().name());
                return wrapper;
            })::iterator);
        }
    }

    @Override
    public void read(final NBTWrapper tag) {
        modeGrid.clear();
        tag.getList(NODE_LIST).forEach(comp -> {
            final SignalBoxNode node = new SignalBoxNode(network);
            node.read(comp);
            modeGrid.put(node.getPoint(), node);
            final List<NBTWrapper> subsidiaryTags = comp.getList(SUBSIDIARY_LIST);
            if (subsidiaryTags == null)
                return;
            subsidiaryTags.forEach(subsidiaryTag -> {
                final ModeSet mode = new ModeSet(subsidiaryTag);
                final SubsidiaryState entry = SubsidiaryState.of(subsidiaryTag);
                node.setSubsidiaryState(mode, entry);
            });
        });
        counter = tag.getInteger(SUBSIDIARY_COUNTER);
    }

    public void readPathways(final NBTWrapper tag) {
        if (tag.contains(PATHWAY_LIST)) {
            clearPaths();
            tag.getList(PATHWAY_LIST).forEach(comp -> {
                final PathwayData data = PathwayData.of(this, comp);
                final SignalBoxPathway pathway = data.createPathway();
                pathway.setSignalBoxGrid(this);
                pathway.setTile(tile);
                pathway.read(comp);
                if (pathway.isEmptyOrBroken()) {
                    OpenSignalsMain.getLogger()
                            .error("Remove empty or broken pathway, try to recover!");
                    return;
                }
                onWayAdd(pathway);
                pathway.postRead(comp);
            });
        }
        if (tag.contains(NEXT_PATHWAYS)) {
            tag.getList(NEXT_PATHWAYS).forEach(comp -> {
                final Point start = new Point();
                start.read(comp.getWrapper(START_POINT));
                final Point end = new Point();
                end.read(comp.getWrapper(END_POINT));
                if (comp.contains(PATH_TYPE)) {
                    nextPathways.put(Maps.immutableEntry(start, end),
                            PathType.valueOf(comp.getString(PATH_TYPE)));
                } else {
                    nextPathways.put(Maps.immutableEntry(start, end),
                            SignalBoxUtil.getPathTypeFrom(modeGrid.get(start), modeGrid.get(end)));
                }
            });
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(modeGrid, tile);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalBoxGrid other = (SignalBoxGrid) obj;
        return Objects.equals(modeGrid, other.modeGrid) && Objects.equals(tile, other.tile);
    }

    @Override
    public String toString() {
        return "SignalBoxGrid [modeGrid=" + modeGrid.entrySet().stream()
                .map(entry -> entry.toString()).collect(Collectors.joining("\n")) + "]";
    }

    public SignalBoxNode getNode(final Point point) {
        return modeGrid.get(point);
    }

    public SignalBoxNode getOrCreateNode(final Point point) {
        return modeGrid.computeIfAbsent(point, p -> new SignalBoxNode(p, network));
    }

    public Optional<SignalBoxNode> getNodeChecked(final Point point) {
        return Optional.ofNullable(getNode(point));
    }

    public List<SignalBoxNode> getNodes() {
        return ImmutableList.copyOf(this.modeGrid.values());
    }

    public int getCurrentCounter() {
        return counter;
    }

    public void count() {
        setCounter(this.counter + 1);
    }

    public void setCounter(final int counter) {
        this.counter = (counter < MAX_COUNTS ? counter : 0);
        network.sendCounter();
    }

    public void setCounterFromNetwork(final int counter) {
        this.counter = counter;
    }

    protected Map<Point, SignalBoxNode> getModeGrid() {
        return modeGrid;
    }

    public void putAllNodes(final Map<Point, SignalBoxNode> nodes) {
        modeGrid.putAll(nodes);
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        modeGrid.clear();
        modeGrid.putAll(buffer.getMapWithCombinedValueFunc(
                ReadBuffer.getINetworkSaveableFunction(Point.class),
                (b, point) -> NetworkBufferWrappers.getSignalBoxNodeFunc(point, network).apply(b)));
        counter = buffer.getInt();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putINetworkSaveableMap(modeGrid);
        buffer.putInt(counter);
    }

    public void updateManuellRSOutput(final Point point, final ModeSet mode, final boolean state) {
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return;
        final PathOptionEntry entry = node.getOption(mode).get();
        final Optional<BlockPos> outputPos = entry.getEntry(PathEntryType.OUTPUT);
        final EnumPathUsage usage =
                entry.getEntry(PathEntryType.PATHUSAGE).orElse(EnumPathUsage.FREE);
        if (!outputPos.isPresent() || !usage.equals(EnumPathUsage.FREE))
            return;
        node.handleManuellEnabledOutputUpdate(mode, state);
        SignalBoxHandler.updateRedstoneOutput(new StateInfo(tile.getLevel(), outputPos.get()),
                state);
    }

    public List<Point> getAllPoints() {
        return ImmutableList.copyOf(modeGrid.keySet());
    }

    public void sendDebugPointUpdates(final List<Point> points) {
        network.sendDebugPoints(points);
    }
}