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
import java.util.concurrent.atomic.AtomicBoolean;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.ChunkLoadable;
import com.troblecodings.signals.core.LinkedPositions;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.PathGetter;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SignalBoxHandler {

    private SignalBoxHandler() {
    }

    private static final Map<StateInfo, SignalBoxGrid> ALL_GRIDS = new HashMap<>();
    private static final Map<StateInfo, LinkedPositions> ALL_LINKED_POS = new HashMap<>();
    private static final Map<StateInfo, LinkingUpdates> POS_UPDATES = new HashMap<>();
    private static final Map<StateInfo, Boolean> OUTPUT_UPDATES = new HashMap<>();

    public static void putGrid(final StateInfo info, final SignalBoxGrid grid) {
        synchronized (ALL_GRIDS) {
            ALL_GRIDS.put(info, grid);
        }
    }

    public static SignalBoxGrid getGrid(final StateInfo info) {
        synchronized (ALL_GRIDS) {
            return ALL_GRIDS.get(info);
        }
    }

    public static boolean requesetInterSignalBoxPathway(final StateInfo startBox, final Point start,
            final Point end) {
        if (startBox.isWorldNullOrClientSide())
            return false;

        final AtomicBoolean returnBoolean = new AtomicBoolean(true);
        final ChunkLoadable chunkLoader = new ChunkLoadable();
        chunkLoader.loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) startBox.world,
                startBox.pos, (startTile, _u) -> {
                    final SignalBoxGrid startGrid = startTile.getSignalBoxGrid();
                    final SignalBoxNode endNode = startGrid.getNode(end);
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
                        returnBoolean.set(false);
                        return;
                    }
                    final Optional<BlockPos> otherPos = outConnectionEntry
                            .getEntry(PathEntryType.SIGNALBOX);
                    final Optional<Point> otherStartPoint = outConnectionEntry
                            .getEntry(PathEntryType.POINT);
                    if (!otherPos.isPresent() || !otherStartPoint.isPresent()) {
                        returnBoolean.set(false);
                        return;
                    }
                    chunkLoader.loadChunkAndGetTile(SignalBoxTileEntity.class,
                            (ServerLevel) startBox.world, otherPos.get(), (endTile, _u2) -> {
                                final SignalBoxGrid endGrid = endTile.getSignalBoxGrid();
                                final SignalBoxNode otherStartNode = endGrid
                                        .getNode(otherStartPoint.get());
                                if (otherStartNode == null) {
                                    returnBoolean.set(false);
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
                                    returnBoolean.set(false);
                                    return;
                                }
                                final Optional<Point> otherEndPoint = inConnectionEntry
                                        .getEntry(PathEntryType.POINT);
                                if (!otherEndPoint.isPresent()) {
                                    returnBoolean.set(false);
                                    return;
                                }
                                final boolean startRequeset = startGrid.requestWay(start, end);
                                final boolean endRequeset = endGrid
                                        .requestWay(otherStartPoint.get(), otherEndPoint.get());
                                if (!startRequeset || !endRequeset) {
                                    if (startRequeset)
                                        startGrid.resetPathway(start);
                                    if (endRequeset)
                                        endGrid.resetPathway(otherStartPoint.get());
                                    returnBoolean.set(false);
                                    return;
                                }
                                final SignalBoxPathway startPath = startGrid
                                        .getPathwayByLastPoint(end);
                                final SignalBoxPathway endPath = endGrid
                                        .getPathwayByLastPoint(otherEndPoint.get());
                                startPath.setOtherPathwayToBlock(endPath);
                                endPath.setOtherPathwayToReset(startPath);
                            });
                });
        return returnBoolean.get();
    }

    public static void writeTileNBT(final StateInfo identifier, final NBTWrapper wrapper) {
        if (identifier.isWorldNullOrClientSide())
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.write(wrapper);
    }

    public static void readTileNBT(final StateInfo identifier, final NBTWrapper wrapper) {
        if (identifier.isWorldNullOrClientSide())
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(identifier,
                    _u -> new LinkedPositions(identifier.pos));
        }
        holder.read(wrapper);
    }

    public static boolean isTileEmpty(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return true;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return true;
        return holder.isEmpty();
    }

    public static boolean linkPosToSignalBox(final StateInfo identifier, final BlockPos linkPos,
            final BasicBlock block, final LinkType type) {
        if (identifier.isWorldNullOrClientSide())
            return false;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(identifier,
                    _u -> new LinkedPositions(identifier.pos));
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

    public static void relinkAllRedstoneIOs(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
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
            final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return new HashMap<>();
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return new HashMap<>();
        return holder.getValidSubsidiariesForPos();
    }

    public static Signal getSignal(final StateInfo identifier, final BlockPos signalPos) {
        if (identifier.isWorldNullOrClientSide())
            return null;
        final LinkedPositions signals;
        synchronized (ALL_LINKED_POS) {
            signals = ALL_LINKED_POS.get(identifier);
        }
        if (signals == null)
            return null;
        return signals.getSignal(signalPos);
    }

    public static void unlinkPosFromSignalBox(final StateInfo identifier, final BlockPos pos) {
        if (identifier.isWorldNullOrClientSide())
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.removeLinkedPos(pos, identifier.world);
    }

    public static Map<BlockPos, LinkType> getAllLinkedPos(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return new HashMap<>();
        final LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return new HashMap<>();
        return holder.getAllLinkedPos();
    }

    public static void onPosRemove(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return;
        synchronized (ALL_LINKED_POS) {
            ALL_LINKED_POS.forEach((pos, holder) -> {
                if (pos.world.equals(identifier.world))
                    holder.removeLinkedPos(identifier.pos, identifier.world);
            });
        }
        synchronized (POS_UPDATES) {
            POS_UPDATES.remove(identifier);
        }
    }

    public static void unlinkAll(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return;
        LinkedPositions allPos;
        synchronized (ALL_LINKED_POS) {
            allPos = ALL_LINKED_POS.get(identifier);
        }
        if (allPos == null)
            return;
        allPos.unlink(identifier.pos, identifier.world);
    }

    public static void unlinkTileFromPos(final StateInfo identifier, final BlockPos posToUnlink) {
        if (identifier.isWorldNullOrClientSide() || tryDirectUnlink(identifier, posToUnlink))
            return;
        final LinkingUpdates update;
        synchronized (POS_UPDATES) {
            update = POS_UPDATES.computeIfAbsent(new StateInfo(identifier.world, posToUnlink),
                    _u -> new LinkingUpdates());
        }
        update.addPosToUnlink(posToUnlink);
    }

    public static void linkTileToPos(final StateInfo identifier, final BlockPos posToLink) {
        if (identifier.isWorldNullOrClientSide() || tryDirectLink(identifier, posToLink))
            return;
        final LinkingUpdates update;
        synchronized (POS_UPDATES) {
            update = POS_UPDATES.computeIfAbsent(new StateInfo(identifier.world, posToLink),
                    _u -> new LinkingUpdates());
        }
        update.addPosToLink(posToLink);
    }

    private static boolean tryDirectLink(final StateInfo identifier, final BlockPos posToLink) {
        if (identifier.isWorldNullOrClientSide())
            return false;
        final BlockEntity entity = identifier.world.getBlockEntity(posToLink);
        if (entity != null && entity instanceof RedstoneIOTileEntity) {
            ((RedstoneIOTileEntity) entity).link(identifier.pos);
            return true;
        }
        return false;
    }

    private static boolean tryDirectUnlink(final StateInfo identifier, final BlockPos posToUnlink) {
        if (identifier.isWorldNullOrClientSide())
            return false;
        final BlockEntity entity = identifier.world.getBlockEntity(posToUnlink);
        if (entity != null && entity instanceof RedstoneIOTileEntity) {
            ((RedstoneIOTileEntity) entity).unlink(identifier.pos);
            return true;
        }
        return false;
    }

    public static void removeSignalBox(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return;
        synchronized (ALL_LINKED_POS) {
            ALL_LINKED_POS.remove(identifier);
        }
        synchronized (ALL_GRIDS) {
            ALL_GRIDS.remove(identifier);
        }
    }

    public static LinkingUpdates getPosUpdates(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return null;
        synchronized (POS_UPDATES) {
            return POS_UPDATES.remove(identifier);
        }
    }

    public static void updateRedstoneOutput(final StateInfo identifier, final boolean state) {
        if (identifier.isWorldNullOrClientSide())
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

    public static boolean containsOutputUpdates(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return false;
        synchronized (OUTPUT_UPDATES) {
            return OUTPUT_UPDATES.containsKey(identifier);
        }
    }

    public static boolean getNewOutputState(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return false;
        synchronized (OUTPUT_UPDATES) {
            return OUTPUT_UPDATES.remove(identifier);
        }
    }

    public static void loadSignals(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.loadSignals(identifier.world);
    }

    public static void unloadSignals(final StateInfo identifier) {
        if (identifier.isWorldNullOrClientSide())
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.get(identifier);
        }
        if (holder == null)
            return;
        holder.unloadSignals(identifier.world);
    }

    private static final String LINKING_UPDATE = "linkingUpdates";
    private static final String OUTPUT_UPDATE = "ouputUpdates";
    private static final String BOOL_STATE = "boolState";

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
            final File file = PathGetter.getNewPathForFiles(world, "signalboxhandlerfiles")
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
        migrateFilesToNewDirectory(world);
        try {
            final Path newPath = PathGetter.getNewPathForFiles(world, "signalboxhandlerfiles");
            final NBTWrapper wrapper = new NBTWrapper(NbtIo.read(newPath.toFile()));
            wrapper.getList(LINKING_UPDATE).forEach(tag -> {
                final LinkingUpdates updates = new LinkingUpdates();
                updates.readNBT(tag);
                synchronized (POS_UPDATES) {
                    final StateInfo identifier = new StateInfo(world, tag.getAsPos());
                    POS_UPDATES.put(identifier, updates);
                }
            });
            wrapper.getList(OUTPUT_UPDATE).forEach(tag -> {
                synchronized (OUTPUT_UPDATES) {
                    OUTPUT_UPDATES.put(new StateInfo(world, tag.getAsPos()),
                            tag.getBoolean(BOOL_STATE));
                }
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateFilesToNewDirectory(final Level world) {
        final Path oldPath = Paths.get("osfiles/signalboxhandler/",
                world.getServer().getWorldData().getLevelName().replace("/", "") + "_"
                        + world.dimension().location().toString().replace(":", "_"));
        if (!Files.exists(oldPath)) {
            return;
        }
        final Path newPath = PathGetter.getNewPathForFiles(world, "signalboxhandlerfiles");
        try {
            Files.copy(oldPath, newPath);
            if (Files.isDirectory(oldPath)) {
                Files.list(oldPath).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            Files.deleteIfExists(oldPath);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}