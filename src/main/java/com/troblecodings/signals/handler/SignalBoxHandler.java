package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.LinkedPositions;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.PathwayHolder;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SignalBoxHandler {

    private static final Map<BlockPos, PathwayHolder> ALL_GRIDS = new HashMap<>();
    private static final Map<BlockPos, LinkedPositions> ALL_LINKED_POS = new HashMap<>();
    private static final Map<BlockPos, LinkingUpdates> POS_UPDATES = new HashMap<>();
    private static final Map<BlockPos, Boolean> OUTPUT_UPDATES = new HashMap<>();
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(3);

    public static void resetPathway(final BlockPos tilePos, final Point point) {
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(tilePos);
        }
        if (grid == null)
            return;
        grid.resetPathway(point);
    }

    public static boolean requestPathway(final BlockPos tilePos, final Point p1, final Point p2,
            final Map<Point, SignalBoxNode> modeGrid) {
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(tilePos);
        }
        if (grid == null)
            return false;
        return grid.requestWay(p1, p2, modeGrid);
    }

    public static void resetAllPathways(final BlockPos tilePos) {
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(tilePos);
        }
        if (grid == null)
            return;
        grid.resetAllPathways();
    }

    public static void updateInput(final BlockPos tilePos, final RedstoneUpdatePacket update) {
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(tilePos);
        }
        if (grid == null)
            return;
        grid.updateInput(update);
    }

    public static void writeTileNBT(final BlockPos tilePos, final NBTWrapper wrapper) {
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(tilePos);
        }
        if (grid == null)
            return;
        grid.write(wrapper);
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(tilePos);
        }
        if (holder == null)
            return;
        holder.write(wrapper);
    }

    public static void readTileNBT(final BlockPos tilePos, final NBTWrapper wrapper,
            final Map<Point, SignalBoxNode> modeGrid, final Level world) {
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.computeIfAbsent(tilePos, _u -> new PathwayHolder(world, tilePos));
        }
        grid.read(wrapper, modeGrid);
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(tilePos, _u -> new LinkedPositions());
        }
        holder.read(wrapper);
    }

    public static void setWorld(final BlockPos tilePos, final Level world) {
        if (world.isClientSide)
            return;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(tilePos);
        }
        if (grid == null)
            return;
        grid.setWorld(world);
    }

    public static boolean isTileEmpty(final BlockPos tilePos) {
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(tilePos);
        }
        if (holder == null)
            return true;
        return holder.isEmpty();
    }

    public static boolean linkPosToSignalBox(final BlockPos tilePos, final BlockPos linkPos,
            final BasicBlock block, final LinkType type, final Level world) {
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(tilePos, _u -> new LinkedPositions());
        }
        final boolean linked = holder.addLinkedPos(linkPos, type);
        if (!linked)
            return false;
        if (block instanceof Signal)
            holder.addSignal(linkPos, (Signal) block, world);
        if (block == OSBlocks.REDSTONE_IN || block == OSBlocks.REDSTONE_OUT
                || block == OSBlocks.COMBI_REDSTONE_INPUT)
            linkTileToPos(linkPos, tilePos, world);
        return linked;
    }

    public static Signal getSignal(final BlockPos tilePos, final BlockPos signalPos) {
        final LinkedPositions signals;
        synchronized (ALL_LINKED_POS) {
            signals = ALL_LINKED_POS.get(tilePos);
        }
        if (signals == null)
            return null;
        return signals.getSignal(signalPos);
    }

    public static void unlinkPosFromSignalBox(final BlockPos tilePos, final BlockPos pos) {
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(tilePos);
        }
        if (holder == null)
            return;
        holder.removeLinkedPos(pos);
    }

    public static Map<BlockPos, LinkType> getAllLinkedPos(final BlockPos tilePos) {
        final LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(tilePos);
        }
        if (holder == null)
            return new HashMap<>();
        return holder.getAllLinkedPos();
    }

    public static void onPosRemove(final BlockPos pos) {
        SERVICE.execute(() -> {
            synchronized (ALL_LINKED_POS) {
                ALL_LINKED_POS.values().forEach(holder -> holder.removeLinkedPos(pos));
            }
        });
    }

    public static void unlinkAll(final BlockPos tilePos, final Level world) {
        LinkedPositions allPos;
        synchronized (ALL_LINKED_POS) {
            allPos = ALL_LINKED_POS.get(tilePos);
        }
        if (allPos == null)
            return;
        allPos.unlink(tilePos, world);
    }

    public static void unlinkTileFromPos(final BlockPos pos, final BlockPos tilePos,
            final Level world) {
        if (tryDirectUnlink(world, pos, tilePos))
            return;
        SERVICE.execute(() -> {
            final LinkingUpdates update;
            synchronized (POS_UPDATES) {
                update = POS_UPDATES.computeIfAbsent(pos, _u -> new LinkingUpdates());
            }
            update.addPosToUnlink(tilePos);
        });
    }

    public static void linkTileToPos(final BlockPos pos, final BlockPos tilePos,
            final Level world) {
        if (tryDirectLink(world, pos, tilePos))
            return;
        SERVICE.execute(() -> {
            final LinkingUpdates update;
            synchronized (POS_UPDATES) {
                update = POS_UPDATES.computeIfAbsent(tilePos, _u -> new LinkingUpdates());
            }
            update.addPosToLink(tilePos);
        });
    }

    private static boolean tryDirectLink(final Level world, final BlockPos pos,
            final BlockPos posToLink) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null && entity instanceof BasicBlockEntity) {
            ((RedstoneIOTileEntity) entity).link(posToLink);
            return true;
        }
        return false;
    }

    private static boolean tryDirectUnlink(final Level world, final BlockPos pos,
            final BlockPos posToUnlink) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null && entity instanceof BasicBlockEntity) {
            ((RedstoneIOTileEntity) entity).unlink(posToUnlink);
            return true;
        }
        return false;
    }

    public static void removeSignalBox(final BlockPos tilePos) {
        SERVICE.execute(() -> {
            synchronized (ALL_GRIDS) {
                ALL_GRIDS.remove(tilePos);
            }
            synchronized (ALL_LINKED_POS) {
                ALL_LINKED_POS.remove(tilePos);
            }
        });
    }

    public static LinkingUpdates getPosUpdates(final BlockPos pos) {
        return POS_UPDATES.remove(pos);
    }

    public static void updateRedstoneOutput(final BlockPos outputPos, final Level world,
            final boolean state) {
        if (world.isClientSide)
            return;
        BlockState blockState = world.getBlockState(outputPos);
        if (blockState != null) {
            blockState = blockState.setValue(RedstoneIO.POWER, state);
            world.setBlockAndUpdate(outputPos, blockState);
            return;
        }
        synchronized (OUTPUT_UPDATES) {
            OUTPUT_UPDATES.put(outputPos, state);
        }
    }

    public static boolean containsOutputUpdates(final BlockPos pos) {
        return OUTPUT_UPDATES.containsKey(pos);
    }

    public static boolean getNewOutputState(final BlockPos pos) {
        return OUTPUT_UPDATES.remove(pos);
    }
}