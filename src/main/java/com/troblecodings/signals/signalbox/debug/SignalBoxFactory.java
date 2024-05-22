package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.PathwayData;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

public class SignalBoxFactory {

    protected SignalBoxFactory() {
    }

    private static SignalBoxFactory factory = null;

    public static final SignalBoxFactory getFactory() {
        if (factory == null) {
            factory = OpenSignalsMain.isDebug() ? new DebugFactory() : new SignalBoxFactory();
        }
        return factory;
    }

    public ConnectionChecker getConnectionChecker() {
        return new ConnectionChecker();
    }

    public SignalBoxPathway getPathway(final PathwayData data) {
        return new SignalBoxPathway(data);
    }

    public SignalBoxGrid getGrid() {
        return new SignalBoxGrid();
    }

    public PathOptionEntry getEntry() {
        return new PathOptionEntry();
    }

    public PathwayData getPathwayData() {
        return new PathwayData();
    }
}