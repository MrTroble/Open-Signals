package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBoxGrid implements ISaveable {

    public static final String ERROR_STRING = "error";

    private static final String NODE_LIST = "nodeList";
    private static final String PATHWAY_LIST = "pathwayList";

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

    @Override
    public void write(final NBTTagCompound tag) {
        final NBTTagList list = new NBTTagList();
        modeGrid.values().forEach(node -> {
            final NBTTagCompound nodeTag = new NBTTagCompound();
            node.write(nodeTag);
            list.appendTag(nodeTag);
        });
        tag.setTag(NODE_LIST, list);
        final NBTTagList pathList = new NBTTagList();
        startsToPath.values().forEach(pathway -> {
            final NBTTagCompound path = new NBTTagCompound();
            pathway.write(path);
            pathList.appendTag(path);
        });
        tag.setTag(PATHWAY_LIST, pathList);
    }

    private void clearPaths() {
        previousPathways.clear();
        startsToPath.clear();
        endsToPath.clear();
    }

    @Override
    public void read(final NBTTagCompound tag) {
        clearPaths();
        modeGrid.clear();
        final NBTTagList nodes = (NBTTagList) tag.getTag(NODE_LIST);
        nodes.forEach(comp -> {
            final SignalBoxNode node = new SignalBoxNode();
            node.read(tag);
            modeGrid.put(node.getPoint(), node);
        });
        final NBTTagList list = (NBTTagList) tag.getTag(PATHWAY_LIST);
        list.forEach(comp -> {
            final SignalBoxPathway pathway = new SignalBoxPathway(this.modeGrid);
            pathway.read(tag);
            onWayAdd(null, pathway);
        });
    }

    @Override
    public int hashCode() {
        return Objects.hash(endsToPath, modeGrid, previousPathways, startsToPath);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalBoxGrid other = (SignalBoxGrid) obj;
        return Objects.equals(endsToPath, other.endsToPath)
                && Objects.equals(modeGrid, other.modeGrid)
                && Objects.equals(previousPathways, other.previousPathways)
                && Objects.equals(startsToPath, other.startsToPath);
    }

    @Override
    public String toString() {
        return "SignalBoxGrid [modeGrid=" + modeGrid + "]";
    }

    public boolean isEmpty() {
        return this.modeGrid.isEmpty();
    }
}
