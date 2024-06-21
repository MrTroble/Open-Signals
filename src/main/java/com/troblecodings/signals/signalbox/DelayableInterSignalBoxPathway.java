package com.troblecodings.signals.signalbox;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.signals.enums.EnumPathUsage;

import net.minecraft.util.math.BlockPos;

public class DelayableInterSignalBoxPathway extends InterSignalBoxPathway {

    private final ExecutorService service = Executors.newFixedThreadPool(1);

    public DelayableInterSignalBoxPathway(final PathwayData data) {
        super(data);
    }

    @Override
    public void updatePathwaySignals() {
        setPathStatus(EnumPathUsage.PREPARED);
        if (pathwayToBlock != null) {
            pathwayToBlock.loadTileAndExecute(_u -> {
                pathwayToBlock.isExecutingSignalSet = true;
                pathwayToBlock.setPathStatus(EnumPathUsage.PREPARED);
                pathwayToBlock.updateToNet();
            });
        }
        if (isExecutingSignalSet)
            return;
        this.isExecutingSignalSet = true;
        service.execute(() -> {
            try {
                Thread.sleep(data.getDelay() * 1000);
            } catch (final InterruptedException e) {
            }
            if (isEmptyOrBroken()) {
                return;
            }
            final Map<BlockPos, OtherSignalIdentifier> distantSignalPositions = data
                    .getOtherSignals();
            this.isExecutingSignalSet = false;
            pathwayToBlock.isExecutingSignalSet = false;
            synchronized (distantSignalPositions) {
                setSignals(getLastSignalInfo());
            }
            tile.getWorld().getMinecraftServer().addScheduledTask(() -> {
                loadTileAndExecute(thisTile -> {
                    final SignalBoxPathway pw = thisTile.getSignalBoxGrid()
                            .getPathwayByLastPoint(getLastPoint());
                    pw.setPathStatus(EnumPathUsage.SELECTED);
                    pw.updateToNet();
                });
                if (pathwayToBlock != null) {
                    pathwayToBlock.loadTileAndExecute(otherTile -> {
                        pathwayToBlock = (DelayableInterSignalBoxPathway) otherTile
                                .getSignalBoxGrid()
                                .getPathwayByLastPoint(pathwayToBlock.getLastPoint());
                        pathwayToBlock.setPathStatus(EnumPathUsage.SELECTED);
                        pathwayToBlock.updateToNet();
                    });
                }
            });
        });
    }

    @Override
    public String toString() {
        return "DelayableInterSignalBoxPathway [start=" + getFirstPoint() + ", end="
                + getLastPoint() + ", delay=" + data.getDelay() + "]";
    }
}