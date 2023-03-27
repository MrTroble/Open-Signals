package com.troblecodings.signals.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RedstonePacket;
import com.troblecodings.signals.signalbox.GridComponent;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SignalBoxHandler {

    private static final Map<BlockPos, GridComponent> ALL_GRIDS = new HashMap<>();
    private static final Map<BlockPos, Map<BlockPos, Signal>> ALL_SIGNALS = new HashMap<>();
    private static final Map<BlockPos, List<BlockPos>> REMOVED_POS_FROM_INPUT = new HashMap<>();
    private static final Map<BlockPos, List<BlockPos>> NEW_LINKED_POS = new HashMap<>();

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
        final GridComponent grid = ALL_GRIDS.get(tilePos);
        if (grid == null)
            return;
        grid.setWorld(world);
    }

    public static void addSignal(final BlockPos tilePos, final Signal signal,
            final BlockPos signalPos) {
        final Map<BlockPos, Signal> signals = ALL_SIGNALS.computeIfAbsent(tilePos,
                _u -> new HashMap<>());
        signals.put(signalPos, signal);
        ALL_SIGNALS.put(tilePos, signals);
    }

    public static Signal getSignal(final BlockPos tilePos, final BlockPos signalPos) {
        final Map<BlockPos, Signal> signals = ALL_SIGNALS.get(tilePos);
        if (signals == null)
            return null;
        return signals.get(signalPos);
    }

    public static Signal removeSignal(final BlockPos tilePos, final BlockPos signalPos) {
        final Map<BlockPos, Signal> signals = ALL_SIGNALS.get(tilePos);
        if (signals == null)
            return null;
        return signals.remove(signalPos);
    }

    public static Map<BlockPos, Signal> clearSignals(final BlockPos tilePos) {
        final Map<BlockPos, Signal> signals = ALL_SIGNALS.get(tilePos);
        if (signals == null)
            return new HashMap<>();
        return ImmutableMap.copyOf(ALL_SIGNALS.remove(tilePos));
    }

    public static Map<BlockPos, Signal> getSignals(final BlockPos tilePos) {
        final Map<BlockPos, Signal> signals = ALL_SIGNALS.get(tilePos);
        if (signals == null)
            return new HashMap<>();
        return ImmutableMap.copyOf(ALL_SIGNALS.get(tilePos));
    }

    public static void setSignals(final BlockPos tilePos, final Map<BlockPos, Signal> signals) {
        ALL_SIGNALS.put(tilePos, signals);
    }

    public static void unlinkInputPos(final BlockPos ioPos, final BlockPos posToRemove,
            final Level world) {
        final BlockEntity entity = world.getBlockEntity(ioPos);
        if (entity != null) {
            ((RedstoneIOTileEntity) entity).unlink(posToRemove);
            return;
        }
        final List<BlockPos> removePos = REMOVED_POS_FROM_INPUT.computeIfAbsent(ioPos,
                _u -> new ArrayList<>());
        removePos.add(posToRemove);
        REMOVED_POS_FROM_INPUT.put(ioPos, removePos);
        final List<BlockPos> newPos = NEW_LINKED_POS.get(ioPos);
        if (newPos != null && newPos.contains(posToRemove)) {
            newPos.remove(posToRemove);
            NEW_LINKED_POS.put(ioPos, newPos);
        }
    }

    public static List<BlockPos> getUnlinkedPos(final BlockPos ioPos) {
        final List<BlockPos> positions = REMOVED_POS_FROM_INPUT.get(ioPos);
        if (positions == null)
            return new ArrayList<>();
        return ImmutableList.copyOf(REMOVED_POS_FROM_INPUT.remove(ioPos));
    }

    public static void linkRedstoneInput(final BlockPos ioPos, final BlockPos tilePos,
            final Level world) {
        final BlockEntity entity = world.getBlockEntity(ioPos);
        if (entity != null) {
            ((RedstoneIOTileEntity) entity).link(tilePos);
            return;
        }
    }

    public static List<BlockPos> getNewLinkedPos(final BlockPos ioPos) {
        final List<BlockPos> positions = NEW_LINKED_POS.get(ioPos);
        if (positions == null)
            return new ArrayList<>();
        return ImmutableList.copyOf(NEW_LINKED_POS.get(ioPos));
    }
}