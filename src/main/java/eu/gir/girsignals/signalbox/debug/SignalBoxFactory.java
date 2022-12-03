package eu.gir.girsignals.signalbox.debug;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import eu.gir.girsignals.SignalsMain;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxGrid;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.girsignals.signalbox.SignalBoxPathway;
import eu.gir.girsignals.signalbox.WorldLoadOperations;
import eu.gir.girsignals.signalbox.WorldOperations;
import eu.gir.girsignals.signalbox.SignalBoxUtil.ConnectionChecker;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class SignalBoxFactory {

    protected SignalBoxFactory() {
    }

    private static SignalBoxFactory factory = null;

    public static final SignalBoxFactory getFactory() {
        if (factory == null) {
            factory = SignalsMain.isDebug() ? new DebugFactory() : new SignalBoxFactory();
        }
        return factory;
    }

    public ConnectionChecker getConnectionChecker() {
        return new ConnectionChecker();
    }

    public WorldOperations getWorldOperations(final @Nullable World world) {
        if (world == null)
            return new WorldOperations();
        return new WorldLoadOperations(world);
    }

    public SignalBoxPathway getPathway(final Map<Point, SignalBoxNode> modeGrid,
            final List<SignalBoxNode> pNodes, final PathType type) {
        return new SignalBoxPathway(modeGrid, pNodes, type);
    }

    public SignalBoxPathway getPathway(final Map<Point, SignalBoxNode> modeGrid) {
        return new SignalBoxPathway(modeGrid);
    }

    public SignalBoxGrid getGrid(final Consumer<NBTTagCompound> sendToAll) {
        return new SignalBoxGrid(sendToAll);
    }

    public PathOptionEntry getEntry() {
        return new PathOptionEntry();
    }
}
