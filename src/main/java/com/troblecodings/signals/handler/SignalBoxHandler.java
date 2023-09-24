package com.troblecodings.signals.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.LinkedPositions;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.PathwayHolder;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SignalBoxHandler {

    private SignalBoxHandler() {
    }

    private static final Map<PosIdentifier, PathwayHolder> ALL_GRIDS = new HashMap<>();
    private static final Map<PosIdentifier, LinkedPositions> ALL_LINKED_POS = new HashMap<>();
    private static final Map<PosIdentifier, LinkingUpdates> POS_UPDATES = new HashMap<>();
    private static final Map<PosIdentifier, Boolean> OUTPUT_UPDATES = new HashMap<>();

    public static void resetPathway(final PosIdentifier identifier, final Point point) {
        if (identifier.world.isClientSide)
            return;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(identifier);
        }
        if (grid == null)
            return;
        grid.resetPathway(point);
    }

    public static boolean requestPathway(final PosIdentifier identifier, final Point p1,
            final Point p2, final Map<Point, SignalBoxNode> modeGrid) {
        if (identifier.world.isClientSide)
            return false;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(identifier);
        }
        if (grid == null)
            return false;
        return grid.requestWay(p1, p2, modeGrid);
    }

    public static void resetAllPathways(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(identifier);
        }
        if (grid == null)
            return;
        grid.resetAllPathways();
    }

    public static void updateInput(final PosIdentifier identifier,
            final RedstoneUpdatePacket update) {
        if (identifier.world.isClientSide)
            return;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(identifier);
        }
        if (grid == null)
            return;
        grid.updateInput(update);
    }

    public static void writeTileNBT(final PosIdentifier identifier, final NBTWrapper wrapper) {
        if (identifier.world.isClientSide)
            return;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(identifier);
        }
        if (grid == null)
            return;
        grid.write(wrapper);
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.write(wrapper);
    }

    public static void readTileNBT(final PosIdentifier identifier, final NBTWrapper wrapper,
            final Map<Point, SignalBoxNode> modeGrid) {
        if (identifier.world.isClientSide)
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(identifier, _u -> new LinkedPositions());
        }
        holder.read(wrapper);
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.computeIfAbsent(identifier,
                    _u -> new PathwayHolder(identifier.world, identifier.pos));
        }
        grid.read(wrapper, modeGrid);
    }

    public static void setWorld(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        PathwayHolder grid;
        synchronized (ALL_GRIDS) {
            grid = ALL_GRIDS.get(identifier);
        }
        if (grid == null)
            return;
        grid.setWorld(identifier.world);
    }

    public static boolean isTileEmpty(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return true;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return true;
        return holder.isEmpty();
    }

    public static boolean linkPosToSignalBox(final PosIdentifier identifier, final BlockPos linkPos,
            final BasicBlock block, final LinkType type) {
        if (identifier.world.isClientSide)
            return false;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(identifier, _u -> new LinkedPositions());
        }
        final boolean linked = holder.addLinkedPos(linkPos, type);
        if (!linked)
            return false;
        if (block instanceof Signal)
            holder.addSignal(linkPos, (Signal) block, identifier.world);
        if (block == OSBlocks.REDSTONE_IN || block == OSBlocks.REDSTONE_OUT
                || block == OSBlocks.COMBI_REDSTONE_INPUT)
            linkTileToPos(identifier, linkPos);
        return linked;
    }

    public static void relinkAllRedstoneIOs(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.getAllRedstoneIOs().forEach(pos -> linkTileToPos(identifier, pos));
    }

    public static Map<BlockPos, List<SubsidiaryState>> getPossibleSubsidiaries(
            final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return new HashMap<>();
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return new HashMap<>();
        return holder.getValidSubsidiariesForPos();
    }

    public static Signal getSignal(final PosIdentifier identifier, final BlockPos signalPos) {
        if (identifier.world.isClientSide)
            return null;
        final LinkedPositions signals;
        synchronized (ALL_LINKED_POS) {
            signals = ALL_LINKED_POS.get(identifier);
        }
        if (signals == null)
            return null;
        return signals.getSignal(signalPos);
    }

    public static void unlinkPosFromSignalBox(final PosIdentifier identifier, final BlockPos pos) {
        if (identifier.world.isClientSide)
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.removeLinkedPos(pos);
    }

    public static Map<BlockPos, LinkType> getAllLinkedPos(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return new HashMap<>();
        final LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return new HashMap<>();
        return holder.getAllLinkedPos();
    }

    public static void onPosRemove(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        synchronized (ALL_LINKED_POS) {
            ALL_LINKED_POS.forEach((pos, holder) -> {
                if (pos.world.equals(identifier.world))
                    holder.removeLinkedPos(identifier.pos);
            });
        }
        synchronized (POS_UPDATES) {
            POS_UPDATES.remove(identifier);
        }
    }

    public static void unlinkAll(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        LinkedPositions allPos;
        synchronized (ALL_LINKED_POS) {
            allPos = ALL_LINKED_POS.get(identifier);
        }
        if (allPos == null)
            return;
        allPos.unlink(identifier.pos, identifier.world);
    }

    public static void unlinkTileFromPos(final PosIdentifier identifier,
            final BlockPos posToUnlink) {
        if (identifier.world.isClientSide || tryDirectUnlink(identifier, posToUnlink))
            return;
        final LinkingUpdates update;
        synchronized (POS_UPDATES) {
            update = POS_UPDATES.computeIfAbsent(new PosIdentifier(posToUnlink, identifier.world),
                    _u -> new LinkingUpdates());
        }
        update.addPosToUnlink(posToUnlink);
    }

    public static void linkTileToPos(final PosIdentifier identifier, final BlockPos posToLink) {
        if (identifier.world.isClientSide || tryDirectLink(identifier, posToLink))
            return;
        final LinkingUpdates update;
        synchronized (POS_UPDATES) {
            update = POS_UPDATES.computeIfAbsent(new PosIdentifier(posToLink, identifier.world),
                    _u -> new LinkingUpdates());
        }
        update.addPosToLink(posToLink);
    }

    private static boolean tryDirectLink(final PosIdentifier identifier, final BlockPos posToLink) {
        if (identifier.world.isClientSide)
            return false;
        final BlockEntity entity = identifier.world.getBlockEntity(posToLink);
        if (entity != null && entity instanceof RedstoneIOTileEntity) {
            ((RedstoneIOTileEntity) entity).link(identifier.pos);
            return true;
        }
        return false;
    }

    private static boolean tryDirectUnlink(final PosIdentifier identifier,
            final BlockPos posToUnlink) {
        if (identifier.world.isClientSide)
            return false;
        final BlockEntity entity = identifier.world.getBlockEntity(posToUnlink);
        if (entity != null && entity instanceof RedstoneIOTileEntity) {
            ((RedstoneIOTileEntity) entity).unlink(identifier.pos);
            return true;
        }
        return false;
    }

    public static void removeSignalBox(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        synchronized (ALL_GRIDS) {
            ALL_GRIDS.remove(identifier);
        }
        synchronized (ALL_LINKED_POS) {
            ALL_LINKED_POS.remove(identifier);
        }
    }

    public static LinkingUpdates getPosUpdates(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return null;
        synchronized (POS_UPDATES) {
            return POS_UPDATES.remove(identifier);
        }
    }

    public static void updateRedstoneOutput(final PosIdentifier identifier, final boolean state) {
        if (identifier.world.isClientSide)
            return;
        BlockState blockState = identifier.world.getBlockState(identifier.pos);
        if (blockState != null && blockState.getBlock() == OSBlocks.REDSTONE_OUT) {
            blockState = blockState.setValue(RedstoneIO.POWER, state);
            identifier.world.setBlockAndUpdate(identifier.pos, blockState);
            return;
        }
        synchronized (OUTPUT_UPDATES) {
            OUTPUT_UPDATES.put(identifier, state);
        }
    }

    public static boolean containsOutputUpdates(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return false;
        synchronized (OUTPUT_UPDATES) {
            return OUTPUT_UPDATES.containsKey(identifier);
        }
    }

    public static boolean getNewOutputState(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return false;
        synchronized (OUTPUT_UPDATES) {
            return OUTPUT_UPDATES.remove(identifier);
        }
    }

    public static void loadSignals(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.loadSignals(identifier.world);
    }

    public static void unloadSignals(final PosIdentifier identifier) {
        if (identifier.world.isClientSide)
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.unloadSignals(identifier.world);
    }

    public static void updatePathwayToAutomatic(final PosIdentifier identifier, final Point point) {
        if (identifier.world.isClientSide)
            return;
        PathwayHolder holder;
        synchronized (ALL_GRIDS) {
            holder = ALL_GRIDS.get(identifier);
        }
        if (holder == null)
            return;
        holder.updatePathwayToAutomatic(point);
    }

    private static final String LINKING_UPDATE = "linkingUpdates";
    private static final String OUTPUT_UPDATE = "ouputUpdates";
    private static final String BOOL_STATE = "boolState";
    private static final Path NBT_FILES_DIRECTORY = Paths.get("osfiles/signalboxhandler");

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save event) {
        final Level world = (Level) event.getWorld();
        if (world.isClientSide)
            return;
        final NBTWrapper wrapper = new NBTWrapper();
        final List<NBTWrapper> wrapperList = new ArrayList<>();
        final String levelName = (((ServerLevel) world).getServer().getWorldData().getLevelName()
                + "_" + world.dimension().location().toString().replace(":", "_"));
        synchronized (POS_UPDATES) {
            POS_UPDATES.forEach((pos, update) -> {
                if (!levelName
                        .equals(((ServerLevel) world).getServer().getWorldData().getLevelName()
                                + "_" + world.dimension().location().toString().replace(":", "_")))
                    return;
                final NBTWrapper posWrapper = NBTWrapper.getBlockPosWrapper(pos.pos);
                update.writeNBT(posWrapper);
                wrapperList.add(posWrapper);
            });
        }
        wrapper.putList(LINKING_UPDATE, wrapperList);
        wrapperList.clear();
        synchronized (OUTPUT_UPDATES) {
            OUTPUT_UPDATES.forEach((pos, state) -> {
                if (!levelName
                        .equals(((ServerLevel) world).getServer().getWorldData().getLevelName()
                                + "_" + world.dimension().location().toString().replace(":", "_")))
                    return;
                final NBTWrapper posWrapper = NBTWrapper.getBlockPosWrapper(pos.pos);
                posWrapper.putBoolean(BOOL_STATE, state);
                wrapperList.add(posWrapper);
            });
        }
        wrapper.putList(OUTPUT_UPDATE, wrapperList);
        try {
            Files.createDirectories(NBT_FILES_DIRECTORY);
            final File file = Paths
                    .get("osfiles/signalboxhandler/",
                            world.getServer().getWorldData().getLevelName().replace("/", "") + "_"
                                    + world.dimension().location().toString().replace(":", "_"))
                    .toFile();
            if (file.exists())
                file.delete();
            Files.createFile(file.toPath());
            NbtIo.write(wrapper.tag, file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(final WorldEvent.Load event) {
        final Level world = (Level) event.getWorld();
        if (world.isClientSide)
            return;
        try {
            Files.createDirectories(NBT_FILES_DIRECTORY);
            final Optional<Path> file = Files.list(NBT_FILES_DIRECTORY)
                    .filter(path -> path.endsWith(
                            ((ServerLevel) world).getServer().getWorldData().getLevelName() + "_"
                                    + world.dimension().location().toString().replace(":", "_")))
                    .findFirst();
            if (file.isEmpty() || !file.get().toFile().exists())
                return;
            final NBTWrapper wrapper = new NBTWrapper(NbtIo.read(file.get().toFile()));
            wrapper.getList(LINKING_UPDATE).forEach(tag -> {
                final LinkingUpdates updates = new LinkingUpdates();
                updates.readNBT(tag);
                synchronized (POS_UPDATES) {
                    final PosIdentifier identifier = new PosIdentifier(tag.getAsPos(), world);
                    POS_UPDATES.put(identifier, updates);
                }
            });
            wrapper.getList(OUTPUT_UPDATE).forEach(tag -> {
                synchronized (OUTPUT_UPDATES) {
                    OUTPUT_UPDATES.put(new PosIdentifier(tag.getAsPos(), world),
                            tag.getBoolean(BOOL_STATE));
                }
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}