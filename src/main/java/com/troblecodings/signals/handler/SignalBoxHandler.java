package com.troblecodings.signals.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RedstonePacket;
import com.troblecodings.signals.signalbox.GridComponent;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SignalBoxHandler {

    private static final Map<BlockPos, GridComponent> ALL_GRIDS = new HashMap<>();
    private static final Map<BlockPos, Map<BlockPos, Signal>> ALL_SIGNALS = new HashMap<>();
    private static final Map<BlockPos, List<BlockPos>> UNLINK_POS = new HashMap<>();
    private static final Map<BlockPos, List<BlockPos>> LINK_POS = new HashMap<>();
    // private static final Map<BlockPos, Boolean> OUTPUT_UPDATES = new HashMap<>();
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    public static void resetPathway(final BlockPos tilePos, final Point point) {
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.resetPathway(point);
    }

    public static boolean requestPathway(final BlockPos tilePos, final Point p1, final Point p2,
            final Map<Point, SignalBoxNode> modeGrid) {
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return false;
        return grid.requestWay(p1, p2, modeGrid);
    }

    public static void resetAllPathways(final BlockPos tilePos) {
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.resetAllPathways();
    }

    public static void updateInput(final BlockPos tilePos, final RedstonePacket update) {
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.setPowered(update.pos);
    }

    public static GridComponent computeIfAbsent(final BlockPos tilePos, final Level world) {
        if (world.isClientSide)
            return null;
        return ALL_GRIDS.computeIfAbsent(tilePos, _u -> new GridComponent(world, tilePos));
    }

    public static void readFromNBT(final BlockPos tilePos, final NBTWrapper wrapper,
            final Map<Point, SignalBoxNode> modeGrid) {
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.read(wrapper, modeGrid);
    }

    public static void writeToNBT(final BlockPos tilePos, final NBTWrapper wrapper) {
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.write(wrapper);
    }

    public static void setWorld(final BlockPos tilePos, final Level world) {
        if (world.isClientSide)
            return;
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.setWorld(world);
    }

    public static void addSignal(final BlockPos tilePos, final Signal signal,
            final BlockPos signalPos, final Level world) {
        SERVICE.execute(() -> {
            if (world.isClientSide)
                return;
            final Map<BlockPos, Signal> signals;
            synchronized (ALL_SIGNALS) {
                signals = ALL_SIGNALS.computeIfAbsent(tilePos, _u -> new HashMap<>());
            }
            signals.put(signalPos, signal);
        });
    }

    public static boolean containsSignal(final BlockPos pos, final BlockPos signalPos) {
        final Map<BlockPos, Signal> signals;
        synchronized (ALL_SIGNALS) {
            signals = ALL_SIGNALS.get(pos);
        }
        if (signals == null)
            return false;
        return signals.containsKey(signalPos);
    }

    public static Signal getSignal(final BlockPos tilePos, final BlockPos signalPos) {
        final Map<BlockPos, Signal> signals;
        synchronized (ALL_SIGNALS) {
            signals = ALL_SIGNALS.get(tilePos);
        }
        if (signals == null)
            return null;
        return signals.get(signalPos);
    }

    public static Signal removeSignal(final BlockPos tilePos, final BlockPos signalPos,
            final Level world) {
        final Map<BlockPos, Signal> signals;
        synchronized (ALL_SIGNALS) {
            signals = ALL_SIGNALS.get(tilePos);
        }
        if (signals == null)
            return null;
        return signals.remove(signalPos);
    }

    public static void onSignalRemoved(final BlockPos signalPos) {
        // TODO Add system to remove and place new signal
        /*
         * SERVICE.execute(() -> { synchronized (ALL_SIGNALS) { ALL_SIGNALS.forEach((_u,
         * map) -> { map.remove(signalPos); }); } System.out.println(); });
         */
    }

    public static Map<BlockPos, Signal> clearSignals(final BlockPos tilePos) {
        final Map<BlockPos, Signal> signals;
        synchronized (ALL_SIGNALS) {
            signals = ALL_SIGNALS.remove(tilePos);
        }
        return signals == null ? new HashMap<>() : signals;
    }

    public static Map<BlockPos, Signal> getSignals(final BlockPos tilePos) {
        final Map<BlockPos, Signal> signals;
        synchronized (ALL_SIGNALS) {
            signals = ALL_SIGNALS.get(tilePos);
        }
        return signals == null ? new HashMap<>() : ImmutableMap.copyOf(signals);
    }

    public static void setSignals(final BlockPos tilePos, final Map<BlockPos, Signal> signals) {
        synchronized (ALL_SIGNALS) {
            ALL_SIGNALS.put(tilePos, signals);
        }
    }

    public static void unlinkPosFromTile(final BlockPos pos, final BlockPos tilePos,
            final Level world) {
        if (tryDirectUnlink(world, pos, tilePos))
            return;
        final List<BlockPos> removePos = UNLINK_POS.computeIfAbsent(pos, _u -> new ArrayList<>());
        removePos.add(tilePos);
        final List<BlockPos> newPos = LINK_POS.get(pos);
        if (newPos != null && newPos.contains(tilePos))
            newPos.remove(tilePos);
    }

    public static void linkTileToPos(final BlockPos pos, final BlockPos tilePos,
            final Level world) {
        if (tryDirectLink(world, pos, tilePos))
            return;
        final List<BlockPos> linkPos = LINK_POS.computeIfAbsent(tilePos, _u -> new ArrayList<>());
        linkPos.add(tilePos);
    }

    private static boolean tryDirectLink(final Level world, final BlockPos pos,
            final BlockPos posToLink) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null && entity instanceof BasicBlockEntity) {
            ((RedstoneIOTileEntity) entity).link(posToLink);
            return true;
        }
        return false;
    }

    private static boolean tryDirectUnlink(final Level world, final BlockPos pos,
            final BlockPos posToUnlink) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null && entity instanceof BasicBlockEntity) {
            ((RedstoneIOTileEntity) entity).unlink(posToUnlink);
            return true;
        }
        return false;
    }

    public static List<BlockPos> removeUnlinkPos(final BlockPos ioPos) {
        final List<BlockPos> unlinkPos = UNLINK_POS.remove(ioPos);
        return unlinkPos == null ? new ArrayList<>() : unlinkPos;
    }

    public static List<BlockPos> removeLinkPos(final BlockPos ioPos) {
        final List<BlockPos> positions = LINK_POS.remove(ioPos);
        return positions == null ? new ArrayList<>() : positions;
    }
}