package com.troblecodings.signals.signalbox;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.BlockPosSignalHolder;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InterSignalBoxPathway extends SignalBoxPathway {

    private static final String PATHWAY_TO_BLOCK = "pathwayToBlock";
    private static final String PATHWAY_TO_RESET = "pathwayToReset";
    private static final String END_POINT = "endPoint";
    private static final String TILE_POS = "signalBoxPos";

    protected InterSignalBoxPathway pathwayToBlock;
    protected InterSignalBoxPathway pathwayToReset;

    public InterSignalBoxPathway(final PathwayData data) {
        super(data);
    }

    @Override
    public void write(final NBTWrapper tag) {
        if (pathwayToBlock != null) {
            final NBTWrapper blockWrapper = new NBTWrapper();
            blockWrapper.putBlockPos(TILE_POS, pathwayToBlock.tile.getPos());
            final NBTWrapper pointWrapper = new NBTWrapper();
            pathwayToBlock.getLastPoint().write(pointWrapper);
            blockWrapper.putWrapper(END_POINT, pointWrapper);
            tag.putWrapper(PATHWAY_TO_BLOCK, blockWrapper);
        }
        if (pathwayToReset != null) {
            final NBTWrapper resetWrapper = new NBTWrapper();
            resetWrapper.putBlockPos(TILE_POS, pathwayToReset.tile.getPos());
            final NBTWrapper pointWrapper = new NBTWrapper();
            pathwayToReset.getLastPoint().write(pointWrapper);
            resetWrapper.putWrapper(END_POINT, pointWrapper);
            tag.putWrapper(PATHWAY_TO_RESET, resetWrapper);
        }
        super.write(tag);
    }

    private Map.Entry<BlockPos, Point> blockPW = null;
    private Map.Entry<BlockPos, Point> resetPW = null;

    @Override
    public void postRead(final NBTWrapper tag) {
        final NBTWrapper blockWrapper = tag.getWrapper(PATHWAY_TO_BLOCK);
        if (!blockWrapper.isTagNull()) {
            final Point end = new Point();
            end.read(blockWrapper.getWrapper(END_POINT));
            final BlockPos otherPos = blockWrapper.getBlockPos(TILE_POS);
            final World world = tile.getWorld();
            if (world == null || world.isRemote) {
                blockPW = Maps.immutableEntry(otherPos, end);
            } else {
                final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
                otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, otherPos)));
                if (otherGrid.get() == null) {
                    loadTileAndExecute(otherPos, tile -> otherGrid.set(tile.getSignalBoxGrid()));
                }

                final SignalBoxPathway otherPathway = otherGrid.get().getPathwayByLastPoint(end);
                pathwayToBlock = (InterSignalBoxPathway) otherPathway;
            }
        }
        final NBTWrapper resetWrapper = tag.getWrapper(PATHWAY_TO_RESET);
        if (!resetWrapper.isTagNull()) {
            final Point end = new Point();
            end.read(resetWrapper.getWrapper(END_POINT));
            final BlockPos otherPos = resetWrapper.getBlockPos(TILE_POS);
            final World world = tile.getWorld();
            if (world == null || world.isRemote) {
                resetPW = Maps.immutableEntry(otherPos, end);
            } else {
                final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
                otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, otherPos)));
                if (otherGrid.get() == null) {
                    loadTileAndExecute(otherPos, tile -> otherGrid.set(tile.getSignalBoxGrid()));
                }

                final SignalBoxPathway otherPathway = otherGrid.get().getPathwayByLastPoint(end);
                pathwayToReset = (InterSignalBoxPathway) otherPathway;
            }
        }
        super.postRead(tag);
    }

    @Override
    public void onLoad() {
        final World world = tile.getWorld();
        if (world == null || world.isRemote)
            return;
        if (blockPW != null) {
            final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
            otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, blockPW.getKey())));
            if (otherGrid.get() == null) {
                loadTileAndExecute(blockPW.getKey(),
                        tile -> otherGrid.set(tile.getSignalBoxGrid()));
            }

            if (otherGrid.get() != null) {
                final SignalBoxPathway otherPathway =
                        otherGrid.get().getPathwayByLastPoint(blockPW.getValue());
                pathwayToBlock = (InterSignalBoxPathway) otherPathway;
                blockPW = null;
            }
        }
        if (resetPW != null) {
            final AtomicReference<SignalBoxGrid> otherGrid = new AtomicReference<>();
            otherGrid.set(SignalBoxHandler.getGrid(new StateInfo(world, resetPW.getKey())));
            if (otherGrid.get() == null) {
                loadTileAndExecute(resetPW.getKey(),
                        tile -> otherGrid.set(tile.getSignalBoxGrid()));
            }

            if (otherGrid.get() != null) {
                final SignalBoxPathway otherPathway =
                        otherGrid.get().getPathwayByLastPoint(resetPW.getValue());
                pathwayToReset = (InterSignalBoxPathway) otherPathway;
                resetPW = null;
            }
        }
        super.onLoad();
    }

    @Override
    protected SignalStateInfo getLastSignalInfo() {
        if (pathwayToBlock != null) {
            final MainSignalIdentifier otherLastSignal = pathwayToBlock.data.getEndSignal();
            if (otherLastSignal != null) {
                final Signal nextSignal = SignalBoxHandler.getSignal(
                        new StateInfo(pathwayToBlock.tile.getWorld(), pathwayToBlock.tile.getPos()),
                        otherLastSignal.pos);
                if (nextSignal != null) {
                    lastSignalInfo =
                            new SignalStateInfo(tile.getWorld(), otherLastSignal.pos, nextSignal);
                }
            }
        }
        return super.getLastSignalInfo();
    }

    @Override
    public void resetAllSignals() {
        super.resetAllSignals();
        if (pathwayToReset != null) {
            pathwayToReset.loadTileAndExecute(tile -> {
                final SignalBoxGrid otherGrid = tile.getSignalBoxGrid();
                pathwayToReset = (InterSignalBoxPathway) otherGrid
                        .getPathwayByLastPoint(pathwayToReset.getLastPoint());
                if (pathwayToReset == null)
                    return;
                pathwayToReset.updatePathwaySignals();
                pathwayToReset.updatePrevious();
            });
        }
    }

    @Override
    protected void setSignals(final SignalStateInfo lastSignal) {
        if (pathwayToReset != null && lastSignal != null) {
            if (tile != null) {
                final StateInfo identifier = new StateInfo(tile.getWorld(), tile.getPos());
                final Signal signal = SignalBoxHandler.getSignal(identifier, lastSignal.pos);
                if (signal != null) {
                    pathwayToReset.setSignals(
                            new SignalStateInfo(tile.getWorld(), lastSignal.pos, signal));
                }
            }
        }
        super.setSignals(lastSignal);
    }

    @Override
    public void compact(final Point point) {
        super.compact(point);
        if (pathwayToBlock != null) {
            pathwayToBlock.loadTileAndExecute(tile -> {
                final SignalBoxGrid otherGrid = tile.getSignalBoxGrid();
                pathwayToBlock = (InterSignalBoxPathway) otherGrid
                        .getPathwayByLastPoint(pathwayToBlock.getLastPoint());
                if (pathwayToBlock == null)
                    return;
                pathwayToBlock.setOtherPathwayToReset(this);
            });
        }
    }

    @Override
    public void resetPathway(final Point point, final boolean manuellReset) {
        super.resetPathway(point, manuellReset);
        if (!manuellReset && data.totalPathwayReset(point) && pathwayToReset != null) {
            pathwayToReset.loadTileAndExecute(tile -> {
                final SignalBoxGrid otherGrid = tile.getSignalBoxGrid();
                final SignalBoxPathway pw =
                        otherGrid.getPathwayByLastPoint(pathwayToReset.getLastPoint());
                if (pw == null)
                    return;
                otherGrid.resetPathway(pw.getFirstPoint());
            });
        }
    }

    @Override
    public boolean tryBlock(final BlockPos position) {
        final boolean result = super.tryBlock(position);
        if (result && pathwayToBlock != null) {
            pathwayToBlock.loadTileAndExecute(otherTile -> {
                final SignalBoxGrid otherGrid = otherTile.getSignalBoxGrid();
                final SignalBoxPathway pw =
                        otherGrid.getPathwayByLastPoint(pathwayToBlock.getLastPoint());
                if (pw == null || !(pw instanceof InterSignalBoxPathway)) {
                    pathwayToBlock = null;
                }
                pathwayToBlock = (InterSignalBoxPathway) pw;
                pathwayToBlock.setPathStatus(EnumPathUsage.BLOCKED);
                pathwayToBlock.updateTrainNumber(trainNumber);
            });
        }
        return result;
    }

    @Override
    protected void updateSignalStates() {
        final MainSignalIdentifier startSignal = data.getStartSignal();
        final MainSignalIdentifier lastSignal = data.getEndSignal();
        if (startSignal != null) {
            if (isBlocked)
                return;
            startSignal.updateSignalState(SignalState.GREEN);
            data.getPreSignals().forEach(signalIdent -> {
                signalIdent.updateSignalState(SignalState.GREEN);
            });
        }
        final Map<BlockPosSignalHolder, OtherSignalIdentifier> distantSignalPositions =
                data.getOtherSignals();
        distantSignalPositions.forEach((holder, position) -> {
            if (holder.shouldTurnSignalOff()) {
                position.updateSignalState(SignalState.OFF);
                return;
            }
            final SignalBoxPathway next = getNextPathway();
            SignalState toSet = SignalState.RED;
            if (lastSignal != null && next != null && !next.isEmptyOrBroken()) {
                if (!next.isExecutingSignalSet) {
                    toSet = SignalState.GREEN;
                }
            } else if (pathwayToBlock != null) {
                final SignalBoxPathway otherNext = pathwayToBlock.getNextPathway();
                if (otherNext != null && !otherNext.isEmptyOrBroken()) {
                    if (!otherNext.isExecutingSignalSet) {
                        toSet = SignalState.GREEN;
                    }
                } else {
                    toSet = SignalState.RED;
                }
            } else {
                toSet = SignalState.RED;
            }
            if (position.guiMode.equals(EnumGuiMode.RS)) {
                toSet = SignalState.GREEN;
            } else if (position.guiMode.equals(EnumGuiMode.HP)) {
                toSet = SignalState.OFF;
            }
            position.updateSignalState(toSet);
        });
    }

    public void setOtherPathwayToBlock(final InterSignalBoxPathway pathway) {
        if (!(pathway instanceof InterSignalBoxPathway))
            return;
        this.pathwayToBlock = pathway;
    }

    public void setOtherPathwayToReset(final InterSignalBoxPathway pathway) {
        if (!(pathway instanceof InterSignalBoxPathway))
            return;
        this.pathwayToReset = pathway;
    }

    @Override
    public String toString() {
        return "InterSignalBoxPathway [start=" + getFirstPoint() + ", end=" + getLastPoint() + "]";
    }
}