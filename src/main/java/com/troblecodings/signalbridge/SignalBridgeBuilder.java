package com.troblecodings.signalbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.core.Vec3i;

public class SignalBridgeBuilder {

    private final Map<Point, SignalBridgeBasicBlock> pointForBlocks = new HashMap<>();
    private final Map<Vec3i, Signal> vecForSignal = new HashMap<>();
    private Point startPoint = null;

    public void changeStartPoint(final Point newPoint) {
        this.startPoint = newPoint;
    }

    public void addBlock(final Point point, final SignalBridgeBasicBlock block) {
        pointForBlocks.put(point, block);
    }

    public SignalBridgeBasicBlock getBlockOnPoint(final Point point) {
        return pointForBlocks.get(point);
    }

    public void removeBridgeBlock(final Point point) {
        pointForBlocks.remove(point);
    }

    public List<Map.Entry<Vec3i, BasicBlock>> getRelativesToStart() {
        final List<Map.Entry<Vec3i, BasicBlock>> relativeToStart = new ArrayList<>();
        if (startPoint == null)
            return relativeToStart;
        final Vec3i startVec = new Vec3i(startPoint.getX(), startPoint.getY(), 0);
        pointForBlocks.forEach((point, block) -> {
            final Vec3i vector = new Vec3i(point.getX(), point.getY(), 0);
            relativeToStart.add(Maps.immutableEntry(startVec.subtract(vector), block));
        });
        vecForSignal.forEach((vec, signal) -> relativeToStart
                .add(Maps.immutableEntry(startVec.subtract(vec), signal)));
        return relativeToStart;
    }
}
