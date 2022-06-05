package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SignalBoxNode implements ISaveable, Iterable<ModeSet> {

    private static final Set<EnumGuiMode> VALID_MODES = ImmutableSet.of(EnumGuiMode.HP,
            EnumGuiMode.RS, EnumGuiMode.RA10, EnumGuiMode.END);

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
        possibleModes.forEach((mode, option) -> {
            final NBTTagCompound entry = new NBTTagCompound();
            mode.write(entry);
            option.write(entry);
            pointList.appendTag(entry);
        });
        compound.setTag(this.identifier, pointList);
    }

    @Override
    public void read(final NBTTagCompound compound) {
        if (!compound.hasKey(this.identifier))
            return;
        final NBTTagList pointList = (NBTTagList) compound.getTag(this.identifier);
        pointList.forEach(e -> {
            final NBTTagCompound tag = (NBTTagCompound) e;
            final PathOptionEntry entry = new PathOptionEntry();
            entry.read(tag);
            possibleModes.put(new ModeSet(tag), entry);
        });
        this.post();
    }

    public Optional<PathOptionEntry> getOption(final Path path) {
        return getOption(Optional.ofNullable(possibleConnections.get(path)));
    }

    public Optional<PathOptionEntry> getOption(final ModeSet mode) {
        return Optional.ofNullable(possibleModes.get(mode));
    }

    public Optional<PathOptionEntry> getOption(final Optional<ModeSet> mode) {
        if (mode.isPresent())
            return getOption(mode.get());
        return Optional.empty();
    }

    /**
     * Get's the identifier of the given node
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    public PathType getPathType(final SignalBoxNode other) {
        final Set<PathType> pathTypes = other.possibleModes.keySet().stream()
                .map(mode -> PathType.of(mode.mode)).collect(Collectors.toSet());
        return this.possibleModes.keySet().stream().map(mode -> PathType.of(mode.mode))
                .filter(pathTypes::contains)
                .min((t1, t2) -> Integer.min(t1.ordinal(), t2.ordinal())).orElse(PathType.NONE);
    }

    public boolean canMakePath(final Path path, final PathType type) {
        final ModeSet modeSet = this.possibleConnections.get(path);
        if (modeSet == null)
            return false;
        for (final EnumGuiMode mode : type.getModes()) {
            final ModeSet possibleOverStepping = new ModeSet(mode, modeSet.rotation);
            if (this.possibleModes.containsKey(possibleOverStepping))
                return false; // Found another signal on the path that is not the target
        }
        return true;
    }

    public boolean isEmpty() {
        return this.possibleModes.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, possibleModes);
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
        return Objects.equals(point, other.point)
                && Objects.equals(possibleModes, other.possibleModes);
    }

    @Override
    public String toString() {
        return "SignalBoxNode [point=" + point + ", possibleConnections=" + possibleConnections
                + ", possibleModes=" + possibleModes + "]";
    }

    public boolean isValidStart() {
        return this.possibleModes.keySet().stream()
                .anyMatch(modeSet -> VALID_MODES.contains(modeSet.mode));
    }

    public Set<Path> connections() {
        return ImmutableSet.copyOf(this.possibleConnections.keySet());
    }

    @Override
    public Iterator<ModeSet> iterator() {
        return this.possibleModes.keySet().iterator();
    }

}
