package com.troblecodings.signals.signalbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.CombinedRedstoneInput;
import com.troblecodings.signals.core.BufferFactory;
import com.troblecodings.signals.core.RedstonePacket;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class GridComponent {

    private static final String PATHWAY_LIST = "pathwayList";

    private final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    private final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    private Level world;
    private final BlockPos tilePos;

    public GridComponent(final Level world, final BlockPos pos) {
        this.world = world;
        this.tilePos = pos;
    }

    public void setWorld(final Level world) {
        this.world = world;
        startsToPath.values().forEach(pw -> pw.setWorldAndPos(world, tilePos));
    }

    private void onWayAdd(final SignalBoxPathway pathway) {
        startsToPath.put(pathway.getFirstPoint(), pathway);
        endsToPath.put(pathway.getLastPoint(), pathway);
        updatePrevious(pathway);
    }

    public boolean requestWay(final Point p1, final Point p2,
            final Map<Point, SignalBoxNode> modeGrid) {
        if (startsToPath.containsKey(p1) || endsToPath.containsKey(p2))
            return false;
        final Optional<SignalBoxPathway> ways = SignalBoxUtil.requestWay(modeGrid, p1, p2);
        ways.ifPresent(way -> {
            way.setWorldAndPos(world, tilePos);
            way.disableAllOutputs();
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
            previousPath.updatePathwaySignals();
            count++;
        }
        if (count == 0) {
            OpenSignalsMain.getLogger().debug("Could not find previous! " + pathway);
        }
    }

    public void resetAllPathways() {
        this.startsToPath.values().forEach(pathway -> {
            pathway.resetPathway();
        });
        clearPaths();
    }

    private void clearPaths() {
        startsToPath.clear();
        endsToPath.clear();
    }

    public void updateInput(final RedstonePacket update) {
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
                } else {
                    updateToNet(pathway);
                    pathway.compact(optPoint.get());
                    this.startsToPath.remove(first);
                    this.startsToPath.put(pathway.getFirstPoint(), pathway);
                }
            }
        });
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
    }

    private void resetPathway(final SignalBoxPathway pathway) {
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
        final BufferFactory buffer = new BufferFactory();
        buffer.putByte((byte) SignalBoxNetwork.SEND_PW_UPDATE.ordinal());
        buffer.putInt(nodes.size());
        nodes.forEach(node -> {
            node.getPoint().writeNetwork(buffer);
            node.writeUpdateNetwork(buffer);
        });
        OpenSignalsMain.network.sendTo(tile.get(0).getPlayer(), buffer.build());
    }

    public void read(final NBTWrapper tag, final Map<Point, SignalBoxNode> modeGrid) {
        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        if (!tag.contains(PATHWAY_LIST))
            return;
        clearPaths();
        tag.getList(PATHWAY_LIST).forEach(comp -> {
            final SignalBoxPathway pathway = factory.getPathway(modeGrid);
            pathway.read(comp);
            if (pathway.isEmptyOrBroken()) {
                OpenSignalsMain.getLogger()
                        .error("Remove empty or broken pathway, try to recover!");
                return;
            }
            pathway.setWorldAndPos(world, tilePos);
            onWayAdd(pathway);
        });
    }

    public void write(final NBTWrapper tag) {
        tag.putList(PATHWAY_LIST,
                startsToPath.values().stream().filter(pw -> !pw.isEmptyOrBroken()).map(pathway -> {
                    final NBTWrapper path = new NBTWrapper();
                    pathway.write(path);
                    return path;
                })::iterator);
    }
}