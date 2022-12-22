package com.troblecodings.signals.signalbox.debug;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.nbt.CompoundTag;

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

    public SignalBoxPathway getPathway(final Map<Point, SignalBoxNode> modeGrid,
            final List<SignalBoxNode> pNodes, final PathType type) {
        return new SignalBoxPathway(modeGrid, pNodes, type);
    }

    public SignalBoxPathway getPathway(final Map<Point, SignalBoxNode> modeGrid) {
        return new SignalBoxPathway(modeGrid);
    }

    public SignalBoxGrid getGrid(final Consumer<CompoundTag> sendToAll) {
        return new SignalBoxGrid(sendToAll);
    }

    public PathOptionEntry getEntry() {
        return new PathOptionEntry();
    }
}
