package com.troblecodings.signalbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.core.Vec3i;

public class SignalBridgeBuilder {

    private final Map<Point, SignalBridgeBasicBlock> pointForBlocks = new HashMap<>();
    private final Map<Vec3i, Signal> vecForSignal = new HashMap<>();
    private final List<Map.Entry<Vec3i, BasicBlock>> relativeToStart = new ArrayList<>();
    private Point startPoint = null;

    public void changeStartPoint(final Point newPoint) {
        if (newPoint.equals(startPoint))
            return;
        this.startPoint = newPoint;
        updateRelativesToStart();
    }

    private void updateRelativesToStart() {
        relativeToStart.clear();
        if (startPoint == null)
            return;
        final Vec3i startVec = new Vec3i(0, startPoint.getY(), startPoint.getX());
        pointForBlocks.forEach((point, block) -> {
            final Vec3i vector = new Vec3i(0, point.getY(), point.getX());
            relativeToStart.add(Maps.immutableEntry(startVec.offset(vector), block));
        });
        vecForSignal.forEach((vec, signal) -> relativeToStart
                .add(Maps.immutableEntry(startVec.offset(vec), signal)));
    }

    public List<Map.Entry<Vec3i, BasicBlock>> getRelativesToStart() {
        return ImmutableList.copyOf(relativeToStart);
    }

}
