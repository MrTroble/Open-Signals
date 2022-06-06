package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBoxPathway implements ISaveable {

    private final ImmutableList<SignalBoxNode> listOfNodes;
    private final PathType type;
    private final int speed;
    private final Optional<Entry<BlockPos, BlockPos>> signalPositions;
    private final Map<BlockPos, SignalBoxNode> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, SignalBoxNode> mapOfBlockingPositions = new HashMap<>();
    private final Point firstPoint;
    private final Point lastPoint;

    private WorldLoadOperations loadOps = new WorldLoadOperations(null);

    /**
     * Creates a new pathway
     * 
     * @param pNodes the nodes that are contained in this pathway
     */
    public SignalBoxPathway(final List<SignalBoxNode> pNodes, final PathType type) {
        this.listOfNodes = ImmutableList.copyOf(pNodes);
        this.type = Objects.requireNonNull(type);
        if (listOfNodes.size() < 2)
            throw new IndexOutOfBoundsException();
        if (this.type.equals(PathType.NONE))
            throw new IllegalArgumentException();
        final AtomicInteger atomic = new AtomicInteger(Integer.MAX_VALUE);
        foreachEntry((optionEntry, node) -> {
            optionEntry.getEntry(PathEntryType.SPEED)
                    .ifPresent(value -> atomic.updateAndGet(in -> Math.min(in, value)));
            optionEntry.getEntry(PathEntryType.BLOCKING)
                    .ifPresent(position -> mapOfBlockingPositions.put(position, node));
            optionEntry.getEntry(PathEntryType.RESETING)
                    .ifPresent(position -> mapOfResetPositions.put(position, node));
        });
        final SignalBoxNode firstNode = pNodes.get(pNodes.size() - 1);
        this.firstPoint = firstNode.getPoint();
        final BlockPos firstPos = makeFromNext(type, firstNode, pNodes.get(pNodes.size() - 2),
                Rotation.NONE);
        final SignalBoxNode lastNode = pNodes.get(0);
        this.lastPoint = lastNode.getPoint();
        final BlockPos lastPos = makeFromNext(type, lastNode, pNodes.get(1),
                Rotation.CLOCKWISE_180);
        if (firstPos != null && lastPos != null) {
            this.signalPositions = Optional.of(Maps.immutableEntry(firstPos, lastPos));
        } else {
            this.signalPositions = Optional.empty();
        }
        this.speed = atomic.get();
    }

    private BlockPos makeFromNext(final PathType type, final SignalBoxNode first,
            final SignalBoxNode next, final Rotation pRotation) {
        final Point delta = first.getPoint().delta(next.getPoint());
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(delta).add(pRotation);
        for (final EnumGuiMode mode : type.getModes()) {
            final BlockPos possiblePosition = first.getOption(new ModeSet(mode, rotation))
                    .flatMap(option -> option.getEntry(PathEntryType.SIGNAL)).orElse(null);
            if (possiblePosition != null)
                return possiblePosition;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTTagCompound tag) {
        listOfNodes.forEach(node -> node.write(tag));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTTagCompound tag) {
        listOfNodes.forEach(node -> node.read(tag));
    }

    public void setWorld(final @Nullable World world) {
        this.loadOps = new WorldLoadOperations(world);
    }

    private void foreachEntry(final Consumer<PathOptionEntry> consumer,
            final @Nullable Point point) {
        foreachEntry((entry, _u) -> consumer.accept(entry), point);
    }

    private void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer) {
        foreachEntry(consumer, null);
    }

    private void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer,
            final @Nullable Point point) {
        for (int i = 1; i < listOfNodes.size() - 1; i++) {
            final Point oldPos = listOfNodes.get(i - 1).getPoint();
            final Point newPos = listOfNodes.get(i + 1).getPoint();
            final SignalBoxNode current = listOfNodes.get(i);
            if (current.getPoint().equals(point))
                break;
            current.getOption(new Path(oldPos, newPos))
                    .ifPresent(entry -> consumer.accept(entry, current));
        }
    }

    public void setPathStatus(final EnumPathUsage status, final @Nullable Point point) {
        foreachEntry(option -> {
            option.getEntry(PathEntryType.OUTPUT)
                    .ifPresent(pos -> loadOps.setPower(pos, !status.equals(EnumPathUsage.FREE)));
            option.setEntry(PathEntryType.PATHUSAGE, status);
        }, point);
    }

    public void setPathStatus(final EnumPathUsage status) {
        setPathStatus(status, null);
    }

    public void updatePathwaySignals() {
        this.signalPositions
                .ifPresent(entry -> loadOps.loadAndConfig(speed, entry.getKey(), entry.getValue()));
    }

    public void resetPathway() {
        resetPathway(null);
    }

    public void resetPathway(final @Nullable Point point) {
        this.signalPositions.ifPresent(entry -> loadOps.loadAndReset(entry.getKey()));
        this.setPathStatus(EnumPathUsage.FREE, point);
    }

    public boolean tryReset(final BlockPos position) {
        final SignalBoxNode node = this.mapOfResetPositions.get(position);
        if (node == null)
            return false;
        this.resetPathway(node.getPoint());
        return true;
    }

    public boolean tryBlock(final BlockPos position) {
        if (!this.mapOfBlockingPositions.containsKey(position))
            return false;
        this.setPathStatus(EnumPathUsage.BLOCKED);
        return true;
    }

    /**
     * Getter for the first point of this pathway
     * 
     * @return the firstPoint
     */
    public Point getFirstPoint() {
        return firstPoint;
    }

    /**
     * @return the lastPoint
     */
    public Point getLastPoint() {
        return lastPoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstPoint, lastPoint, listOfNodes, mapOfBlockingPositions,
                mapOfResetPositions, speed, type);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalBoxPathway other = (SignalBoxPathway) obj;
        return Objects.equals(firstPoint, other.firstPoint)
                && Objects.equals(lastPoint, other.lastPoint)
                && Objects.equals(listOfNodes, other.listOfNodes)
                && Objects.equals(mapOfBlockingPositions, other.mapOfBlockingPositions)
                && Objects.equals(mapOfResetPositions, other.mapOfResetPositions)
                && speed == other.speed && type == other.type;
    }

    @Override
    public String toString() {
        return "SignalBoxPathway [type=" + type + ", speed=" + speed + ", signalPositions="
                + signalPositions + ", mapOfResetPositions=" + mapOfResetPositions
                + ", mapOfBlockingPositions=" + mapOfBlockingPositions + ", firstPoint="
                + firstPoint + ", lastPoint=" + lastPoint + "]";
    }

}
