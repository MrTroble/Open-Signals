package com.troblecodings.signals.signalbox;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.BlockPosSignalHolder;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.config.ConfigInfo;
import com.troblecodings.signals.signalbox.config.ResetInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public class SignalBoxPathway implements IChunkLoadable {

    protected final PathwayData data;

    protected boolean isBlocked;
    protected boolean isAutoPathway = false;
    protected Point originalFirstPoint = null;
    protected SignalBoxGrid grid = null;
    protected SignalBoxTileEntity tile;
    protected TrainNumber trainNumber;
    protected boolean isExecutingSignalSet = false;

    protected final SignalBoxFactory factory = SignalBoxFactory.getFactory();

    public void setTile(final SignalBoxTileEntity tile) {
        this.tile = tile;
    }

    public SignalBoxPathway(final PathwayData data) {
        this.data = data;
        this.originalFirstPoint = new Point(data.getFirstPoint());
        updatePathwayToAutomatic();
        resetAllTrainNumbers();
    }

    private static final String IS_BLOCKED = "isBlocked";
    private static final String ORIGINAL_FIRST_POINT = "origianlFirstPoint";

    public void write(final NBTWrapper tag) {
        data.write(tag);
        tag.putBoolean(IS_BLOCKED, isBlocked);
        if (originalFirstPoint != null) {
            final NBTWrapper originalFirstPoint = new NBTWrapper();
            this.originalFirstPoint.write(originalFirstPoint);
            tag.putWrapper(ORIGINAL_FIRST_POINT, originalFirstPoint);
        }
        if (trainNumber != null) {
            this.trainNumber.writeTag(tag);
        }
    }

    public void read(final NBTWrapper tag) {
        this.isBlocked = tag.getBoolean(IS_BLOCKED);
        final NBTWrapper originalFirstPoint = tag.getWrapper(ORIGINAL_FIRST_POINT);
        if (!originalFirstPoint.isTagNull()) {
            this.originalFirstPoint = new Point();
            this.originalFirstPoint.read(originalFirstPoint);
        }
        this.trainNumber = TrainNumber.of(tag);
        updatePathwayToAutomatic();
    }

    public void postRead(final NBTWrapper tag) {
    }

    public void onLoad() {
    }

    public void setPathStatus(final EnumPathUsage status, final @Nullable Point point) {
        data.foreachEntry(option -> {
            option.getEntry(PathEntryType.OUTPUT)
                    .ifPresent(pos -> SignalBoxHandler.updateRedstoneOutput(
                            new StateInfo(tile.getLevel(), pos),
                            !status.equals(EnumPathUsage.FREE)));
            if (!status.equals(EnumPathUsage.FREE)) {
                option.setEntry(PathEntryType.PATHUSAGE, status);
            } else {
                option.removeEntry(PathEntryType.PATHUSAGE);
            }
        }, point);
        if (status.equals(EnumPathUsage.SELECTED) || status.equals(EnumPathUsage.PREPARED)) {
            setProtectionWay();
        }
    }

    private void setProtectionWay() {
        data.forEachEntryProtectionWay((option, _u) -> {
            option.getEntry(PathEntryType.OUTPUT).ifPresent(pos -> SignalBoxHandler
                    .updateRedstoneOutput(new StateInfo(tile.getLevel(), pos), true));
            option.setEntry(PathEntryType.PATHUSAGE, EnumPathUsage.PROTECTED);
        });
    }

    public void checkResetOfProtectionWay(final BlockPos position) {
        if (!data.canResetProtectionWay(position))
            return;
        data.resetProtectionWay();
    }

    public boolean directResetOfProtectionWay() {
        return data.directResetOfProtectionWay();
    }

    public void setUpPathwayStatus() {
        setPathStatus(EnumPathUsage.SELECTED);
    }

    public void setPathStatus(final EnumPathUsage status) {
        setPathStatus(status, null);
    }

    public void updatePrevious() {
        if (grid == null)
            return;
        grid.updatePrevious(this);
    }

    protected SignalStateInfo lastSignalInfo = null;

    protected SignalStateInfo getLastSignalInfo() {
        if (lastSignalInfo != null)
            return lastSignalInfo;
        final Level world = tile.getLevel();
        if (world == null || world.isClientSide)
            return null;
        final StateInfo identifier = new StateInfo(world, tile.getBlockPos());
        final MainSignalIdentifier lastSignal = data.getEndSignal();
        if (lastSignal != null) {
            final Signal nextSignal = SignalBoxHandler.getSignal(identifier, lastSignal.pos);
            if (nextSignal != null) {
                lastSignalInfo = new SignalStateInfo(world, lastSignal.pos, nextSignal);
            }
        }
        return lastSignalInfo;
    }

    public void updatePathwaySignals() {
        final Level world = tile.getLevel();
        if (world == null || world.isClientSide || isExecutingSignalSet)
            return;
        setSignals();
    }

    protected SignalBoxPathway getNextPathway() {
        return grid.startsToPath.get(getLastPoint());
    }

    protected void setSignals() {
        setSignals(getLastSignalInfo());
    }

    protected void setSignals(final SignalStateInfo lastSignal) {
        if (isExecutingSignalSet || tile == null)
            return;
        isExecutingSignalSet = true;
        final Level world = tile.getLevel();
        final StateInfo identifier = new StateInfo(world, tile.getBlockPos());
        final MainSignalIdentifier startSignal = data.getStartSignal();
        if (startSignal != null) {
            if (isBlocked)
                return;
            final Signal first = SignalBoxHandler.getSignal(identifier, startSignal.pos);
            if (first == null)
                return;
            final SignalStateInfo firstInfo = new SignalStateInfo(world, startSignal.pos, first);
            SignalConfig.change(new ConfigInfo(firstInfo, lastSignal, data));
            updatePreSignals();
        }
        final Map<BlockPosSignalHolder, OtherSignalIdentifier> distantSignalPositions =
                data.getOtherSignals();
        distantSignalPositions.forEach((holder, position) -> {
            if (holder.shouldTurnSignalOff())
                return;
            final Signal current = SignalBoxHandler.getSignal(identifier, position.pos);
            if (current == null)
                return;
            final ConfigInfo info =
                    new ConfigInfo(new SignalStateInfo(world, position.pos, current), lastSignal,
                            data, position.isRepeater);
            if (position.guiMode.equals(EnumGuiMode.HP)) {
                SignalConfig.loadDisable(info);
            } else {
                SignalConfig.change(info);
            }
        });
        updateSignalStates();
        isExecutingSignalSet = false;
    }

    private void updatePreSignals() {
        final MainSignalIdentifier startSignal = data.getStartSignal();
        if (startSignal == null)
            return;
        final StateInfo identifier = new StateInfo(tile.getLevel(), tile.getBlockPos());
        final Signal first = SignalBoxHandler.getSignal(identifier, startSignal.pos);
        if (first == null)
            return;
        final SignalStateInfo firstInfo =
                new SignalStateInfo(tile.getLevel(), startSignal.pos, first);
        data.getPreSignals().forEach(posIdent -> {
            final Signal current = SignalBoxHandler.getSignal(identifier, posIdent.pos);
            if (current == null)
                return;
            SignalConfig.change(
                    new ConfigInfo(new SignalStateInfo(tile.getLevel(), posIdent.pos, current),
                            firstInfo, data, posIdent.isRepeater));
        });
    }

    protected void updateSignalStates() {
        final MainSignalIdentifier startSignal = data.getStartSignal();
        final MainSignalIdentifier endSignal = data.getEndSignal();
        if (startSignal != null) {
            if (!isBlocked) {
                startSignal.updateSignalState(SignalState.GREEN);
                data.getPreSignals().forEach(signalIdent -> {
                    signalIdent.updateSignalState(SignalState.GREEN);
                });
            }
        }
        final Map<BlockPosSignalHolder, OtherSignalIdentifier> distantSignalPositions =
                data.getOtherSignals();
        distantSignalPositions.forEach((holder, position) -> {
            if (holder.shouldTurnSignalOff()) {
                position.updateSignalState(SignalState.OFF);
                return;
            }
            final SignalBoxPathway next = getNextPathway();
            SignalState stateToSet = SignalState.RED;
            if (endSignal != null && next != null && !next.isEmptyOrBroken()) {
                if (!next.isExecutingSignalSet) {
                    stateToSet = SignalState.GREEN;
                }
                if (next.isBlocked) {
                    stateToSet = SignalState.RED;
                }
            } else {
                stateToSet = SignalState.RED;
            }
            if (position.guiMode.equals(EnumGuiMode.RS)) {
                stateToSet = SignalState.GREEN;
            } else if (position.guiMode.equals(EnumGuiMode.HP)) {
                stateToSet = SignalState.OFF;
            }
            position.updateSignalState(stateToSet);
        });
    }

    protected void updatePathwayOnGrid() {
        grid.updatePrevious(this);
    }

    protected void setSignalBoxGrid(final SignalBoxGrid grid) {
        this.grid = grid;
    }

    public void resetPathway() {
        resetPathway(null);
    }

    public void resetAllSignals() {
        resetFirstSignal();
        resetOther();
        isBlocked = true;
    }

    private void resetFirstSignal() {
        final MainSignalIdentifier startSignal = data.getStartSignal();
        if (startSignal != null) {
            final StateInfo stateInfo = new StateInfo(tile.getLevel(), tile.getBlockPos());
            final Signal current = SignalBoxHandler.getSignal(stateInfo, startSignal.pos);
            if (current == null)
                return;
            SignalConfig.reset(
                    new ResetInfo(new SignalStateInfo(tile.getLevel(), startSignal.pos, current)));
            startSignal.updateSignalState(SignalState.RED);
            data.getPreSignals().forEach(ident -> {
                final Signal currentPreSignal = SignalBoxHandler.getSignal(stateInfo, ident.pos);
                if (currentPreSignal == null)
                    return;
                SignalConfig.reset(new ResetInfo(
                        new SignalStateInfo(tile.getLevel(), ident.pos, currentPreSignal),
                        ident.isRepeater));
                ident.updateSignalState(SignalState.RED);
            });
        }
    }

    private void resetOther() {
        final Map<BlockPosSignalHolder, OtherSignalIdentifier> distantSignalPositions =
                data.getOtherSignals();
        distantSignalPositions.values().forEach((position) -> {
            final Signal current = SignalBoxHandler
                    .getSignal(new StateInfo(tile.getLevel(), tile.getBlockPos()), position.pos);
            if (current == null)
                return;
            SignalConfig.reset(
                    new ResetInfo(new SignalStateInfo(tile.getLevel(), position.pos, current),
                            position.isRepeater));
            position.updateSignalState(SignalState.RED);
        });
    }

    public void resetPathway(final @Nullable Point point) {
        this.setPathStatus(EnumPathUsage.FREE, point);
        resetFirstSignal();
        if (data.totalPathwayReset(point)) {
            resetOther();
            resetAllTrainNumbers();
            directResetOfProtectionWay();
        }
    }

    public void postReset() {
        final SignalBoxPathway next = getNextPathway();
        if (next != null) {
            next.updatePreSignals();
            next.updateSignalStates();
        }
    }

    public void compact(final Point point) {
        data.foreachPath((path, node) -> {
            final Rotation rotation =
                    SignalBoxUtil.getRotationFromDelta(node.getPoint().delta(path.point1));
            for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.VP, EnumGuiMode.RS,
                    EnumGuiMode.HP, EnumGuiMode.ZS3)) {
                node.getOption(new ModeSet(mode, rotation)).ifPresent(
                        option -> option.getEntry(PathEntryType.SIGNAL).ifPresent(position -> {
                            final Signal current = SignalBoxHandler.getSignal(
                                    new StateInfo(tile.getLevel(), tile.getBlockPos()), position);
                            if (current == null)
                                return;
                            final Map<BlockPosSignalHolder, OtherSignalIdentifier> distantSignalPositions =
                                    data.getOtherSignals();
                            final OtherSignalIdentifier identifier = distantSignalPositions
                                    .getOrDefault(new BlockPosSignalHolder(position),
                                            new OtherSignalIdentifier(point,
                                                    new ModeSet(mode, rotation), position, false,
                                                    mode, grid));
                            SignalConfig.reset(new ResetInfo(
                                    new SignalStateInfo(tile.getLevel(), position, current),
                                    identifier.isRepeater));
                            identifier.updateSignalState(SignalState.RED);
                            final OtherSignalIdentifier otherIdent = distantSignalPositions
                                    .get(new BlockPosSignalHolder(position, true));
                            if (otherIdent != null) {
                                otherIdent.updateSignalState(SignalState.RED);
                            }
                        }));
            }
        }, point);
        resetAllTrainNumbers(data.getTrainNumberDisplays());
        data.compact(point);
        updateTrainNumber(trainNumber);
        updateSignalStates();
    }

    public Optional<Point> tryReset(final BlockPos position) {
        final SignalBoxNode node = data.tryReset(position);
        if (node == null)
            return checkReverseReset(position) ? Optional.of(getFirstPoint()) : Optional.empty();
        final Point point = node.getPoint();
        final AtomicBoolean atomic = new AtomicBoolean(false);
        data.foreachEntry((option, cNode) -> {
            option.getEntry(PathEntryType.BLOCKING).ifPresent(pos -> {
                if (isPowerd(pos)) {
                    atomic.set(true);
                }
            });
        }, point);
        if (atomic.get())
            return Optional.empty();
        this.resetPathway(point);
        return Optional.of(point);
    }

    private boolean checkReverseReset(final BlockPos pos) {
        if (!isBlocked || getFirstPoint().equals(originalFirstPoint))
            return false;
        final List<SignalBoxNode> listOfNodes = data.getListOfNodes();
        final SignalBoxNode firstNode = listOfNodes.get(listOfNodes.size() - 1);
        for (final Rotation rot : Rotation.values()) {
            if (tryReversReset(pos, firstNode, rot))
                return true;
        }
        return false;
    }

    private boolean tryReversReset(final BlockPos pos, final SignalBoxNode node,
            final Rotation rot) {
        final AtomicBoolean canReset = new AtomicBoolean(false);
        for (final EnumGuiMode mode : Arrays.asList(EnumGuiMode.CORNER, EnumGuiMode.STRAIGHT,
                EnumGuiMode.CROSSING)) {
            node.getOption(new ModeSet(mode, rot)).ifPresent(
                    entry -> entry.getEntry(PathEntryType.RESETING).ifPresent(blockPos -> {
                        if (!blockPos.equals(pos))
                            return;
                        final AtomicBoolean atomic = new AtomicBoolean(false);
                        data.foreachEntry((option, cNode) -> {
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
        final Level world = tile.getLevel();
        if (world == null)
            return false;
        final BlockState state = world.getBlockState(pos);
        if (state == null || !(state.getBlock() instanceof RedstoneIO))
            return false;
        return state.getValue(RedstoneIO.POWER);
    }

    public boolean tryBlock(final BlockPos position) {
        if (!data.tryBlock(position))
            return false;
        resetFirstSignal();
        this.setPathStatus(EnumPathUsage.BLOCKED);
        if (!isBlocked) {
            getTrainNumberFromPrevious();
        }
        isBlocked = true;
        return true;
    }

    private void getTrainNumberFromPrevious() {
        final SignalBoxPathway previous = grid.getPathwayByLastPoint(getFirstPoint());
        if (previous != null) {
            final TrainNumber number = previous.trainNumber;
            if (number != null && !number.trainNumber.isEmpty()) {
                updateTrainNumber(previous.trainNumber);
            }
        }
    }

    public void checkTrainNumberUpdate(final TrainNumber number, final SignalBoxNode node) {
        if (!data.getListOfNodes().contains(node))
            return;
        updateTrainNumber(number);
    }

    protected void updateTrainNumber(final TrainNumber number) {
        resetAllTrainNumbers();
        final List<ModeIdentifier> trainNumberDisplays = data.getTrainNumberDisplays();
        if (trainNumberDisplays == null || number == null)
            return;
        trainNumberDisplays.forEach(ident -> {
            final PathOptionEntry entry =
                    grid.getNode(ident.point).getOption(ident.mode).orElse(factory.getEntry());
            if (number.equals(TrainNumber.DEFAULT)) {
                entry.removeEntry(PathEntryType.TRAINNUMBER);
            } else {
                entry.setEntry(PathEntryType.TRAINNUMBER, number);
            }
        });
        this.trainNumber = number;
    }

    private void resetAllTrainNumbers() {
        resetAllTrainNumbers(data.getTrainNumberDisplays());
    }

    private void resetAllTrainNumbers(final List<ModeIdentifier> trainNumberDisplays) {
        if (grid != null && trainNumberDisplays != null) {
            trainNumberDisplays.forEach(ident -> {
                final SignalBoxNode node = grid.getNode(ident.point);
                if (node == null)
                    return;
                node.getOption(ident.mode).orElse(factory.getEntry())
                        .removeEntry(PathEntryType.TRAINNUMBER);
            });
        }
    }

    public void deactivateAllOutputsOnPathway() {
        data.foreachPath((_u, node) -> {
            final List<BlockPos> outputs = node.clearAllManuellOutputs();
            outputs.forEach(pos -> SignalBoxHandler
                    .updateRedstoneOutput(new StateInfo(tile.getLevel(), pos), false));
        }, null);
        data.forEachEntryProtectionWay((_u, node) -> {
            final List<BlockPos> outputs = node.clearAllManuellOutputs();
            outputs.forEach(pos -> SignalBoxHandler
                    .updateRedstoneOutput(new StateInfo(tile.getLevel(), pos), false));
        });
    }

    public void updatePathwayToAutomatic() {
        final SignalBoxNode first = data.grid.getModeGrid().get(originalFirstPoint);
        if (first == null) {
            isAutoPathway = false;
            return;
        }
        this.isAutoPathway = first.isAutoPoint();
    }

    protected boolean loadTileAndExecute(final Consumer<SignalBoxTileEntity> consumer) {
        return loadTileAndExecute(this.tile.getBlockPos(), consumer);
    }

    protected boolean loadTileAndExecute(final BlockPos tilePos,
            final Consumer<SignalBoxTileEntity> consumer) {
        return loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) tile.getLevel(),
                tilePos, (blockTile, _u) -> consumer.accept(blockTile));
    }

    public void checkReRequest() {
        if (isAutoPathway) {
            grid.requestWay(originalFirstPoint, getLastPoint(), getPathType());
        }
    }

    /**
     * Getter for the first point of this pathway
     *
     * @return the firstPoint
     */
    public Point getFirstPoint() {
        return data.getFirstPoint();
    }

    /**
     * @return the lastPoint
     */
    public Point getLastPoint() {
        return data.getLastPoint();
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalBoxPathway other = (SignalBoxPathway) obj;
        return Objects.equals(data, other.data);
    }

    @Override
    public String toString() {
        return "SignalBoxPathway [start=" + getFirstPoint() + ", end=" + getLastPoint() + "]";
    }

    /**
     * @return the listOfNodes
     */
    public List<SignalBoxNode> getListOfNodes() {
        return data.getListOfNodes();
    }

    public List<SignalBoxNode> getProtectionWayNodes() {
        return data.getProtectionWayNodes();
    }

    /**
     * @return the emptyOrBroken
     */
    public boolean isEmptyOrBroken() {
        return data.isEmptyOrBroken();
    }

    public boolean isShuntingPath() {
        return getPathType().equals(PathType.SHUNTING);
    }

    public PathType getPathType() {
        return data.getPathType();
    }

    public boolean isInterSignalBoxPathway() {
        return this instanceof InterSignalBoxPathway;
    }

    public SignalBoxGrid getGrid() {
        return grid;
    }
}
