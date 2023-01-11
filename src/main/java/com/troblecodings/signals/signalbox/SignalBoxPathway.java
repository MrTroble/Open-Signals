package com.troblecodings.signals.signalbox;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.signalbox.config.ConfigInfo;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class SignalBoxPathway implements INetworkSavable {

    private final Map<BlockPos, SignalBoxNode> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, SignalBoxNode> mapOfBlockingPositions = new HashMap<>();
    private ImmutableList<SignalBoxNode> listOfNodes = ImmutableList.of();
    private PathType type = PathType.NONE;
    private Point firstPoint = new Point();
    private Point lastPoint = new Point();
    private int speed = -1;
    private Optional<Entry<BlockPos, BlockPos>> signalPositions = Optional.empty();
    private Optional<BlockPos> lastSignal = Optional.empty();
    private ImmutableList<BlockPos> distantSignalPositions = ImmutableList.of();
    private WorldOperations loadOps = new WorldOperations();
    private Map<Point, SignalBoxNode> modeGrid = null;
    private boolean emptyOrBroken = false;

    public SignalBoxPathway(final Map<Point, SignalBoxNode> modeGrid) {
        this.modeGrid = modeGrid;
    }

    public SignalBoxPathway(final Map<Point, SignalBoxNode> modeGrid,
            final List<SignalBoxNode> pNodes, final PathType type) {
        this(modeGrid);
        this.listOfNodes = ImmutableList.copyOf(pNodes);
        this.type = Objects.requireNonNull(type);
        if (listOfNodes.size() < 2)
            throw new IndexOutOfBoundsException();
        if (this.type.equals(PathType.NONE))
            throw new IllegalArgumentException();
        initalize();
    }

    private void initalize() {
        final AtomicInteger atomic = new AtomicInteger(Integer.MAX_VALUE);
        final Builder<BlockPos> distantPosBuilder = ImmutableList.builder();
        foreachEntry((optionEntry, node) -> {
            optionEntry.getEntry(PathEntryType.SPEED)
                    .ifPresent(value -> atomic.updateAndGet(in -> Math.min(in, value)));
            optionEntry.getEntry(PathEntryType.BLOCKING)
                    .ifPresent(position -> mapOfBlockingPositions.put(position, node));
            optionEntry.getEntry(PathEntryType.RESETING)
                    .ifPresent(position -> mapOfResetPositions.put(position, node));
        });
        foreachPath((path, node) -> {
            final Rotation rotation = SignalBoxUtil
                    .getRotationFromDelta(node.getPoint().delta(path.point1));
            for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.VP, EnumGuiMode.RS)) {
                node.getOption(new ModeSet(mode, rotation))
                        .ifPresent(option -> option.getEntry(PathEntryType.SIGNAL)
                                .ifPresent(position -> distantPosBuilder.add(position)));
            }
        }, null);
        this.distantSignalPositions = distantPosBuilder.build();
        final SignalBoxNode firstNode = this.listOfNodes.get(this.listOfNodes.size() - 1);
        this.firstPoint = firstNode.getPoint();
        final BlockPos firstPos = makeFromNext(type, firstNode,
                this.listOfNodes.get(this.listOfNodes.size() - 2), Rotation.NONE);
        final SignalBoxNode lastNode = this.listOfNodes.get(0);
        this.lastPoint = lastNode.getPoint();
        final BlockPos lastPos = makeFromNext(type, lastNode, this.listOfNodes.get(1),
                Rotation.CLOCKWISE_180);
        if (lastPos != null) {
            lastSignal = Optional.of(lastPos);
        }
        if (firstPos != null) {
            this.signalPositions = Optional.of(Maps.immutableEntry(firstPos, lastPos));
        } else {
            this.signalPositions = Optional.empty();
        }
        this.speed = atomic.get();
    }

    private BlockPos makeFromNext(final PathType type, final SignalBoxNode first,
            final SignalBoxNode next, final Rotation pRotation) {
        final Point delta = first.getPoint().delta(next.getPoint());
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(delta).getRotated(pRotation);
        for (final EnumGuiMode mode : type.getModes()) {
            final BlockPos possiblePosition = first.getOption(new ModeSet(mode, rotation))
                    .flatMap(option -> option.getEntry(PathEntryType.SIGNAL)).orElse(null);
            if (possiblePosition != null)
                return possiblePosition;
        }
        return null;
    }

    private static final String LIST_OF_NODES = "listOfNodes";
    private static final String PATH_TYPE = "pathType";

    @Override
    public void write(final NBTWrapper tag) {
        tag.putList(LIST_OF_NODES, listOfNodes.stream().map(node -> {
            final NBTWrapper entry = new NBTWrapper();
            node.getPoint().write(entry);
            return entry;
        })::iterator);
        tag.putString(PATH_TYPE, this.type.name());
    }

    @Override
    public void read(final NBTWrapper tag) {
        final Builder<SignalBoxNode> nodeBuilder = ImmutableList.builder();
        tag.getList(LIST_OF_NODES).forEach(nodeNBT -> {
            final Point point = new Point();
            point.read(nodeNBT);
            final SignalBoxNode node = modeGrid.get(point);
            if (node == null) {
                OpenSignalsMain.log.error("Detecting broken pathway at {}!", point.toString());
                this.emptyOrBroken = true;
                return;
            }
            nodeBuilder.add(node);
        });
        this.listOfNodes = nodeBuilder.build();
        this.type = PathType.valueOf(tag.getString(PATH_TYPE));
        if (this.listOfNodes.size() < 2) {
            OpenSignalsMain.log.error("Detecting pathway with only 2 elements!");
            this.emptyOrBroken = true;
            return;
        }
        this.initalize();
    }

    private void foreachEntry(final Consumer<PathOptionEntry> consumer,
            final @Nullable Point point) {
        foreachEntry((entry, _u) -> consumer.accept(entry), point);
    }

    private void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer) {
        foreachEntry(consumer, null);
    }

    private void foreachPath(final BiConsumer<Path, SignalBoxNode> consumer,
            final @Nullable Point point) {
        for (int i = listOfNodes.size() - 2; i > 0; i--) {
            final Point oldPos = listOfNodes.get(i - 1).getPoint();
            final Point newPos = listOfNodes.get(i + 1).getPoint();
            final SignalBoxNode current = listOfNodes.get(i);
            consumer.accept(new Path(oldPos, newPos), current);
            if (current.getPoint().equals(point))
                break;
        }

    }

    private void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer,
            final @Nullable Point point) {
        foreachPath((path, current) -> current.getOption(path)
                .ifPresent(entry -> consumer.accept(entry, current)), point);
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

    private void configUpdate(final ConfigInfo info) {
        info.type = this.type;
    }

    public void updatePathwaySignals() {
        this.signalPositions.ifPresent(entry -> {
            loadOps.loadAndConfig(speed, entry.getKey(), entry.getValue(), this::configUpdate);
        });
        distantSignalPositions.forEach(position -> loadOps.loadAndConfig(speed, position,
                lastSignal.orElse(null), this::configUpdate));
    }

    public void resetPathway() {
        resetPathway(null);
    }

    private void resetFirstSignal() {
        this.signalPositions.ifPresent(entry -> loadOps.loadAndReset(entry.getKey()));
    }

    private void resetOther() {
        distantSignalPositions.forEach(position -> loadOps.loadAndReset(position));
    }

    public void resetPathway(final @Nullable Point point) {
        this.setPathStatus(EnumPathUsage.FREE, point);
        resetFirstSignal();
        if (point == null || point.equals(this.getLastPoint())
                || point.equals(this.listOfNodes.get(1).getPoint())) {
            this.emptyOrBroken = true;
            resetOther();
        }
    }

    public void compact(final Point point) {
        foreachEntry(entry -> entry.getEntry(PathEntryType.SIGNAL).ifPresent(loadOps::loadAndReset),
                point);
        this.listOfNodes = ImmutableList.copyOf(this.listOfNodes.subList(0,
                this.listOfNodes.indexOf(this.modeGrid.get(point)) + 1));
        this.initalize();
    }

    public Optional<Point> tryReset(final BlockPos position) {
        final SignalBoxNode node = this.mapOfResetPositions.get(position);
        if (node == null)
            return Optional.empty();
        final Point point = node.getPoint();
        final AtomicBoolean atomic = new AtomicBoolean(false);
        foreachEntry((option, cNode) -> {
            option.getEntry(PathEntryType.BLOCKING).ifPresent(pos -> {
                if (loadOps.isPowered(pos))
                    atomic.set(true);
            });
        }, point);
        if (atomic.get())
            return Optional.empty();
        this.resetPathway(point);
        return Optional.of(point);
    }

    public boolean tryBlock(final BlockPos position) {
        if (!this.mapOfBlockingPositions.containsKey(position))
            return false;
        resetFirstSignal();
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
        return Objects.hash(firstPoint, lastPoint, listOfNodes, modeGrid, type);
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
                && Objects.equals(modeGrid, other.modeGrid) && type == other.type;
    }

    @Override
    public String toString() {
        return "SignalBoxPathway [start=" + firstPoint + ", end=" + lastPoint + "]";
    }

    /**
     * @return the listOfNodes
     */
    public ImmutableList<SignalBoxNode> getListOfNodes() {
        return listOfNodes;
    }

    /**
     * @return the emptyOrBroken
     */
    public boolean isEmptyOrBroken() {
        return emptyOrBroken;
    }

    @Override
    public void readNetwork(ByteBuffer buffer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeNetwork(ByteBuffer buffer) {
        // TODO Auto-generated method stub
        
    }

}
