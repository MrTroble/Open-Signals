package com.troblecodings.signals.signalbox;

import java.nio.ByteBuffer;
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
import com.troblecodings.signals.core.BufferBuilder;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SignalBoxGrid implements INetworkSavable {

    private static final String NODE_LIST = "nodeList";
    private static final String PATHWAY_LIST = "pathwayList";

    protected final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    protected final Consumer<NBTWrapper> sendToAll;
    protected final SignalBoxFactory factory;
    private Level world;
    private BlockPos boxPos;

    public SignalBoxGrid(final Consumer<NBTWrapper> sendToAll) {
        this.sendToAll = sendToAll;
        this.factory = SignalBoxFactory.getFactory();
    }

    public void setWorldAndPos(final Level world, final BlockPos pos) {
        this.world = world;
        this.boxPos = pos;
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
        pathway.resetPathway();
        updatePrevious(pathway);
        this.startsToPath.remove(pathway.getFirstPoint());
        this.endsToPath.remove(pathway.getLastPoint());
    }

    public boolean requestWay(final Point p1, final Point p2) {
        if (startsToPath.containsKey(p1) || endsToPath.containsKey(p2))
            return false;
        final Optional<SignalBoxPathway> ways = SignalBoxUtil.requestWay(modeGrid, p1, p2);
        ways.ifPresent(way -> {
            way.setWorld(world);
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
        if (world == null || boxPos == null) {
            OpenSignalsMain.getLogger()
                    .warn("World or Pos are null for this grid. This shouldn't be the case!");
            pathway.resetPathway();
            startsToPath.remove(pathway.getFirstPoint());
            endsToPath.remove(pathway.getLastPoint());
            return;
        }
        final SignalBoxTileEntity tile = (SignalBoxTileEntity) world.getBlockEntity(boxPos);
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
        tag.putList(PATHWAY_LIST, startsToPath.values().stream()
                .filter(SignalBoxPathway::isEmptyOrBroken).map(pathway -> {
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
            node.readNetwork(buffer);
            node.post();
        }
    }

    public void writeToBuffer(final BufferBuilder buffer) {
        buffer.putInt(modeGrid.size());
        modeGrid.forEach((point, node) -> {
            point.writeToBuffer(buffer);
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
        buffer.putInt(modeGrid.size());
        modeGrid.forEach((point, node) -> {
            point.writeNetwork(buffer);
            node.writeNetwork(buffer);
        });
    }
}