package com.troblecodings.signals.signalbox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class PathwayData {

    public static final PathwayData EMPTY_DATA = new PathwayData();

    private static final String LIST_OF_NODES = "listOfNodes";
    private static final String PATH_TYPE = "pathType";

    protected SignalBoxGrid grid = null;
    private final Map<BlockPos, SignalBoxNode> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, SignalBoxNode> mapOfBlockingPositions = new HashMap<>();
    private List<SignalBoxNode> listOfNodes = ImmutableList.of();
    private PathType type = PathType.NONE;
    private Point firstPoint = new Point(-1, -1);
    private Point lastPoint = new Point(-1, -1);
    private int speed = -1;
    private String zs2Value = "";
    private int delay = 0;
    private Optional<MainSignalIdentifier> startSignal = Optional.empty();
    private Optional<MainSignalIdentifier> endSignal = Optional.empty();
    private Map<BlockPos, OtherSignalIdentifier> otherSignals = ImmutableMap.of();
    private boolean emptyOrBroken = false;

    private SignalBoxPathway pathway;

    public static PathwayData of(final SignalBoxGrid grid, final List<SignalBoxNode> pNodes,
            final PathType type) {
        final PathwayData data = SignalBoxFactory.getFactory().getPathwayData();
        data.prepareData(grid, pNodes, type);
        if (data.isEndOfInterSignalBox()) {
            final PathwayData otherData = data.requestInterSignalBoxPathway(grid);
            if (otherData == EMPTY_DATA)
                return EMPTY_DATA;
            data.combineData(otherData);

            final InterSignalBoxPathway startPath = (InterSignalBoxPathway) data.createPathway();
            final InterSignalBoxPathway endPath = (InterSignalBoxPathway) otherData.createPathway();
            startPath.setOtherPathwayToBlock(endPath);
            endPath.setOtherPathwayToReset(startPath);
            otherData.grid.addPathway(otherData);
        }
        return data;
    }

    public static PathwayData of(final SignalBoxGrid grid, final NBTWrapper tag) {
        final PathwayData data = SignalBoxFactory.getFactory().getPathwayData();
        data.grid = grid;
        data.read(tag);
        return data;
    }

    public SignalBoxPathway createPathway() {
        if (pathway == null) {
            final boolean isInterSignalBoxPathway = isInterSignalBoxPathway();
            if (delay > 0) {
                if (isInterSignalBoxPathway) {
                    pathway = new DelayableInterSignalBoxPathway(this);
                } else {
                    pathway = new DelayableSignalBoxPathway(this);
                }
            } else if (isInterSignalBoxPathway) {
                pathway = new InterSignalBoxPathway(this);
            } else {
                pathway = new SignalBoxPathway(this);
            }
        }
        return pathway;
    }

    private void prepareData(final SignalBoxGrid grid, final List<SignalBoxNode> pNodes,
            final PathType type) {
        this.grid = grid;
        this.listOfNodes = ImmutableList.copyOf(pNodes);
        this.type = Objects.requireNonNull(type);
        if (this.listOfNodes.size() < 2)
            throw new IndexOutOfBoundsException();
        if (this.type.equals(PathType.NONE))
            throw new IllegalArgumentException();
        this.initalize();
    }

    private void initalize() {
        final AtomicInteger atomic = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicReference<Byte> zs2Value = new AtomicReference<>((byte) -1);
        final AtomicInteger delayAtomic = new AtomicInteger(0);
        final Builder<BlockPos, OtherSignalIdentifier> distantPosBuilder = ImmutableMap.builder();
        mapOfBlockingPositions.clear();
        mapOfResetPositions.clear();
        foreachEntry((optionEntry, node) -> {
            optionEntry.getEntry(PathEntryType.SPEED)
                    .ifPresent(value -> atomic.updateAndGet(in -> Math.min(in, value)));
            optionEntry.getEntry(PathEntryType.BLOCKING)
                    .ifPresent(position -> mapOfBlockingPositions.put(position, node));
            optionEntry.getEntry(PathEntryType.RESETING)
                    .ifPresent(position -> mapOfResetPositions.put(position, node));
            optionEntry.getEntry(PathEntryType.ZS2).ifPresent(value -> zs2Value.set(value));
        });
        foreachPath((path, node) -> {
            if (!type.equals(PathType.SHUNTING)) {
                final Rotation rotation = SignalBoxUtil
                        .getRotationFromDelta(node.getPoint().delta(path.point1));
                for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.VP, EnumGuiMode.RS)) {
                    final ModeSet modeSet = new ModeSet(mode, rotation);
                    node.getOption(modeSet).ifPresent(
                            option -> option.getEntry(PathEntryType.SIGNAL).ifPresent(position -> {
                                final Optional<Boolean> repeaterOption = option
                                        .getEntry(PathEntryType.SIGNAL_REPEATER);
                                distantPosBuilder.put(position,
                                        new OtherSignalIdentifier(node.getPoint(), modeSet,
                                                position,
                                                repeaterOption.isPresent() && repeaterOption.get(),
                                                mode.equals(EnumGuiMode.RS)));
                            }));
                }
            }
            node.getModes().entrySet().stream()
                    .filter(entry -> entry.getKey().mode.equals(EnumGuiMode.BUE))
                    .forEach(entry -> entry.getValue().getEntry(PathEntryType.DELAY).ifPresent(
                            value -> delayAtomic.updateAndGet(in -> Math.max(in, value))));
        }, null);
        this.otherSignals = distantPosBuilder.build();
        final SignalBoxNode firstNode = this.listOfNodes.get(this.listOfNodes.size() - 1);
        this.firstPoint = firstNode.getPoint();
        final MainSignalIdentifier firstPos = makeFromNext(type, firstNode,
                this.listOfNodes.get(this.listOfNodes.size() - 2), Rotation.NONE);
        final SignalBoxNode lastNode = this.listOfNodes.get(0);
        this.lastPoint = lastNode.getPoint();
        final MainSignalIdentifier lastPos = makeFromNext(type, lastNode, this.listOfNodes.get(1),
                Rotation.CLOCKWISE_180);
        if (lastPos != null) {
            endSignal = Optional.of(lastPos);
        }
        if (firstPos != null) {
            startSignal = Optional.of(firstPos);
        }
        this.speed = atomic.get();
        this.zs2Value = JsonEnumHolder.ZS32.getObjFromID(Byte.toUnsignedInt(zs2Value.get()));
        this.delay = delayAtomic.get();
    }

    private MainSignalIdentifier makeFromNext(final PathType type, final SignalBoxNode first,
            final SignalBoxNode next, final Rotation pRotation) {
        final Point delta = first.getPoint().delta(next.getPoint());
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(delta).getRotated(pRotation);
        for (final EnumGuiMode mode : type.getModes()) {
            final ModeSet modeSet = new ModeSet(mode, rotation);
            final BlockPos possiblePosition = first.getOption(modeSet)
                    .flatMap(option -> option.getEntry(PathEntryType.SIGNAL)).orElse(null);
            if (possiblePosition != null)
                return new MainSignalIdentifier(first.getPoint(), modeSet, possiblePosition);
        }
        return null;
    }

    protected void foreachEntry(final Consumer<PathOptionEntry> consumer,
            final @Nullable Point point) {
        foreachEntry((entry, _u) -> consumer.accept(entry), point);
    }

    protected void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer) {
        foreachEntry(consumer, null);
    }

    protected void foreachPath(final BiConsumer<Path, SignalBoxNode> consumer,
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

    protected void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer,
            final @Nullable Point point) {
        foreachPath((path, current) -> current.getOption(path)
                .ifPresent(entry -> consumer.accept(entry, current)), point);
    }

    public void write(final NBTWrapper tag) {
        tag.putList(LIST_OF_NODES, listOfNodes.stream().map(node -> {
            final NBTWrapper entry = new NBTWrapper();
            node.getPoint().write(entry);
            return entry;
        })::iterator);
        tag.putString(PATH_TYPE, this.type.name());
    }

    public void read(final NBTWrapper tag) {
        final com.google.common.collect.ImmutableList.Builder<SignalBoxNode> nodeBuilder = ImmutableList
                .builder();
        final Map<Point, SignalBoxNode> modeGrid = grid.getModeGrid();
        tag.getList(LIST_OF_NODES).forEach(nodeNBT -> {
            final Point point = new Point();
            point.read(nodeNBT);
            final SignalBoxNode node = modeGrid.get(point);
            if (node == null) {
                OpenSignalsMain.getLogger().error("Detecting broken pathway at {}!",
                        point.toString());
                this.emptyOrBroken = true;
                return;
            }
            nodeBuilder.add(node);
        });
        this.listOfNodes = nodeBuilder.build();
        this.type = PathType.valueOf(tag.getString(PATH_TYPE));
        this.initalize();
    }

    public boolean tryBlock(final BlockPos position) {
        return mapOfBlockingPositions.containsKey(position);
    }

    public SignalBoxNode tryReset(final BlockPos positon) {
        return mapOfResetPositions.get(positon);
    }

    public boolean totalPathwayReset(final @Nullable Point point) {
        if (point == null || point.equals(this.getLastPoint())
                || point.equals(this.listOfNodes.get(1).getPoint())) {
            emptyOrBroken = true;
            return true;
        }
        return false;
    }

    public void compact(final Point point) {
        final Map<Point, SignalBoxNode> modeGrid = grid.getModeGrid();
        this.listOfNodes = ImmutableList.copyOf(
                this.listOfNodes.subList(0, this.listOfNodes.indexOf(modeGrid.get(point)) + 1));
        this.initalize();
    }

    public Map<BlockPos, OtherSignalIdentifier> getOtherSignals() {
        return otherSignals;
    }

    public MainSignalIdentifier getStartSignal() {
        return startSignal.orElse(null);
    }

    public MainSignalIdentifier getEndSignal() {
        return endSignal.orElse(null);
    }

    private boolean isStartOfInterSignalBox() {
        final SignalBoxNode startNode = listOfNodes.get(listOfNodes.size() - 1);
        return startNode.containsInConnection();
    }

    private boolean isEndOfInterSignalBox() {
        final SignalBoxNode endNode = listOfNodes.get(0);
        return endNode.containsOutConnection();
    }

    public boolean isInterSignalBoxPathway() {
        return isStartOfInterSignalBox() || isEndOfInterSignalBox();
    }

    public boolean isEmpty() {
        return listOfNodes.isEmpty();
    }

    private void combineData(final PathwayData other) {
        final int minSpeed = Math.min(speed, other.speed);
        this.speed = minSpeed;
        other.speed = minSpeed;
        final int maxDelay = Math.max(delay, other.delay);
        this.delay = maxDelay;
        other.delay = maxDelay;
        other.zs2Value = this.zs2Value;
    }

    private PathwayData requestInterSignalBoxPathway(final SignalBoxGrid grid) {
        final AtomicReference<PathwayData> returnResult = new AtomicReference<>(EMPTY_DATA);
        final IChunkLoadable chunkLoader = new IChunkLoadable() {
        };
        final SignalBoxNode endNode = listOfNodes.get(0);
        PathOptionEntry outConnectionEntry = null;
        for (final Rotation rot : Rotation.values()) {
            final Optional<PathOptionEntry> entry = endNode
                    .getOption(new ModeSet(EnumGuiMode.OUT_CONNECTION, rot));
            if (entry.isPresent()) {
                outConnectionEntry = entry.get();
                break;
            }
        }
        if (outConnectionEntry == null) {
            return EMPTY_DATA;
        }
        final Optional<BlockPos> otherPos = outConnectionEntry.getEntry(PathEntryType.SIGNALBOX);
        final Optional<Point> otherStartPoint = outConnectionEntry.getEntry(PathEntryType.POINT);
        if (!otherPos.isPresent() || !otherStartPoint.isPresent()) {
            return EMPTY_DATA;
        }
        chunkLoader.loadChunkAndGetTile(SignalBoxTileEntity.class,
                (ServerLevel) grid.tile.getLevel(), otherPos.get(), (endTile, _u2) -> {
                    final SignalBoxGrid endGrid = endTile.getSignalBoxGrid();
                    final SignalBoxNode otherStartNode = endGrid.getNode(otherStartPoint.get());
                    if (otherStartNode == null) {
                        return;
                    }
                    PathOptionEntry inConnectionEntry = null;
                    for (final Rotation rot : Rotation.values()) {
                        final Optional<PathOptionEntry> entry = otherStartNode
                                .getOption(new ModeSet(EnumGuiMode.IN_CONNECTION, rot));
                        if (entry.isPresent()) {
                            inConnectionEntry = entry.get();
                            break;
                        }
                    }
                    if (inConnectionEntry == null) {
                        return;
                    }
                    final Optional<Point> otherEndPoint = inConnectionEntry
                            .getEntry(PathEntryType.POINT);
                    if (!otherEndPoint.isPresent()) {
                        return;
                    }
                    final PathwayRequestResult endRequeset = SignalBoxUtil.requestPathway(endGrid,
                            otherStartPoint.get(), otherEndPoint.get());
                    if (endRequeset.isPass()) {
                        returnResult.set(endRequeset.getPathwayData());
                    }
                });
        return returnResult.get();
    }

    public int getSpeed() {
        return speed;
    }

    public int getDelay() {
        return delay;
    }

    public String getZS2Value() {
        return zs2Value;
    }

    public PathType getPathType() {
        return type;
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

    /**
     * @return the emptyOrBroken
     */
    public boolean isEmptyOrBroken() {
        return emptyOrBroken;
    }

    /**
     * @return the listOfNodes
     */
    public List<SignalBoxNode> getListOfNodes() {
        return listOfNodes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstPoint, lastPoint, listOfNodes);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathwayData other = (PathwayData) obj;
        return Objects.equals(firstPoint, other.firstPoint)
                && Objects.equals(lastPoint, other.lastPoint)
                && Objects.equals(listOfNodes, other.listOfNodes);
    }

    @Override
    public String toString() {
        return "PathwayData [firstPoint=" + firstPoint + ",lastPoint=" + lastPoint + ",listOfNodes="
                + listOfNodes + "]";
    }
}