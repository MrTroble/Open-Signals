package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.BlockPosSignalHolder;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
    private Map<BlockPosSignalHolder, OtherSignalIdentifier> otherSignals = ImmutableMap.of();
    private List<OtherSignalIdentifier> preSignals = ImmutableList.of();
    private boolean emptyOrBroken = false;
    private List<SignalBoxNode> protectionWayNodes = ImmutableList.of();
    private BlockPos protectionWayReset = null;
    private int protectionWayResetDelay = 0;
    private List<ModeIdentifier> trainNumberDisplays = ImmutableList.of();

    private SignalBoxPathway pathway;

    public static PathwayData of(final SignalBoxGrid grid, final List<SignalBoxNode> pNodes,
            final PathType type) {
        final PathwayData data = SignalBoxFactory.getFactory().getPathwayData();
        data.prepareData(grid, pNodes, type);
        if (!data.checkForShuntingPath() || !data.checkForPreviousProtectionWay()
                || !data.checkForProtectionWay()) {
            return EMPTY_DATA;
        }
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
            if (type.equals(PathType.SHUNTING)) {
                if (delay > 0) {
                    pathway = new DelayableShuntingPathway(this);
                } else {
                    pathway = new ShuntingPathway(this);
                }
            } else {
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
        }
        return pathway;
    }

    private boolean checkForShuntingPath() {
        if (!type.equals(PathType.SHUNTING))
            return true;
        final Map<Point, Point> newNodes = new HashMap<>();
        Point previous = listOfNodes.get(listOfNodes.size() - 1).getPoint();
        for (int i = listOfNodes.size() - 2; i > 0; i--) {
            final Point oldPos = listOfNodes.get(i - 1).getPoint();
            final Point newPos = listOfNodes.get(i + 1).getPoint();
            final SignalBoxNode current = listOfNodes.get(i);
            newNodes.put(current.getPoint(), new Point(previous));
            previous = current.getPoint();
            final PathOptionEntry option = current.getOption(new Path(oldPos, newPos)).orElse(null);
            if (option == null) {
                continue;
            }
            final EnumPathUsage usage = option.getEntry(PathEntryType.PATHUSAGE)
                    .orElse(EnumPathUsage.FREE);
            if (!usage.equals(EnumPathUsage.FREE)) {
                final ArrayList<SignalBoxNode> listOfNodes = new ArrayList<>();
                for (Point point = previous; point != null; point = newNodes.get(point)) {
                    listOfNodes.add(grid.getNode(point));
                }
                this.listOfNodes = ImmutableList.copyOf(listOfNodes);
                if (this.listOfNodes.size() < 3)
                    return false;
                this.initalize();
                break;
            }
            if (current.isUsedInDirection(newPos)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkForProtectionWay() {
        if (!type.equals(PathType.NORMAL) || !endSignal.isPresent())
            return true;
        final MainSignalIdentifier signalIdent = endSignal.get();
        final PathOptionEntry option = grid.getNode(signalIdent.getPoint())
                .getOption(signalIdent.getModeSet()).orElse(null);
        if (option == null)
            return true;
        if (grid.startsToPath.containsKey(lastPoint)) {
            return true;
        }
        final Point protectionWayEnd = option.getEntry(PathEntryType.PROTECTIONWAY_END)
                .orElse(lastPoint);
        if (lastPoint.equals(protectionWayEnd))
            return true;
        this.protectionWayNodes = ImmutableList
                .copyOf(SignalBoxUtil.requestProtectionWay(lastPoint, protectionWayEnd, grid));
        if (protectionWayNodes.isEmpty())
            return false;
        if (protectionWayNodes.size() < 3) {
            this.protectionWayNodes = ImmutableList.of();
            return true;
        }
        final AtomicInteger atomicDelay = new AtomicInteger(delay);
        protectionWayNodes.forEach(node -> node.getModes().forEach((mode, entry) -> {
            if (mode.mode.equals(EnumGuiMode.BUE))
                entry.getEntry(PathEntryType.DELAY)
                        .ifPresent(value -> atomicDelay.updateAndGet(in -> Math.max(in, value)));
        }));
        this.delay = atomicDelay.get();
        return true;
    }

    private boolean checkForPreviousProtectionWay() {
        final SignalBoxPathway previous = grid.getPathwayByLastPoint(firstPoint);
        if (previous == null)
            return true;
        final PathwayData otherData = previous.data;
        if (otherData.protectionWayNodes.isEmpty())
            return true;
        final boolean containsAll = listOfNodes.containsAll(otherData.protectionWayNodes);
        if (containsAll) {
            otherData.protectionWayNodes = ImmutableList.of();
        }
        return containsAll;
    }

    protected boolean resetProtectionWay() {
        if (protectionWayNodes.isEmpty())
            return false;
        if (protectionWayResetDelay > 0) {
            final List<SignalBoxNode> copy = ImmutableList.copyOf(protectionWayNodes);
            new Thread(() -> {
                try {
                    Thread.sleep(protectionWayResetDelay * 1000);
                } catch (final InterruptedException e) {
                }
                this.protectionWayNodes = copy;
                directResetOfProtectionWay();
                final World world = pathway.tile.getLevel();
                world.getServer().execute(() -> {
                    pathway.grid.updateToNet(pathway);
                    removeProtectionWay();
                });
            }).start();
            return true;
        }
        directResetOfProtectionWay();
        return true;
    }

    protected boolean directResetOfProtectionWay() {
        if (protectionWayNodes.isEmpty())
            return false;
        forEachEntryProtectionWay((option, _u) -> {
            if (!option.getEntry(PathEntryType.PATHUSAGE).orElse(EnumPathUsage.FREE)
                    .equals(EnumPathUsage.PROTECTED))
                return;
            option.getEntry(PathEntryType.OUTPUT).ifPresent(pos -> SignalBoxHandler
                    .updateRedstoneOutput(new StateInfo(pathway.tile.getLevel(), pos), false));
            option.removeEntry(PathEntryType.PATHUSAGE);
        });
        return true;
    }

    protected void removeProtectionWay() {
        this.protectionWayNodes = ImmutableList.of();
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
        final Map<BlockPosSignalHolder, OtherSignalIdentifier> otherBuilder = new HashMap<>();
        final Set<ModeIdentifier> trainNumberDisplays = new HashSet<>();
        mapOfBlockingPositions.clear();
        mapOfResetPositions.clear();
        foreachPath((path, node) -> {
            node.getOption(path).ifPresent(optionEntry -> {
                optionEntry.getEntry(PathEntryType.SPEED)
                        .ifPresent(value -> atomic.updateAndGet(in -> Math.min(in, value)));
                optionEntry.getEntry(PathEntryType.BLOCKING)
                        .ifPresent(position -> mapOfBlockingPositions.put(position, node));
                optionEntry.getEntry(PathEntryType.RESETING)
                        .ifPresent(position -> mapOfResetPositions.put(position, node));
                optionEntry.getEntry(PathEntryType.ZS2).ifPresent(value -> zs2Value.set(value));
                optionEntry.getEntry(PathEntryType.CONNECTED_TRAINNUMBER)
                        .ifPresent(ident -> trainNumberDisplays.add(ident));
            });
            final Rotation rotation = SignalBoxUtil
                    .getRotationFromDelta(node.getPoint().delta(path.point1));
            for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.VP, EnumGuiMode.RS,
                    EnumGuiMode.HP, EnumGuiMode.ZS3)) {
                final ModeSet modeSet = new ModeSet(mode, rotation);
                node.getOption(modeSet).ifPresent(
                        option -> option.getEntry(PathEntryType.SIGNAL).ifPresent(position -> {
                            final Optional<Boolean> repeaterOption = option
                                    .getEntry(PathEntryType.SIGNAL_REPEATER);
                            final OtherSignalIdentifier ident = new OtherSignalIdentifier(
                                    node.getPoint(), modeSet, position,
                                    repeaterOption.isPresent() && repeaterOption.get(), mode);
                            final BlockPosSignalHolder holder = new BlockPosSignalHolder(position);
                            if (otherBuilder.containsKey(holder)) {
                                final OtherSignalIdentifier otherIdent = otherBuilder.get(holder);
                                if (ident.guiMode.ordinal() < otherIdent.guiMode.ordinal()) {
                                    otherBuilder.put(holder, ident);
                                    otherBuilder.put(new BlockPosSignalHolder(position, true),
                                            otherIdent);
                                } else {
                                    otherBuilder.put(new BlockPosSignalHolder(position, true),
                                            ident);
                                }
                            } else {
                                otherBuilder.put(holder, ident);
                            }
                        }));
            }
            node.getModes().forEach((mode, entry) -> {
                if (mode.mode.equals(EnumGuiMode.BUE))
                    entry.getEntry(PathEntryType.DELAY).ifPresent(
                            value -> delayAtomic.updateAndGet(in -> Math.max(in, value)));
            });
        }, null);
        this.trainNumberDisplays = ImmutableList.copyOf(trainNumberDisplays);
        this.otherSignals = ImmutableMap.copyOf(otherBuilder);
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
            final PathOptionEntry option = grid.getNode(lastPos.getPoint())
                    .getOption(lastPos.getModeSet()).orElse(null);
            this.protectionWayReset = option.getEntry(PathEntryType.PROTECTIONWAY_RESET)
                    .orElse(null);
            this.protectionWayResetDelay = option.getEntry(PathEntryType.DELAY).orElse(0);
        }
        if (firstPos != null) {
            startSignal = Optional.of(firstPos);
            final PathOptionEntry entry = grid.getNode(firstPos.getPoint())
                    .getOption(firstPos.getModeSet()).orElse(null);
            final List<PosIdentifier> posIdents = entry.getEntry(PathEntryType.PRESIGNALS)
                    .orElse(new ArrayList<>());
            posIdents.removeIf(ident -> !grid.getNode(ident.getPoint()).has(ident.getModeSet()));
            this.preSignals = ImmutableList.copyOf(posIdents.stream().map(ident -> {
                final PathOptionEntry vpEntry = grid.getNode(ident.getPoint())
                        .getOption(ident.getModeSet()).orElse(new PathOptionEntry());
                return new OtherSignalIdentifier(ident.getPoint(), ident.getModeSet(), ident.pos,
                        vpEntry.getEntry(PathEntryType.SIGNAL_REPEATER).orElse(false),
                        EnumGuiMode.VP);
            }).collect(Collectors.toList()));
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

    protected void forEachEntryProtectionWay(
            final BiConsumer<PathOptionEntry, SignalBoxNode> consumer) {
        for (int i = protectionWayNodes.size() - 2; i > 0; i--) {
            final Point oldPos = protectionWayNodes.get(i - 1).getPoint();
            final Point newPos = protectionWayNodes.get(i + 1).getPoint();
            final SignalBoxNode current = protectionWayNodes.get(i);
            final PathOptionEntry entry = current.getOption(new Path(oldPos, newPos)).get();
            consumer.accept(entry, current);
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
        tag.getList(LIST_OF_NODES).forEach(nodeNBT -> {
            final SignalBoxNode node = getNodeFromNBT(nodeNBT);
            if (node == null)
                return;
            nodeBuilder.add(node);
        });
        this.listOfNodes = nodeBuilder.build();
        if (!checkForProtectionWay()) {
            this.emptyOrBroken = true;
            return;
        }
        this.type = PathType.valueOf(tag.getString(PATH_TYPE));
        this.initalize();
    }

    private SignalBoxNode getNodeFromNBT(final NBTWrapper nodeNBT) {
        final Map<Point, SignalBoxNode> modeGrid = grid.modeGrid;
        final Point point = new Point();
        point.read(nodeNBT);
        final SignalBoxNode node = modeGrid.get(point);
        if (node == null) {
            OpenSignalsMain.getLogger().error("Detecting broken pathway at {}!", point.toString());
            this.emptyOrBroken = true;
            return null;
        }
        return node;
    }

    public boolean tryBlock(final BlockPos position) {
        return mapOfBlockingPositions.containsKey(position);
    }

    public SignalBoxNode tryReset(final BlockPos positon) {
        return mapOfResetPositions.get(positon);
    }

    public boolean canResetProtectionWay(final BlockPos pos) {
        return pos.equals(protectionWayReset) || this.emptyOrBroken;
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

    public Map<BlockPosSignalHolder, OtherSignalIdentifier> getOtherSignals() {
        return otherSignals;
    }

    public List<OtherSignalIdentifier> getPreSignals() {
        return preSignals;
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
                (ServerWorld) grid.tile.getLevel(), otherPos.get(), (endTile, _u2) -> {
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
                            otherStartPoint.get(), otherEndPoint.get(), PathType.NORMAL);
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

    public List<SignalBoxNode> getProtectionWayNodes() {
        return protectionWayNodes;
    }

    public SignalBoxGrid getGrid() {
        return grid;
    }

    public List<ModeIdentifier> getTrainNumberDisplays() {
        return trainNumberDisplays;
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
