package eu.gir.girsignals.signalbox.debug;

import java.util.function.Consumer;

import eu.gir.girsignals.signalbox.SignalBoxGrid;
import eu.gir.girsignals.signalbox.SignalBoxUtil.ConnectionChecker;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;

public class DebugFactory extends SignalBoxFactory {

    private final boolean enableConnectionChecker = false;
    private final boolean enableDebugGrid = false;
    private final boolean enableDebugPathEntry = false;

    @Override
    public ConnectionChecker getConnectionChecker() {
        if (enableConnectionChecker)
            return new DebugChecker();
        return super.getConnectionChecker();
    }

    @Override
    public SignalBoxGrid getGrid(final Consumer<NBTTagCompound> sendToAll) {
        if (enableDebugGrid)
            return new DebugGrid(sendToAll);
        return super.getGrid(sendToAll);
    }

    @Override
    public PathOptionEntry getEntry() {
        if (enableDebugPathEntry)
            return new DebugOptionEntry();
        return super.getEntry();
    }
}
