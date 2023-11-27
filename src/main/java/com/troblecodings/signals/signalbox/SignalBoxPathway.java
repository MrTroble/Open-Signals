package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.JsonEnumHolder;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.config.ConfigInfo;
import com.troblecodings.signals.signalbox.config.ResetInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public class SignalBoxPathway implements IChunkLoadable {

    private final ExecutorService service = Executors.newFixedThreadPool(1);

    private final Map<BlockPos, SignalBoxNode> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, SignalBoxNode> mapOfBlockingPositions = new HashMap<>();
    private ImmutableList<SignalBoxNode> listOfNodes = ImmutableList.of();
    private PathType type = PathType.NONE;
    private Point firstPoint = new Point();
    private Point lastPoint = new Point();
    private int speed = -1;
    private String zs2Value = "";
    private int delay = 0;
    private Optional<Entry<MainSignalIdentifier, MainSignalIdentifier>> signalPositions = Optional
            .empty();
    private Optional<MainSignalIdentifier> lastSignal = Optional.empty();
    private ImmutableMap<BlockPos, OtherSignalIdentifier> distantSignalPositions = ImmutableMap
            .of();
    private Map<Point, SignalBoxNode> modeGrid = null;
    private boolean emptyOrBroken = false;
    private Level world;
    private BlockPos tilePos;
    private boolean isBlocked;
    private boolean isAutoPathway = false;
    private Point originalFirstPoint = null;
    private Consumer<SignalBoxPathway> consumer;
    private boolean isPathwayReseted = false;
    private SignalBoxGrid holder = null;
    private SignalBoxTileEntity tile;

    private SignalBoxPathway pathwayToBlock;
    private SignalBoxPathway pathwayToReset;

    public SignalBoxPathway(final Map<Point, SignalBoxNode> modeGrid) {
        this.modeGrid = modeGrid;
    }

    public void setTile(final SignalBoxTileEntity tile) {
        this.world = tile.getLevel();
        this.tilePos = tile.getBlockPos();
        this.tile = tile;
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
        this.originalFirstPoint = new Point(firstPoint);
        updatePathwayToAutomatic();
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
            final Rotation rotation = SignalBoxUtil
                    .getRotationFromDelta(node.getPoint().delta(path.point1));
            for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.VP, EnumGuiMode.RS)) {
                final ModeSet modeSet = new ModeSet(mode, rotation);
                node.getOption(modeSet).ifPresent(
                        option -> option.getEntry(PathEntryType.SIGNAL).ifPresent(position -> {
                            final Optional<Boolean> repeaterOption = option
                                    .getEntry(PathEntryType.SIGNAL_REPEATER);
                            distantPosBuilder.put(position,
                                    new OtherSignalIdentifier(node.getPoint(), modeSet, position,
                                            repeaterOption.isPresent() && repeaterOption.get(),
                                            mode.equals(EnumGuiMode.RS)));
                        }));
            }
            node.getModes().entrySet().stream()
                    .filter(entry -> entry.getKey().mode.equals(EnumGuiMode.BUE))
                    .forEach(entry -> entry.getValue().getEntry(PathEntryType.DELAY).ifPresent(
                            value -> delayAtomic.updateAndGet(in -> Math.max(in, value))));
        }, null);
        this.distantSignalPositions = distantPosBuilder.build();
        final SignalBoxNode firstNode = this.listOfNodes.get(this.listOfNodes.size() - 1);
        this.firstPoint = firstNode.getPoint();
        final MainSignalIdentifier firstPos = makeFromNext(type, firstNode,
                this.listOfNodes.get(this.listOfNodes.size() - 2), Rotation.NONE);
        final SignalBoxNode lastNode = this.listOfNodes.get(0);
        this.lastPoint = lastNode.getPoint();
        final MainSignalIdentifier lastPos = makeFromNext(type, lastNode, this.listOfNodes.get(1),
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

    private static final String LIST_OF_NODES = "listOfNodes";
    private static final String PATH_TYPE = "pathType";
    private static final String IS_BLOCKED = "isBlocked";
    private static final String ORIGINAL_FIRST_POINT = "origianlFirstPoint";
    private static final String PATHWAY_TO_BLOCK = "pathwayToBlock";
    private static final String PATHWAY_TO_RESET = "pathwayToReset";
    private static final String END_POINT = "endPoint";
    private static final String TILE_POS = "signalBoxPos";

    public void write(final NBTWrapper tag) {
        tag.putList(LIST_OF_NODES, listOfNodes.stream().map(node -> {
            final NBTWrapper entry = new NBTWrapper();
            node.getPoint().write(entry);
            return entry;
        })::iterator);
        tag.putString(PATH_TYPE, this.type.name());
        tag.putBoolean(IS_BLOCKED, isBlocked);
        if (originalFirstPoint != null) {
            final NBTWrapper originalFirstPoint = new NBTWrapper();
            this.originalFirstPoint.write(originalFirstPoint);
            tag.putWrapper(ORIGINAL_FIRST_POINT, originalFirstPoint);
        }
        if (pathwayToBlock != null) {
            final NBTWrapper blockWrapper = new NBTWrapper();
            blockWrapper.putBlockPos(TILE_POS, pathwayToBlock.tilePos);
            final NBTWrapper pointWrapper = new NBTWrapper();
            pathwayToBlock.lastPoint.write(pointWrapper);
            blockWrapper.putWrapper(END_POINT, pointWrapper);
            tag.putWrapper(PATHWAY_TO_BLOCK, blockWrapper);
        }
        if (pathwayToReset != null) {
            final NBTWrapper resetWrapper = new NBTWrapper();
            resetWrapper.putBlockPos(TILE_POS, pathwayToReset.tilePos);
            final NBTWrapper pointWrapper = new NBTWrapper();
            pathwayToReset.lastPoint.write(pointWrapper);
            resetWrapper.putWrapper(END_POINT, pointWrapper);
            tag.putWrapper(PATHWAY_TO_RESET, resetWrapper);
        }
    }

    public void read(final NBTWrapper tag) {
        final com.google.common.collect.ImmutableList.Builder<SignalBoxNode> nodeBuilder = ImmutableList
                .builder();
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
        this.isBlocked = tag.getBoolean(IS_BLOCKED);
        if (this.listOfNodes.size() < 2) {
            OpenSignalsMain.getLogger().error("Detecting pathway with only 2 elements!");
            this.emptyOrBroken = true;
            return;
        }
        this.initalize();
        final NBTWrapper originalFirstPoint = tag.getWrapper(ORIGINAL_FIRST_POINT);
        if (!originalFirstPoint.isTagNull()) {
            this.originalFirstPoint = new Point();
            this.originalFirstPoint.read(originalFirstPoint);
        }
        updatePathwayToAutomatic();
        updateSignalStates();
    }

    private Map.Entry<BlockPos, Point> blockPW = null;
    private Map.Entry<BlockPos, Point> resetPW = null;

    public void readLinkedPathways(final NBTWrapper tag) {
        final NBTWrapper blockWrapper = tag.getWrapper(PATHWAY_TO_BLOCK);
        if (!blockWrapper.isTagNull()) {
            final Point end = new Point();
            end.read(blockWrapper.getWrapper(END_POINT));
            final BlockPos otherPos = blockWrapper.getBlockPos(TILE_POS);
            if (world == null || world.isClientSide) {
                blockPW = Maps.immutableEntry(otherPos, end);
            } else {
                final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
                otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, otherPos)));
                if (otherGrid.get() == null)
                    loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world, otherPos,
                            (tile, _u) -> otherGrid.set(tile.getSignalBoxGrid()));

                final SignalBoxPathway otherPathway = otherGrid.get().getPathwayByLastPoint(end);
                pathwayToBlock = otherPathway;
            }
        }
        final NBTWrapper resetWrapper = tag.getWrapper(PATHWAY_TO_RESET);
        if (!resetWrapper.isTagNull()) {
            final Point end = new Point();
            end.read(resetWrapper.getWrapper(END_POINT));
            final BlockPos otherPos = resetWrapper.getBlockPos(TILE_POS);
            if (world == null || world.isClientSide) {
                resetPW = Maps.immutableEntry(otherPos, end);
            } else {
                final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
                otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, otherPos)));
                if (otherGrid.get() == null)
                    loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world, otherPos,
                            (tile, _u) -> otherGrid.set(tile.getSignalBoxGrid()));

                final SignalBoxPathway otherPathway = otherGrid.get().getPathwayByLastPoint(end);
                pathwayToReset = otherPathway;
            }
        }
    }

    public void linkPathways() {
        if (world == null || world.isClientSide)
            return;
        if (blockPW != null) {
            final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
            otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, blockPW.getKey())));
            if (otherGrid.get() == null)
                loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world,
                        blockPW.getKey(), (tile, _u) -> otherGrid.set(tile.getSignalBoxGrid()));

            if (otherGrid.get() != null) {
                final SignalBoxPathway otherPathway = otherGrid.get()
                        .getPathwayByLastPoint(blockPW.getValue());
                pathwayToBlock = otherPathway;
                blockPW = null;
            }
        }
        if (resetPW != null) {
            final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
            otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, resetPW.getKey())));
            if (otherGrid.get() == null)
                loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world,
                        resetPW.getKey(), (tile, _u) -> otherGrid.set(tile.getSignalBoxGrid()));

            if (otherGrid.get() != null) {
                final SignalBoxPathway otherPathway = otherGrid.get()
                        .getPathwayByLastPoint(resetPW.getValue());
                pathwayToReset = otherPathway;
                resetPW = null;
            }
        }
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
            option.getEntry(PathEntryType.OUTPUT).ifPresent(
                    pos -> SignalBoxHandler.updateRedstoneOutput(new StateInfo(world, pos),
                            !status.equals(EnumPathUsage.FREE)));
            option.setEntry(PathEntryType.PATHUSAGE, status);
        }, point);
    }

    public void setPathStatus(final EnumPathUsage status) {
        setPathStatus(status, null);
    }

    private SignalStateInfo lastSignalInfo = null;

    private SignalStateInfo getLastSignalInfo() {
        if (lastSignalInfo != null)
            return lastSignalInfo;
        if (world == null || world.isClientSide)
            return null;
        final StateInfo identifier = new StateInfo(world, tilePos);
        SignalStateInfo lastInfo = null;
        if (lastSignal.isPresent()) {
            final Signal nextSignal = SignalBoxHandler.getSignal(identifier, lastSignal.get().pos);
            if (nextSignal != null)
                lastInfo = new SignalStateInfo(world, lastSignal.get().pos, nextSignal);
        }
        if (pathwayToBlock != null && pathwayToBlock.lastSignal.isPresent()) {
            final Signal nextSignal = SignalBoxHandler.getSignal(
                    new StateInfo(pathwayToBlock.world, pathwayToBlock.tilePos),
                    pathwayToBlock.lastSignal.get().pos);
            if (nextSignal != null)
                lastInfo = new SignalStateInfo(world, pathwayToBlock.lastSignal.get().pos,
                        nextSignal);
        }
        return lastInfo;
    }

    private boolean isExecutingSignalSet = false;

    public void updatePathwaySignals() {
        if (world == null || world.isClientSide)
            return;
        final SignalStateInfo lastSignal = getLastSignalInfo();
        if (delay > 0) {
            setPathStatus(EnumPathUsage.PREPARED);
            if (pathwayToBlock != null) {
                loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) pathwayToBlock.world,
                        pathwayToBlock.tilePos, (_u, _u1) -> {
                            pathwayToBlock.isExecutingSignalSet = true;
                            pathwayToBlock.setPathStatus(EnumPathUsage.PREPARED);
                            pathwayToBlock.executeConsumer();
                        });
            }
            if (isExecutingSignalSet)
                return;
            this.isExecutingSignalSet = true;
            service.execute(() -> {
                try {
                    Thread.sleep(delay * 1000);
                } catch (final InterruptedException e) {
                    isExecutingSignalSet = false;
                }
                if (isPathwayReseted) {
                    return;
                }
                synchronized (distantSignalPositions) {
                    this.isExecutingSignalSet = false;
                    setSignals(getLastSignalInfo());
                }
                world.getServer().execute(() -> {
                    loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world, tilePos,
                            (thisTile, _u1) -> {
                                final SignalBoxPathway pw = thisTile.getSignalBoxGrid()
                                        .getPathwayByLastPoint(getLastPoint());
                                pw.setPathStatus(EnumPathUsage.SELECTED);
                                pw.executeConsumer();
                            });
                    if (pathwayToBlock != null) {
                        loadChunkAndGetTile(SignalBoxTileEntity.class,
                                (ServerLevel) pathwayToBlock.world, pathwayToBlock.tilePos,
                                (otherTile, _u1) -> {
                                    pathwayToBlock = otherTile.getSignalBoxGrid()
                                            .getPathwayByLastPoint(pathwayToBlock.getLastPoint());
                                    pathwayToBlock.setPathStatus(EnumPathUsage.SELECTED);
                                    pathwayToBlock.executeConsumer();
                                });
                    }
                });
            });
            return;
        }
        setSignals(lastSignal);
    }

    private SignalBoxPathway getNextPathway() {
        return holder.startsToPath.get(lastPoint);
    }

    public void setSignals() {
        setSignals(getLastSignalInfo());
    }

    private void setSignals(final SignalStateInfo lastSignal) {
        if (isExecutingSignalSet)
            return;
        final StateInfo identifier = new StateInfo(world, tilePos);
        this.signalPositions.ifPresent(entry -> {
            if (isBlocked)
                return;
            final Signal first = SignalBoxHandler.getSignal(identifier, entry.getKey().pos);
            if (first == null)
                return;
            final SignalStateInfo firstInfo = new SignalStateInfo(world, entry.getKey().pos, first);
            SignalConfig.change(new ConfigInfo(firstInfo, lastSignal, speed, zs2Value, type));
        });
        distantSignalPositions.values().forEach(position -> {
            final Signal current = SignalBoxHandler.getSignal(identifier, position.pos);
            if (current == null)
                return;
            SignalConfig.change(new ConfigInfo(new SignalStateInfo(world, position.pos, current),
                    lastSignal, speed, zs2Value, type, position.isRepeater));
        });
        if (this.lastSignal.isPresent() && pathwayToReset != null) {
            final Signal signal = SignalBoxHandler.getSignal(identifier, this.lastSignal.get().pos);
            if (signal == null)
                return;
            pathwayToReset
                    .setSignals(new SignalStateInfo(world, this.lastSignal.get().pos, signal));
        }
        updateSignalStates();
    }

    private void updateSignalStates() {
        final List<MainSignalIdentifier> redSignals = new ArrayList<>();
        final List<MainSignalIdentifier> greenSignals = new ArrayList<>();
        this.signalPositions.ifPresent(entry -> {
            if (isBlocked)
                return;
            final SignalState previous = entry.getKey().state;
            entry.getKey().state = SignalState.GREEN;
            if (!entry.getKey().state.equals(previous))
                greenSignals.add(entry.getKey());
        });
        distantSignalPositions.values().forEach(position -> {
            final SignalBoxPathway next = getNextPathway();
            final SignalState previous = position.state;
            if (lastSignal != null && next != null && !next.isPathwayRestted()) {
                if (!next.isExecutingSignalSet)
                    position.state = SignalState.GREEN;
            } else if (pathwayToBlock != null) {
                final SignalBoxPathway otherNext = pathwayToBlock.getNextPathway();
                if (otherNext != null && !otherNext.isPathwayRestted()) {
                    if (!otherNext.isExecutingSignalSet)
                        position.state = SignalState.GREEN;
                } else {
                    position.state = SignalState.RED;
                }
            } else {
                position.state = SignalState.RED;
            }
            if (position.isRSSignal) {
                position.state = SignalState.GREEN;
            }
            if (position.state.equals(previous)) {
                return;
            } else {
                if (position.state.equals(SignalState.RED)) {
                    redSignals.add(position);
                } else if (position.state.equals(SignalState.GREEN)) {
                    greenSignals.add(position);
                }
            }
        });
        updateSignalsOnClient(redSignals, greenSignals);
    }

    public List<MainSignalIdentifier> getGreenSignals() {
        final List<MainSignalIdentifier> returnList = new ArrayList<>();
        signalPositions.ifPresent(entry -> {
            if (entry.getKey().state.equals(SignalState.GREEN))
                returnList.add(entry.getKey());
        });
        distantSignalPositions.values().forEach(signal -> {
            if (signal.state.equals(SignalState.GREEN))
                returnList.add(signal);
        });
        return returnList;
    }

    private void executeConsumer() {
        this.consumer.accept(this);
    }

    public void setUpdater(final Consumer<SignalBoxPathway> consumer) {
        this.consumer = consumer;
    }

    public void setOtherPathwayToBlock(final SignalBoxPathway pathway) {
        this.pathwayToBlock = pathway;
        this.delay = Math.max(delay, pathwayToBlock.delay);
        if (delay > 0) {
            pathwayToBlock.service.shutdownNow();
            service.shutdownNow();
            resetFirstSignal();
            resetOther();
            updatePathwaySignals();
            executeConsumer();
            pathwayToBlock.executeConsumer();
        }
    }

    public void setOtherPathwayToReset(final SignalBoxPathway pathway) {
        this.pathwayToReset = pathway;
    }

    public void setSignalBoxGrid(final SignalBoxGrid holder) {
        this.holder = holder;
    }

    private void updateSignalsOnClient(final List<MainSignalIdentifier> redSignals) {
        updateSignalsOnClient(redSignals, new ArrayList<>());
    }

    private void updateSignalsOnClient(final List<MainSignalIdentifier> redSignals,
            final List<MainSignalIdentifier> greenSignals) {
        if (redSignals.isEmpty() && greenSignals.isEmpty())
            return;
        if (world == null || world.isClientSide)
            return;
        world.getServer().execute(() -> {
            final WriteBuffer buffer = new WriteBuffer();
            buffer.putEnumValue(SignalBoxNetwork.SET_SIGNALS);
            buffer.putByte((byte) redSignals.size());
            redSignals.forEach(signal -> {
                signal.writeNetwork(buffer);
                holder.updateSubsidiarySignal(signal.getPoint(), signal.getModeSet(),
                        new SubsidiaryEntry(null, false));
            });
            buffer.putByte((byte) greenSignals.size());
            greenSignals.forEach(signal -> signal.writeNetwork(buffer));
            if (tile == null || !tile.isBlocked())
                return;
            OpenSignalsMain.network.sendTo(tile.get(0).getPlayer(), buffer);
        });
    }

    public void resetPathway() {
        resetPathway(null);
    }

    private void resetFirstSignal() {
        this.signalPositions.ifPresent(entry -> {
            final Signal current = SignalBoxHandler.getSignal(new StateInfo(world, tilePos),
                    entry.getKey().pos);
            if (current == null)
                return;
            SignalConfig.reset(
                    new ResetInfo(new SignalStateInfo(world, entry.getKey().pos, current), false));
            final SignalState previous = entry.getKey().state;
            entry.getKey().state = SignalState.RED;
            if (!entry.getKey().state.equals(previous)) {
                updateSignalsOnClient(ImmutableList.of(entry.getKey()));
            }
        });
    }

    private void resetOther() {
        final List<MainSignalIdentifier> redSignals = new ArrayList<>();
        distantSignalPositions.values().forEach(position -> {
            final Signal current = SignalBoxHandler.getSignal(new StateInfo(world, tilePos),
                    position.pos);
            if (current == null)
                return;
            SignalConfig.reset(new ResetInfo(new SignalStateInfo(world, position.pos, current),
                    position.isRepeater));
            final SignalState previous = position.state;
            position.state = SignalState.RED;
            if (!position.state.equals(previous)) {
                redSignals.add(position);
            }
        });
        updateSignalsOnClient(redSignals);
    }

    public void resetPathway(final @Nullable Point point) {
        this.setPathStatus(EnumPathUsage.FREE, point);
        resetFirstSignal();
        if (point == null || point.equals(this.getLastPoint())
                || point.equals(this.listOfNodes.get(1).getPoint())) {
            this.emptyOrBroken = true;
            this.isBlocked = false;
            this.isPathwayReseted = true;
            resetOther();
            if (pathwayToReset != null) {
                loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) pathwayToReset.world,
                        pathwayToReset.tilePos, (tile, _u) -> tile.getSignalBoxGrid()
                                .resetPathway(pathwayToReset.getFirstPoint()));
            }
        }
    }

    public void compact(final Point point) {
        final List<MainSignalIdentifier> redSignals = new ArrayList<>();
        foreachPath((path, node) -> {
            final Rotation rotation = SignalBoxUtil
                    .getRotationFromDelta(node.getPoint().delta(path.point1));
            for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.VP, EnumGuiMode.RS)) {
                node.getOption(new ModeSet(mode, rotation)).ifPresent(
                        option -> option.getEntry(PathEntryType.SIGNAL).ifPresent(position -> {
                            final Signal current = SignalBoxHandler
                                    .getSignal(new StateInfo(world, tilePos), position);
                            if (current == null)
                                return;
                            final OtherSignalIdentifier identifier = distantSignalPositions
                                    .getOrDefault(position,
                                            new OtherSignalIdentifier(point,
                                                    new ModeSet(mode, rotation), position, false,
                                                    mode.equals(EnumGuiMode.RS)));
                            SignalConfig.reset(
                                    new ResetInfo(new SignalStateInfo(world, position, current),
                                            identifier.isRepeater));
                            final SignalState previous = identifier.state;
                            identifier.state = SignalState.RED;
                            if (!identifier.state.equals(previous)) {
                                redSignals.add(identifier);
                            }
                        }));
            }
        }, point);
        this.listOfNodes = ImmutableList.copyOf(this.listOfNodes.subList(0,
                this.listOfNodes.indexOf(this.modeGrid.get(point)) + 1));
        this.initalize();
        updateSignalsOnClient(redSignals);
    }

    public Optional<Point> tryReset(final BlockPos position) {
        final SignalBoxNode node = this.mapOfResetPositions.get(position);
        if (node == null) {
            if (checkReverseReset(position)) {
                return Optional.of(firstPoint);
            } else {
                return Optional.empty();
            }
        }
        final Point point = node.getPoint();
        final AtomicBoolean atomic = new AtomicBoolean(false);
        foreachEntry((option, cNode) -> {
            option.getEntry(PathEntryType.BLOCKING).ifPresent(pos -> {
                if (isPowerd(pos))
                    atomic.set(true);
            });
        }, point);
        if (atomic.get())
            return Optional.empty();
        this.resetPathway(point);
        return Optional.of(point);
    }

    private boolean checkReverseReset(final BlockPos pos) {
        final SignalBoxNode firstNode = listOfNodes.get(listOfNodes.size() - 1);
        for (final Rotation rot : Rotation.values()) {
            if (tryReversReset(pos, firstNode, rot)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryReversReset(final BlockPos pos, final SignalBoxNode node,
            final Rotation rot) {
        final AtomicBoolean canReset = new AtomicBoolean(false);
        for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.CORNER, EnumGuiMode.STRAIGHT)) {
            node.getOption(new ModeSet(mode, rot)).ifPresent(
                    entry -> entry.getEntry(PathEntryType.RESETING).ifPresent(blockPos -> {
                        if (!blockPos.equals(pos))
                            return;
                        final AtomicBoolean atomic = new AtomicBoolean(false);
                        foreachEntry((option, cNode) -> {
                            option.getEntry(PathEntryType.BLOCKING).ifPresent(blockingPos -> {
                                if (isPowerd(blockingPos)) {
                                    atomic.set(true);
                                }
                            });
                        });
                        if (atomic.get())
                            return;
                        canReset.set(true);
                        this.resetPathway();
                    }));
        }
        return canReset.get();
    }

    private boolean isPowerd(final BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        if (state == null || !(state.getBlock() instanceof RedstoneIO))
            return false;
        return state.getValue(RedstoneIO.POWER);
    }

    public boolean tryBlock(final BlockPos position) {
        if (!mapOfBlockingPositions.containsKey(position))
            return false;
        resetFirstSignal();
        this.setPathStatus(EnumPathUsage.BLOCKED);
        isBlocked = true;
        if (pathwayToBlock != null) {
            loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) pathwayToBlock.world,
                    pathwayToBlock.tilePos, (otherTile, _u) -> {
                        pathwayToBlock = otherTile.getSignalBoxGrid()
                                .getPathwayByLastPoint(pathwayToBlock.getLastPoint());
                        pathwayToBlock.setPathStatus(EnumPathUsage.BLOCKED);
                        pathwayToBlock.executeConsumer();
                    });
        }
        return true;
    }

    public void deactivateAllOutputsOnPathway() {
        foreachPath((_u, node) -> {
            final List<BlockPos> outputs = node.clearAllManuellOutputs();
            outputs.forEach(
                    pos -> SignalBoxHandler.updateRedstoneOutput(new StateInfo(world, pos), false));
        }, null);
    }

    public void updatePathwayToAutomatic() {
        final SignalBoxNode first = modeGrid.get(originalFirstPoint);
        if (first == null) {
            isAutoPathway = false;
            return;
        }
        this.isAutoPathway = first.isAutoPoint();
    }

    public void checkReRequest() {
        if (isAutoPathway) {
            holder.requestWay(firstPoint, getLastPoint());
        }
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
        if ((obj == null) || (getClass() != obj.getClass()))
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

    public boolean isPathwayRestted() {
        return isPathwayReseted;
    }
}
