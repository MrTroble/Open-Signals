package com.troblecodings.signals.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.LinkedPositions;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.PathGetter;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

    public static PathwayRequestResult requesetInterSignalBoxPathway(final StateInfo startBox,
            final Point start, final Point end) {
        if (startBox.worldNullOrClientSide())
            return PathwayRequestResult.NO_PATH;

        final AtomicReference<PathwayRequestResult> returnResult = new AtomicReference<>();
        final IChunkLoadable chunkLoader = new IChunkLoadable() {
        };
        chunkLoader.loadChunkAndGetTile(SignalBoxTileEntity.class, startBox.world, startBox.pos,
                (startTile, _u) -> {
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
                        returnResult.set(PathwayRequestResult.NO_INTERSIGNALBOX_SELECTED);
                        return;
                    }
                    final Optional<BlockPos> otherPos = outConnectionEntry
                            .getEntry(PathEntryType.SIGNALBOX);
                    final Optional<Point> otherStartPoint = outConnectionEntry
                            .getEntry(PathEntryType.POINT);
                    if (!otherPos.isPresent() || !otherStartPoint.isPresent()) {
                        returnResult.set(PathwayRequestResult.NO_INTERSIGNALBOX_SELECTED);
                        return;
                    }
                    chunkLoader.loadChunkAndGetTile(SignalBoxTileEntity.class, startBox.world,
                            otherPos.get(), (endTile, _u2) -> {
                                final SignalBoxGrid endGrid = endTile.getSignalBoxGrid();
                                final SignalBoxNode otherStartNode = endGrid
                                        .getNode(otherStartPoint.get());
                                if (otherStartNode == null) {
                                    returnResult.set(PathwayRequestResult.NOT_IN_GRID);
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
                                    returnResult
                                            .set(PathwayRequestResult.NO_INTERSIGNALBOX_SELECTED);
                                    return;
                                }
                                final Optional<Point> otherEndPoint = inConnectionEntry
                                        .getEntry(PathEntryType.POINT);
                                if (!otherEndPoint.isPresent()) {
                                    returnResult
                                            .set(PathwayRequestResult.NO_INTERSIGNALBOX_SELECTED);
                                    return;
                                }
                                final PathwayRequestResult startRequeset = startGrid
                                        .requestWay(start, end);
                                final PathwayRequestResult endRequeset = endGrid
                                        .requestWay(otherStartPoint.get(), otherEndPoint.get());
                                final boolean startDone = startRequeset.isPass();
                                final boolean endDone = endRequeset.isPass();

                                if (!startDone || !endDone) {
                                    if (startDone) {
                                        startGrid.resetPathway(start);
                                        returnResult.set(endRequeset);
                                    }
                                    if (endDone) {
                                        endGrid.resetPathway(otherStartPoint.get());
                                        returnResult.set(startRequeset);
                                    }
                                    if (!startDone && !endDone) {
                                        returnResult.set(startRequeset);
                                    }
                                    return;
                                }
                                final SignalBoxPathway startPath = startGrid
                                        .getPathwayByLastPoint(end);
                                final SignalBoxPathway endPath = endGrid
                                        .getPathwayByLastPoint(otherEndPoint.get());
                                startPath.setOtherPathwayToBlock(endPath);
                                endPath.setOtherPathwayToReset(startPath);
                                returnResult.set(PathwayRequestResult.PASS);
                            });
                });
        return returnResult.get();
    }

    public static void writeTileNBT(final StateInfo identifier, final NBTWrapper wrapper) {
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
            return;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(identifier,
                    _u -> new LinkedPositions(identifier.pos));
        }
        holder.read(wrapper);
    }

    public static boolean isTileEmpty(final StateInfo identifier) {
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
            return false;
        LinkedPositions holder;
        synchronized (ALL_LINKED_POS) {
            holder = ALL_LINKED_POS.computeIfAbsent(identifier,
                    _u -> new LinkedPositions(identifier.pos));
        }
        final boolean linked = holder.addLinkedPos(linkPos, identifier.world, type);
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
        if (identifier.worldNullOrClientSide())
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
        if (identifier.world.isRemote)
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
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide() || tryDirectUnlink(identifier, posToUnlink))
            return;
        final LinkingUpdates update;
        synchronized (POS_UPDATES) {
            update = POS_UPDATES.computeIfAbsent(new StateInfo(identifier.world, posToUnlink),
                    _u -> new LinkingUpdates());
        }
        update.addPosToUnlink(posToUnlink);
    }

    public static void linkTileToPos(final StateInfo identifier, final BlockPos posToLink) {
        if (identifier.worldNullOrClientSide() || tryDirectLink(identifier, posToLink))
            return;
        final LinkingUpdates update;
        synchronized (POS_UPDATES) {
            update = POS_UPDATES.computeIfAbsent(new StateInfo(identifier.world, posToLink),
                    _u -> new LinkingUpdates());
        }
        update.addPosToLink(posToLink);
    }

    private static boolean tryDirectLink(final StateInfo identifier, final BlockPos posToLink) {
        if (identifier.worldNullOrClientSide())
            return false;
        final TileEntity entity = identifier.world.getTileEntity(posToLink);
        if (entity != null && entity instanceof RedstoneIOTileEntity) {
            ((RedstoneIOTileEntity) entity).link(identifier.pos);
            return true;
        }
        return false;
    }

    private static boolean tryDirectUnlink(final StateInfo identifier, final BlockPos posToUnlink) {
        if (identifier.worldNullOrClientSide())
            return false;
        final TileEntity entity = identifier.world.getTileEntity(posToUnlink);
        if (entity != null && entity instanceof RedstoneIOTileEntity) {
            ((RedstoneIOTileEntity) entity).unlink(identifier.pos);
            return true;
        }
        return false;
    }

    public static void removeSignalBox(final StateInfo identifier) {
        if (identifier.worldNullOrClientSide())
            return;
        synchronized (ALL_GRIDS) {
            ALL_GRIDS.remove(identifier);
        }
        synchronized (ALL_LINKED_POS) {
            ALL_LINKED_POS.remove(identifier);
        }
    }

    public static LinkingUpdates getPosUpdates(final StateInfo identifier) {
        if (identifier.worldNullOrClientSide())
            return null;
        synchronized (POS_UPDATES) {
            return POS_UPDATES.remove(identifier);
        }
    }

    public static void updateRedstoneOutput(final StateInfo identifier, final boolean state) {
        if (identifier.worldNullOrClientSide())
            return;
        IBlockState blockState = identifier.world.getBlockState(identifier.pos);
        if (blockState != null && blockState.getBlock() == OSBlocks.REDSTONE_OUT) {
            blockState = blockState.withProperty(RedstoneIO.POWER, state);
            identifier.world.setBlockState(identifier.pos, blockState);
            return;
        }
        synchronized (OUTPUT_UPDATES) {
            OUTPUT_UPDATES.put(identifier, state);
        }
    }

    public static boolean containsOutputUpdates(final StateInfo identifier) {
        if (identifier.worldNullOrClientSide())
            return false;
        synchronized (OUTPUT_UPDATES) {
            return OUTPUT_UPDATES.containsKey(identifier);
        }
    }

    public static boolean getNewOutputState(final StateInfo identifier) {
        if (identifier.worldNullOrClientSide())
            return false;
        synchronized (OUTPUT_UPDATES) {
            return OUTPUT_UPDATES.remove(identifier);
        }
    }

    public static void loadSignals(final StateInfo identifier) {
        if (identifier.worldNullOrClientSide())
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
        if (identifier.worldNullOrClientSide())
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
        final World world = (World) event.getWorld();
        if (world.isRemote)
            return;
        final NBTWrapper wrapper = new NBTWrapper();
        final List<NBTWrapper> wrapperList = new ArrayList<>();
        final String levelName = ((WorldServer) world).getMinecraftServer().getFolderName() + "_"
                + ((WorldServer) world).provider.getDimensionType().getName().replace(":", "_");
        synchronized (POS_UPDATES) {
            POS_UPDATES.forEach((pos, update) -> {
                if (!levelName.equals(((WorldServer) world).getMinecraftServer().getFolderName()
                        + "_" + ((WorldServer) world).provider.getDimensionType().getName()
                                .replace(":", "_")))
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
                if (!levelName.equals(((WorldServer) world).getMinecraftServer().getFolderName()
                        + "_" + ((WorldServer) world).provider.getDimensionType().getName()
                                .replace(":", "_")))
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
            if (file.delete() || !Files.exists(file.toPath())) {
                file.createNewFile();
            }
            CompressedStreamTools.write(wrapper.tag, file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(final WorldEvent.Load event) {
        final World world = (World) event.getWorld();
        if (world.isRemote)
            return;
        migrateFilesToNewDirectory(world);
        try {
            final Path newPath = PathGetter.getNewPathForFiles(world, "signalboxhandlerfiles");
            if (!Files.exists(newPath))
                return;
            final NBTWrapper wrapper = new NBTWrapper(CompressedStreamTools.read(newPath.toFile()));
            if (wrapper.isTagNull())
                return;
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

    private static void migrateFilesToNewDirectory(final World world) {
        final Path oldPath = Paths.get("osfiles/signalboxhandler/",
                ((WorldServer) world).getMinecraftServer().getName().replace("/", "") + "_"
                        + ((WorldServer) world).provider.getDimensionType().getName().replace(":",
                                "_"));
        if (!Files.exists(oldPath)) {
            return;
        }
        final Path newPath = PathGetter.getNewPathForFiles(world, "signalboxhandlerfiles");
        try {
            Files.createDirectories(newPath);
            Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            if (Files.isDirectory(oldPath)) {
                Files.list(oldPath).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Files.delete(oldPath);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}