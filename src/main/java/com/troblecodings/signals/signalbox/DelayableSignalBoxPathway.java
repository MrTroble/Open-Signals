package com.troblecodings.signals.signalbox;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.signals.core.BlockPosSignalHolder;
import com.troblecodings.signals.enums.EnumPathUsage;

public class DelayableSignalBoxPathway extends SignalBoxPathway {

    private final ExecutorService service = Executors.newFixedThreadPool(1);

    public DelayableSignalBoxPathway(final PathwayData data) {
        super(data);
    }

    @Override
    public void updatePathwaySignals() {
        setPathStatus(EnumPathUsage.PREPARED);
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
            final Map<BlockPosSignalHolder, OtherSignalIdentifier> distantSignalPositions = data
                    .getOtherSignals();
            this.isExecutingSignalSet = false;
            synchronized (distantSignalPositions) {
                setSignals(getLastSignalInfo());
            }
            tile.getWorld().getMinecraftServer().addScheduledTask(() -> {
                loadTileAndExecute(thisTile -> {
                    final SignalBoxPathway pw = thisTile.getSignalBoxGrid()
                            .getPathwayByLastPoint(getLastPoint());
                    pw.setPathStatus(EnumPathUsage.SELECTED);
                    pw.updatePathwayOnGrid();
                });
            });
        });
    }

    @Override
    public String toString() {
        return "DelayableSignalBoxPathway [start=" + getFirstPoint() + ", end=" + getLastPoint()
                + ", delay=" + data.getDelay() + "]";
    }
}