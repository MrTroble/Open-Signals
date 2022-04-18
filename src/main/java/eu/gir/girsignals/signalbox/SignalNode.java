package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import eu.gir.girsignals.signalbox.PathOption.EnumPathUsage;
import eu.gir.guilib.ecs.interfaces.UIAutoSync;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;

public class SignalNode implements UIAutoSync, Iterable<PathOption> {

    private static final String MODE = "mode";
    private static final String ROTATION = "rotation";
    private static final String OPTION = "option";

    private final Point point;
    private HashMap<Entry<Point, Point>, Entry<EnumGuiMode, Rotation>> possibleConnections = new HashMap<>();
    private HashMap<Entry<EnumGuiMode, Rotation>, PathOption> possibleModes = new HashMap<>();

    public SignalNode(final Point point2) {
        this.point = point2;
    }

    public void add(EnumGuiMode mode, Rotation rot) {
        possibleModes.put(Maps.immutableEntry(mode, rot), new PathOption());
    }

    public boolean has(EnumGuiMode mode, Rotation rot) {
        return possibleModes.containsKey(Maps.immutableEntry(mode, rot));
    }

    public void remove(EnumGuiMode mode, Rotation rot) {
        possibleModes.remove(Maps.immutableEntry(mode, rot));
    }

    public void post() {
        possibleModes.forEach((e, i) -> {
            final Point p1 = new Point(this.point);
            final Point p2 = new Point(this.point);
            switch (e.getKey()) {
                case CORNER:
                    switch (e.getValue()) {
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
                    switch (e.getValue()) {
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
            possibleConnections.put(Maps.immutableEntry(p1, p2), e);
        });
    }

    public Point getPoint() {
        return point;
    }

    public Set<Entry<Point, Point>> connections() {
        return this.possibleConnections.keySet();
    }

    public void forEach(BiConsumer<Entry<EnumGuiMode, Rotation>, PathOption> applier) {
        possibleModes.forEach(applier);
    }

    public boolean isEmpty() {
        return possibleModes.isEmpty();
    }

    @Override
    public void write(NBTTagCompound compound) {
        if (possibleModes.isEmpty()) {
            compound.removeTag(this.getID());
            return;
        }
        final NBTTagList pointList = new NBTTagList();
        possibleModes.forEach((mode, option) -> {
            final NBTTagCompound entry = new NBTTagCompound();
            entry.setString(MODE, mode.getKey().name());
            entry.setString(ROTATION, mode.getValue().name());
            entry.setTag(OPTION, option.writeNBT());
            pointList.appendTag(entry);
        });
        compound.setTag(this.getID(), pointList);
    }

    @Override
    public void read(NBTTagCompound compound) {
        if (!compound.hasKey(getID()))
            return;
        final NBTTagList pointList = (NBTTagList) compound.getTag(getID());
        pointList.forEach(e -> {
            final NBTTagCompound entry = (NBTTagCompound) e;
            final EnumGuiMode mode = EnumGuiMode.valueOf(entry.getString(MODE));
            final Rotation rotation = Rotation.valueOf(entry.getString(ROTATION));
            final Entry<EnumGuiMode, Rotation> modeRotation = Maps.immutableEntry(mode, rotation);
            possibleModes.put(modeRotation, new PathOption(entry.getCompoundTag(OPTION)));
        });
    }

    @Override
    public String getID() {
        return point.getX() + "." + point.getY();
    }

    @Override
    public void setID(String id) {
    }

    public Optional<PathOption> getOption(final EnumGuiMode mode) {
        final Optional<Entry<Entry<EnumGuiMode, Rotation>, PathOption>> opt = this.possibleModes
                .entrySet().stream().filter(e -> e.getKey().getKey().equals(mode)).findFirst();
        if (opt.isPresent()) {
            return Optional.of(opt.get().getValue());
        } else {
            return Optional.empty();
        }
    }

    public Optional<PathOption> getOption(final EnumGuiMode guimode, final Rotation rotation) {
        final Entry<EnumGuiMode, Rotation> entry = Maps.immutableEntry(guimode, rotation);
        return getOption(entry);
    }

    public Optional<PathOption> getOption(final Entry<EnumGuiMode, Rotation> entry) {
        if (entry == null || !this.possibleModes.containsKey(entry))
            return Optional.empty();
        return Optional.of(this.possibleModes.get(entry));
    }

    public Optional<PathOption> getOption(final Point p1, final Point p2) {
        final Entry<Point, Point> entry1 = Maps.immutableEntry(p1, p2);
        if (this.possibleConnections.containsKey(entry1)) {
            return getOption(this.possibleConnections.get(entry1));
        }
        final Entry<Point, Point> entry2 = Maps.immutableEntry(p2, p1);
        if (this.possibleConnections.containsKey(entry2)) {
            return getOption(this.possibleConnections.get(entry2));
        }
        return Optional.empty();
    }

    public List<Rotation> getRotations(final EnumGuiMode mode) {
        return this.possibleModes.keySet().stream().filter(entry -> entry.getKey().equals(mode))
                .map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    public boolean isUsed() {
        return !this.possibleModes.values().stream()
                .allMatch(option -> option.getPathUsage().equals(EnumPathUsage.FREE));
    }

    public boolean has(EnumGuiMode mode) {
        return this.possibleModes.keySet().stream().anyMatch(e -> e.getKey().equals(mode));
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalNode other = (SignalNode) obj;
        return Objects.equals(point, other.point);
    }

    @Override
    public Iterator<PathOption> iterator() {
        return possibleModes.values().iterator();
    }

}
