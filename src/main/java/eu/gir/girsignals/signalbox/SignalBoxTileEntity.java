package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.REQUEST_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.RESET_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.fromNBT;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;

import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.LinkType;
import eu.gir.girsignals.init.SignalBlocks;
import eu.gir.girsignals.signalbox.debug.SignalBoxFactory;
import eu.gir.girsignals.tileentitys.IChunkloadable;
import eu.gir.girsignals.tileentitys.RedstoneIOTileEntity;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import eu.gir.girsignals.tileentitys.SyncableTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class SignalBoxTileEntity extends SyncableTileEntity
        implements ISyncable, IChunkloadable, ILinkableTile {

    public static final String ERROR_STRING = "error";
    public static final String REMOVE_SIGNAL = "removeSignal";

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String GUI_TAG = "guiTag";
    private static final String LINK_TYPE = "linkType";

    private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
    private final Map<BlockPos, Signal> signals = new HashMap<>();
    private final SignalBoxGrid grid;

    public SignalBoxTileEntity() {
        grid = SignalBoxFactory.getFactory().getGrid(this::sendToAll);
    }

    private WorldOperations worldLoadOps = new WorldOperations();

    @Override
    public void setWorld(final World worldIn) {
        super.setWorld(worldIn);
        worldLoadOps = SignalBoxFactory.getFactory().getWorldOperations(worldIn);
        grid.setWorld(worldIn);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        final NBTTagList list = new NBTTagList();
        linkedBlocks.forEach((p, t) -> {
            final NBTTagCompound item = NBTUtil.createPosTag(p);
            item.setString(LINK_TYPE, t.name());
            list.appendTag(item);
        });
        compound.setTag(LINKED_POS_LIST, list);
        final NBTTagCompound gridTag = new NBTTagCompound();
        this.grid.write(gridTag);
        compound.setTag(GUI_TAG, gridTag);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
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
        final NBTTagCompound gridComp = compound.getCompoundTag(GUI_TAG);
        grid.read(gridComp);
        if (world != null) {
            onLoad();
        }
    }

    @Override
    public void updateTag(final NBTTagCompound compound) {
        this.markDirty();
        if (compound == null)
            return;
        if (compound.hasKey(REMOVE_SIGNAL)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(REMOVE_SIGNAL);
            final BlockPos p1 = NBTUtil.getPosFromTag(request);
            if (signals.containsKey(p1)) {
                signals.remove(p1);
                worldLoadOps.loadAndReset(p1);
            }
            linkedBlocks.remove(p1);
            this.syncClient();
            return;
        }
        if (compound.hasKey(RESET_WAY)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(RESET_WAY);
            final Point p1 = fromNBT(request, POINT1);
            grid.resetPathway(p1);
            this.syncClient();
            return;
        }
        if (compound.hasKey(REQUEST_WAY)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(REQUEST_WAY);
            final Point p1 = fromNBT(request, POINT1);
            final Point p2 = fromNBT(request, POINT2);
            if (!grid.requestWay(p1, p2)) {
                final NBTTagCompound error = new NBTTagCompound();
                error.setString(ERROR_STRING, "error.nopathfound");
                sendToAll(error);
            } else {
                this.syncClient();
            }
            return;
        }
        this.grid.readEntryNetwork(compound);
        this.syncClient();
    }

    @Override
    public NBTTagCompound getTag() {
        final NBTTagCompound gridComp = new NBTTagCompound();
        this.grid.writeEntryNetwork(gridComp, true);
        return gridComp;
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
        if (block == SignalBlocks.REDSTONE_IN) {
            type = LinkType.INPUT;
            if (!world.isRemote)
                loadChunkAndGetTile(RedstoneIOTileEntity.class, world, linkedPos,
                        (tile, _u) -> tile.link(this.pos));
        } else if (block == SignalBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        }
        if (!world.isRemote) {
            if (type.equals(LinkType.SIGNAL)) {
                loadChunkAndGetTile(SignalTileEnity.class, world, linkedPos, this::updateSingle);
                worldLoadOps.loadAndReset(linkedPos);
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
        signals.keySet().forEach(worldLoadOps::loadAndReset);
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

    public void updateRedstonInput(final BlockPos pos, final boolean power) {
        if (power && !this.world.isRemote) {
            grid.setPowered(pos);
            syncClient();
        }
    }

    public boolean isBlocked() {
        return !this.clientSyncs.isEmpty();
    }

    @Override
    public boolean isValid(final EntityPlayer player) {
        if (clientSyncs.isEmpty())
            return false;
        return this.clientSyncs.get(0).getPlayer().equals(player);
    }
}
