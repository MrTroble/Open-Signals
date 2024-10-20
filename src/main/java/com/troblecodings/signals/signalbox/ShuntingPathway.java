package com.troblecodings.signals.signalbox;

import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public class ShuntingPathway extends SignalBoxPathway {

    public ShuntingPathway(final PathwayData data) {
        super(data);
    }

    @Override
    public void setPathStatus(final EnumPathUsage status, final Point point) {
        data.foreachEntry(option -> {
            option.getEntry(PathEntryType.OUTPUT)
                    .ifPresent(pos -> SignalBoxHandler.updateRedstoneOutput(
                            new StateInfo(tile.getLevel(), pos),
                            !status.equals(EnumPathUsage.FREE)));
            if (!status.equals(EnumPathUsage.FREE)) {
                option.setEntry(PathEntryType.PATHUSAGE, status);
            } else {
                option.removeEntry(PathEntryType.PATHUSAGE);
            }
        }, point);
    }

    @Override
    public void setUpPathwayStatus() {
        setPathStatus(EnumPathUsage.SHUNTING);
    }

}