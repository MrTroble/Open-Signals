package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.enums.EnumPathUsage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBoxGrid {

    public static final String ERROR_STRING = "error";

    private final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    private final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    private final Map<SignalBoxPathway, SignalBoxPathway> previousPathways = new HashMap<>(32);
    private final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();

    public void updateModeGridFromUI(final NBTTagCompound compound) {
        this.modeGrid.entrySet().removeIf(entry -> {
            final SignalBoxNode node = entry.getValue();
            node.readEntryNetwork(compound);
            return !node.isEmpty();
        });
    }

    public void resetPathway(final NBTTagCompound compound, final Point p1) {
        final SignalBoxPathway pathway = startsToPath.get(p1);
        if (pathway == null) {
            GirsignalsMain.log.atWarn().log("Signalboxpath is null, this should not be the case!");
            return;
        }
        pathway.resetPathway();
        pathway.writeEntryNetwork(compound);
        this.startsToPath.remove(pathway.getFirstPoint());
        this.endsToPath.remove(pathway.getLastPoint());
        this.previousPathways.remove(pathway);
        this.previousPathways.entrySet().removeIf(entry -> entry.getValue().equals(pathway));
    }

    public void requestWay(final @Nullable World world, final Point p1, final Point p2) {
        final Optional<SignalBoxPathway> ways = SignalBoxUtil.requestWay(modeGrid, p1, p2);
        if (ways.isPresent()) {
            this.onWayAdd(world, ways.get());
        } else {
            final NBTTagCompound update = new NBTTagCompound();
            update.setString(ERROR_STRING, "error.nopathfound");
        }
    }

    private void onWayAdd(final @Nullable World world, final SignalBoxPathway pathway) {
        pathway.setWorld(world);
        startsToPath.put(pathway.getFirstPoint(), pathway);
        endsToPath.put(pathway.getLastPoint(), pathway);
        final SignalBoxPathway next = startsToPath.get(pathway.getLastPoint());
        if (next != null)
            previousPathways.put(next, pathway);
        final SignalBoxPathway previous = endsToPath.get(pathway.getFirstPoint());
        if (previous != null)
            previousPathways.put(pathway, previous);
        pathway.setPathStatus(EnumPathUsage.SELECTED);
        pathway.updatePathwaySignals();
        SignalBoxPathway previousPath = pathway;
        while ((previousPath = previousPathways.get(previousPath)) != null) {
            previousPath.updatePathwaySignals();
        }
    }

    public void setPowered(final BlockPos pos) {
        startsToPath.values().forEach(pathway -> pathway.tryBlock(pos));
        startsToPath.values().forEach(pathway -> pathway.tryReset(pos));
    }

}
