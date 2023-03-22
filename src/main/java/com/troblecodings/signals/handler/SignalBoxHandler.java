package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RedstonePacket;
import com.troblecodings.signals.signalbox.GridComponent;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class SignalBoxHandler {

    private static final Map<BlockPos, GridComponent> ALL_GRIDS = new HashMap<>();
    private static final Map<BlockPos, Map<BlockPos, Signal>> ALL_SIGNALS = new HashMap<>();

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
        return ALL_SIGNALS.remove(tilePos);
    }

    public static Map<BlockPos, Signal> getSignals(final BlockPos tilePos) {
        return ALL_SIGNALS.get(tilePos);
    }

    public static boolean containsTilePos(final BlockPos tilePos) {
        return ALL_SIGNALS.containsKey(tilePos);
    }

    public static void setSignals(final BlockPos tilePos, final Map<BlockPos, Signal> signals) {
        ALL_SIGNALS.put(tilePos, signals);
    }
}