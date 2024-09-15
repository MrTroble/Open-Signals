package com.troblecodings.signals.signalbox;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.signals.core.BlockPosSignalHolder;
import com.troblecodings.signals.enums.EnumPathUsage;

public class DelayableShuntingPathway extends ShuntingPathway {

    private final ExecutorService service = Executors.newFixedThreadPool(1);

    public DelayableShuntingPathway(final PathwayData data) {
        super(data);
    }

    @Override
    public void setUpPathwayStatus() {
        setPathStatus(EnumPathUsage.PREPARED);
    }

    @Override
    public void updatePathwaySignals() {
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
                    pw.setPathStatus(EnumPathUsage.SHUNTING);
                    pw.updatePathwayOnGrid();
                });
            });
        });
    }

    @Override
    public String toString() {
        return "DelayableShuntingPathway [start=" + getFirstPoint() + ", end=" + getLastPoint()
                + ", delay=" + data.getDelay() + "]";
    }

}
