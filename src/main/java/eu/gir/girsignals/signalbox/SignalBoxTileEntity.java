package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.REQUEST_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.RESET_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.fromNBT;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.requestWay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableMap;

import eu.gir.girsignals.blocks.RedstoneIO;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.LinkType;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.tileentitys.IChunkloadable;
import eu.gir.girsignals.tileentitys.RedstoneIOTileEntity;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import eu.gir.girsignals.tileentitys.SyncableTileEntity;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.interfaces.ISyncable;
import eu.gir.linkableapi.ILinkableTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class SignalBoxTileEntity extends SyncableTileEntity
        implements ISyncable, IChunkloadable, ILinkableTile {

    public static final String ERROR_STRING = "error";
    public static final String REMOVE_SIGNAL = "removeSignal";

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String GUI_TAG = "guiTag";
    private static final String LINK_TYPE = "linkType";

    private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
    private final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    private final Map<BlockPos, Signal> signals = new HashMap<>();
    private NBTTagCompound guiTag = new NBTTagCompound();

    private final List<SignalBoxPathway> pathWayEnd = new ArrayList<>(32);
    private final Map<SignalBoxPathway, SignalBoxPathway> previousPathways = new HashMap<>(32);

    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        final NBTTagList list = new NBTTagList();
        linkedBlocks.forEach((p, t) -> {
            final NBTTagCompound item = NBTUtil.createPosTag(p);
            item.setString(LINK_TYPE, t.name());
            list.appendTag(item);
        });
        compound.setTag(LINKED_POS_LIST, list);
        compound.setTag(GUI_TAG, guiTag);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        final NBTTagList list = (NBTTagList) compound.getTag(LINKED_POS_LIST);
        if (list != null) {
            linkedBlocks.clear();
            list.forEach(pos -> {
                final NBTTagCompound item = (NBTTagCompound) pos;
                if (item.hasKey(LINK_TYPE))
                    linkedBlocks.put(NBTUtil.getPosFromTag(item),
                            LinkType.valueOf(item.getString(LINK_TYPE)));
            });
        }
        this.guiTag = compound.getCompoundTag(GUI_TAG);
        this.updateModeGridFromUI();
        super.readFromNBT(compound);
        if (world != null)
            onLoad();
    }

    private void updateModeGridFromUI() {
        modeGrid.clear();
        this.guiTag.getKeySet().forEach(key -> {
            final String[] names = key.split("\\.");
            if (names.length < 2)
                return;
            final int x = Integer.parseInt(names[0]);
            final int y = Integer.parseInt(names[1]);
            final SignalBoxNode node = new SignalBoxNode(new Point(x, y));
            node.read(this.guiTag);
            node.post();
            modeGrid.put(node.getPoint(), node);
        });
    }

    private void loadAndConfig(final int speed, final BlockPos lastPosition,
            final BlockPos nextPosition) {
        loadAndConfig(speed, lastPosition, nextPosition, null);
    }

    private void config(final int speed, final SignalTileEnity lastTile,
            final SignalTileEnity nextTile, final ISignalAutoconfig override) {
        final Signal last = lastTile.getSignal();
        final ISignalAutoconfig config = override == null ? last.getConfig() : override;
        if (config == null)
            return;
        final ConfigInfo info = new ConfigInfo(lastTile, nextTile, speed);
        config.change(info);
    }

    private void loadAndConfig(final int speed, final BlockPos lastPosition,
            final BlockPos nextPosition, final ISignalAutoconfig override) {
        loadChunkAndGetTile(SignalTileEnity.class, world, lastPosition, (lastTile, chunk) -> {
            if (nextPosition == null) {
                config(speed, lastTile, null, override);
            } else {
                loadChunkAndGetTile(SignalTileEnity.class, world, nextPosition,
                        (nextTile, _u2) -> config(speed, lastTile, nextTile, override));
            }
            this.syncClient(world, lastPosition);
        });
    }

    private void loadAndReset(final BlockPos position) {
        loadChunkAndGetTile(SignalTileEnity.class, world, position, (signaltile, chunk) -> {
            final ISignalAutoconfig config = signaltile.getSignal().getConfig();
            if (config == null)
                return;
            config.reset(signaltile);
            this.syncClient(world, position);
        });
    }

    private void sendGuiTag() {
        this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(guiTag, ui.getPlayer()));
    }

    private void resendSignalTilesToUI() {
        modeGrid.values().forEach(signal -> signal.write(guiTag));
        sendGuiTag();
    }

    private void setPower(final BlockPos position, final boolean power) {
        if (position == null)
            return;
        loadChunkAndGetBlock(world, position, (state, chunk) -> {
            if (!(state.getBlock() instanceof RedstoneIO))
                return;
            final IBlockState ibstate = state.withProperty(RedstoneIO.POWER, power);
            chunk.setBlockState(position, ibstate);
            this.syncClient(world, position);
        });
    }

    private void onWayAdd(final ArrayList<SignalBoxNode> nodes, final PathType type) {
        final AtomicInteger atomic = new AtomicInteger(Integer.MAX_VALUE);
        for (int i = 1; i < nodes.size() - 1; i++) {
            final Point oldPos = nodes.get(i - 1).getPoint();
            final Point newPos = nodes.get(i + 1).getPoint();
            final SignalBoxNode current = nodes.get(i);
            current.getOption(new Path(oldPos, newPos)).ifPresent(option -> {
                option.getEntry(PathEntryType.OUTPUT).ifPresent(pos -> setPower(pos, true));
                option.getEntry(PathEntryType.SPEED).ifPresent(
                        speed -> atomic.getAndUpdate(oldspeed -> Math.min(oldspeed, speed)));
                option.setEntry(PathEntryType.PATHUSAGE, EnumPathUsage.SELECTED);
            });
        }
        final SignalBoxNode node1 = nodes.get(nodes.size() - 1);
        final SignalBoxNode node2 = nodes.get(0);
        final SignalBoxPathway pathway = new SignalBoxPathway(nodes,
                new ConfigInfo(null, null, atomic.get()));
        pathWayEnd.add(pathway);
        resendSignalTilesToUI();
    }

    @Override
    public void updateTag(final NBTTagCompound compound) {
        if (compound == null)
            return;
        if (compound.hasKey(REMOVE_SIGNAL)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(REMOVE_SIGNAL);
            final BlockPos p1 = NBTUtil.getPosFromTag(request);
            if (signals.containsKey(p1)) {
                signals.remove(p1);
                loadAndReset(p1);
            }
            linkedBlocks.remove(p1);
        }
        if (compound.hasKey(RESET_WAY)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(RESET_WAY);
            final Point p1 = fromNBT(request, POINT1);
            // TODO Reset
            return;
        }
        if (compound.hasKey(REQUEST_WAY)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(REQUEST_WAY);
            final Point p1 = fromNBT(request, POINT1);
            final Point p2 = fromNBT(request, POINT2);
            final Optional<ArrayList<SignalBoxNode>> ways = requestWay(modeGrid, p1, p2);
            if (ways.isPresent()) {
                // TODO reset way
                // this.onWayAdd(ways.get());
            } else {
                final NBTTagCompound update = new NBTTagCompound();
                update.setString(ERROR_STRING, "error.nopathfound");
                this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(update, ui.getPlayer()));
            }
            return;
        }
        this.guiTag = compound;
        this.syncClient();
        updateModeGridFromUI();
    }

    @Override
    public NBTTagCompound getTag() {
        return this.guiTag;
    }

    @Override
    public boolean hasLink() {
        return !linkedBlocks.isEmpty();
    }

    @Override
    public boolean link(final BlockPos linkedPos) {
        if (linkedBlocks.containsKey(linkedPos))
            return false;
        final IBlockState state = world.getBlockState(linkedPos);
        final Block block = state.getBlock();
        LinkType type = LinkType.SIGNAL;
        if (block == GIRBlocks.REDSTONE_IN) {
            type = LinkType.INPUT;
            if (!world.isRemote)
                loadChunkAndGetTile(RedstoneIOTileEntity.class, world, linkedPos,
                        (tile, _u) -> tile.link(this.pos));
        } else if (block == GIRBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        }
        if (!world.isRemote) {
            if (type.equals(LinkType.SIGNAL)) {
                loadChunkAndGetTile(SignalTileEnity.class, world, linkedPos, this::updateSingle);
                loadAndReset(linkedPos);
            }
        }
        linkedBlocks.put(linkedPos, type);
        this.syncClient();
        return true;
    }

    private void updateSingle(final SignalTileEnity signaltile, final Chunk unused) {
        final BlockPos signalPos = signaltile.getPos();
        signals.put(signalPos, signaltile.getSignal());
        syncClient();
    }

    @Override
    public void onLoad() {
        if (world.isRemote)
            return;
        signals.clear();
        new Thread(() -> {
            linkedBlocks.forEach((linkedPos, _u) -> loadChunkAndGetTile(SignalTileEnity.class,
                    world, linkedPos, this::updateSingle));
        }).start();
    }

    @Override
    public boolean unlink() {
        signals.keySet().forEach(this::loadAndReset);
        linkedBlocks.entrySet().stream().filter(entry -> !LinkType.SIGNAL.equals(entry.getValue()))
                .forEach(entry -> {
                    loadChunkAndGetTile(RedstoneIOTileEntity.class, world, entry.getKey(),
                            (tile, _u) -> tile.unlink(pos));
                });
        linkedBlocks.clear();
        signals.clear();
        syncClient();
        return true;
    }

    public Signal getSignal(final BlockPos pos) {
        return this.signals.get(pos);
    }

    public ImmutableMap<BlockPos, LinkType> getPositions() {
        return ImmutableMap.copyOf(this.linkedBlocks);
    }

    public boolean isEmpty() {
        return this.modeGrid.isEmpty();
    }

    private void lockPathway(final ArrayList<SignalBoxNode> pathway) {
        final SignalBoxNode node = pathway.get(pathway.size() - 1);
        final Point lastPoint = node.getPoint();
        final Point delta = lastPoint.delta(pathway.get(pathway.size() - 2).getPoint());
        final Rotation rotation = SignalBoxUtil.getRotationFromDelta(delta);
        node.getOption(new ModeSet(EnumGuiMode.HP, rotation)).ifPresent(optionEntry -> optionEntry
                .getEntry(PathEntryType.SIGNAL).ifPresent(this::loadAndReset));
        for (int i = 1; i < pathway.size() - 1; i++) {
            final Point oldPos = pathway.get(i - 1).getPoint();
            final Point newPos = pathway.get(i + 1).getPoint();
            final SignalBoxNode current = pathway.get(i);
            current.getOption(new Path(oldPos, newPos)).ifPresent(
                    option -> option.setEntry(PathEntryType.PATHUSAGE, EnumPathUsage.BLOCKED));
            current.write(guiTag);
        }
        this.sendGuiTag();
    }

    // TODO Redstone input

}
