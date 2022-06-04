package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import eu.gir.girsignals.signalbox.entrys.ISaveable;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SignalBoxNode implements ISaveable {

    private final Point point;
    private final String identifier;
    private final HashMap<Path, ModeSet> possibleConnections = new HashMap<>();
    private final HashMap<ModeSet, PathOptionEntry> possibleModes = new HashMap<>();

    public SignalBoxNode(final Point point) {
        this.point = Objects.requireNonNull(point);
        this.identifier = point.getX() + "." + point.getY();
    }

    public void add(final ModeSet modeSet) {
        possibleModes.put(modeSet, new PathOptionEntry());
    }

    public boolean has(final ModeSet modeSet) {
        return possibleModes.containsKey(modeSet);
    }

    public void remove(final ModeSet modeSet) {
        possibleModes.remove(modeSet);
    }

    public void post() {
        possibleModes.forEach((e, i) -> {
            final Point p1 = new Point(this.point);
            final Point p2 = new Point(this.point);
            switch (e.mode) {
                case CORNER:
                    switch (e.rotation) {
                        case NONE:
                            p1.translate(0, 1);
                            p2.translate(-1, 0);
                            break;
                        case CLOCKWISE_90:
                            p1.translate(0, -1);
                            p2.translate(-1, 0);
                            break;
                        case CLOCKWISE_180:
                            p1.translate(0, -1);
                            p2.translate(1, 0);
                            break;
                        case COUNTERCLOCKWISE_90:
                            p1.translate(0, 1);
                            p2.translate(1, 0);
                            break;
                        default:
                            return;
                    }
                    break;
                case STRAIGHT:
                case END:
                    switch (e.rotation) {
                        case NONE:
                        case CLOCKWISE_180:
                            p1.translate(1, 0);
                            p2.translate(-1, 0);
                            break;
                        case CLOCKWISE_90:
                        case COUNTERCLOCKWISE_90:
                            p1.translate(0, 1);
                            p2.translate(0, -1);
                            break;
                        default:
                            return;
                    }
                    break;
                default:
                    return;
            }
            possibleConnections.put(new Path(p1, p2), e);
        });
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public void write(final NBTTagCompound compound) {
        if (possibleModes.isEmpty()) {
            compound.removeTag(this.identifier);
            return;
        }
        final NBTTagList pointList = new NBTTagList();
        possibleModes.forEach((mode, option) -> pointList.appendTag(mode.writeToNBT(option)));
        compound.setTag(this.identifier, pointList);
    }

    @Override
    public void read(final NBTTagCompound compound) {
        if (!compound.hasKey(this.identifier))
            return;
        final NBTTagList pointList = (NBTTagList) compound.getTag(this.identifier);
        pointList.forEach(e -> {
            final PathOptionEntry entry = new PathOptionEntry();
            possibleModes.put(ModeSet.readFromNBT(entry, compound), entry);
        });
    }

    public Optional<PathOptionEntry> getOption(final ModeSet mode) {
        return Optional.ofNullable(possibleModes.get(mode));
    }

    public Optional<PathOptionEntry> getOption(final Optional<ModeSet> mode) {
        if (mode.isPresent())
            return getOption(mode.get());
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Node[point=" + this.point + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(point);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalBoxNode other = (SignalBoxNode) obj;
        return Objects.equals(point, other.point);
    }

    /**
     * Get's the identifier of the given node
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

}
