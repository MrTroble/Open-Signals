package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.CombinedRedstoneInput;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class PathwayHolder implements IChunkLoadable {

    private static final String PATHWAY_LIST = "pathwayList";
    private static final String NEXT_PATHWAYS = "nextPathways";
    private static final String START_POINT = "startPoint";
    private static final String END_POINT = "endPoint";

    protected final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    protected final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    protected final List<Map.Entry<Point, Point>> nextPathways = new ArrayList<>();
    private Level world;
    private final BlockPos tilePos;
    private Map<Point, SignalBoxNode> modeGrid = new HashMap<>();

    public PathwayHolder(final Level world, final BlockPos pos) {
        this.world = world;
        this.tilePos = pos;
    }

    public void setWorld(final Level world) {
        this.world = world;
        startsToPath.values().forEach(pw -> pw.setWorldAndPos(world, tilePos));
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

    public void updateModeGrid(final SignalBoxGrid grid) {
        this.modeGrid = grid.modeGrid;
    }

    public boolean isValidStart(final Point point) {
        checkTileWasLoaded();
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return false;
        return node.isValidStart();
    }

    public boolean isValidEnd(final Point point) {
        checkTileWasLoaded();
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null)
            return false;
        return node.isValidEnd();
    }

    public List<Point> getValidStarts() {
        checkTileWasLoaded();
        return modeGrid.values().stream().filter(SignalBoxNode::isValidStart)
                .map(SignalBoxNode::getPoint).collect(Collectors.toUnmodifiableList());
    }

    public List<Point> getValidEnds() {
        checkTileWasLoaded();
        return modeGrid.values().stream().filter(SignalBoxNode::isValidEnd)
                .map(SignalBoxNode::getPoint).collect(Collectors.toUnmodifiableList());
    }

    public List<Point> getAllInConnections() {
        checkTileWasLoaded();
        return modeGrid.values().stream().filter(SignalBoxNode::containsInConnection)
                .map(SignalBoxNode::getPoint).collect(Collectors.toUnmodifiableList());
    }

    public SignalBoxNode getNode(final Point point) {
        checkTileWasLoaded();
        return modeGrid.getOrDefault(point, new SignalBoxNode());
    }

    public List<MainSignalIdentifier> getGreenSignals() {
        final List<MainSignalIdentifier> returnList = new ArrayList<>();
        startsToPath.values().forEach(pathway -> returnList.addAll(pathway.getGreenSignals()));
        return returnList;
    }

    public boolean requestWay(final Point p1, final Point p2) {
        if (startsToPath.containsKey(p1) || endsToPath.containsKey(p2))
            return false;
        checkTileWasLoaded();
        final Optional<SignalBoxPathway> ways = SignalBoxUtil.requestWay(modeGrid, p1, p2);
        ways.ifPresent(way -> {
            way.setWorldAndPos(world, tilePos);
            way.deactivateAllOutputsOnPathway();
            way.setUpdater(pathway -> {
                updatePrevious(pathway);
                updateToNet(pathway);
            });
            way.setPathwayHolder(this);
            way.setPathStatus(EnumPathUsage.SELECTED);
            way.updatePathwaySignals();
            this.onWayAdd(way);
            updateToNet(way);
        });
        return ways.isPresent();
    }

    private void checkTileWasLoaded() {
        if (modeGrid == null || modeGrid.isEmpty()) {
            loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world, tilePos,
                    (tile, _u) -> {
                    });
        }
    }

    public SignalBoxPathway getPathwayByLastPoint(final Point end) {
        return endsToPath.get(end);
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
            previousPath.updatePathwaySignals();
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
                final SignalBoxTileEntity tile = (SignalBoxTileEntity) world
                        .getBlockEntity(tilePos);
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

    public void removeNextPathway(final Point start, final Point end) {
        nextPathways.remove(Maps.immutableEntry(start, end));
    }

    public void resetPathway(final Point p1) {
        if (startsToPath.isEmpty())
            return;
        final SignalBoxPathway pathway = startsToPath.get(p1);
        if (pathway == null) {
            OpenSignalsMain.getLogger().warn("No Pathway to reset on [" + p1 + "]!");
            return;
        }
        resetPathway(pathway);
        updateToNet(pathway);
        tryNextPathways();
    }

    protected void resetPathway(final SignalBoxPathway pathway) {
        pathway.resetPathway();
        updatePrevious(pathway);
        this.startsToPath.remove(pathway.getFirstPoint());
        this.endsToPath.remove(pathway.getLastPoint());
    }

    private void updateToNet(final SignalBoxPathway pathway) {
        final SignalBoxTileEntity tile = (SignalBoxTileEntity) world.getBlockEntity(tilePos);
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

    public void read(final NBTWrapper tag) {
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
            pathway.setPathwayHolder(this);
            pathway.setWorldAndPos(world, tilePos);
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

    public void write(final NBTWrapper tag) {
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
}