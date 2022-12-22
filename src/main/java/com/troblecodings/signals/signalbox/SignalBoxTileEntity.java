package com.troblecodings.signals.signalbox;

import static com.troblecodings.signals.signalbox.SignalBoxUtil.POINT1;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.POINT2;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.REQUEST_WAY;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.RESET_WAY;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.fromNBT;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.IChunkloadable;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEnity;
import com.troblecodings.signals.tileentitys.SyncableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
    public void setLevel(final Level worldIn) {
        super.setLevel(worldIn);
        worldLoadOps = SignalBoxFactory.getFactory().getLevelOperations(worldIn);
    }

    @Override
    public CompoundTag writeToNBT(final CompoundTag compound) {
        final ListTag list = new ListTag();
        linkedBlocks.forEach((p, t) -> {
            final CompoundTag item = NBTUtil.createPosTag(p);
            item.putString(LINK_TYPE, t.name());
            list.add(item);
        });
        compound.put(LINKED_POS_LIST, list);
        final CompoundTag gridTag = new CompoundTag();
        this.grid.write(gridTag);
        compound.put(GUI_TAG, gridTag);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final CompoundTag compound) {
        super.readFromNBT(compound);
        final ListTag list = (ListTag) compound.get(LINKED_POS_LIST);
        if (list != null) {
            linkedBlocks.clear();
            list.forEach(pos -> {
                final CompoundTag item = (CompoundTag) pos;
                if (item.hasKey(LINK_TYPE))
                    linkedBlocks.put(NBTUtil.getPosFromTag(item),
                            LinkType.valueOf(item.getString(LINK_TYPE)));
            });
        }
        final CompoundTag gridComp = compound.getCompoundTag(GUI_TAG);
        grid.read(gridComp);
        if (level != null) {
            onLoad();
        }
    }

    @Override
    public void updateTag(final CompoundTag compound) {
        this.markDirty();
        if (compound == null)
            return;
        if (compound.hasKey(REMOVE_SIGNAL)) {
            final CompoundTag request = (CompoundTag) compound.get(REMOVE_SIGNAL);
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
            final CompoundTag request = (CompoundTag) compound.get(RESET_WAY);
            final Point p1 = fromNBT(request, POINT1);
            grid.resetPathway(p1);
            this.syncClient();
            return;
        }
        if (compound.hasKey(REQUEST_WAY)) {
            final CompoundTag request = (CompoundTag) compound.get(REQUEST_WAY);
            final Point p1 = fromNBT(request, POINT1);
            final Point p2 = fromNBT(request, POINT2);
            if (!grid.requestWay(p1, p2)) {
                final CompoundTag error = new CompoundTag();
                error.putString(ERROR_STRING, "error.nopathfound");
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
    public CompoundTag getTag() {
        final CompoundTag gridComp = new CompoundTag();
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
        final BlockState state = world.getBlockState(linkedPos);
        final Block block = state.getBlock();
        LinkType type = LinkType.SIGNAL;
        if (block == OSBlocks.REDSTONE_IN) {
            type = LinkType.INPUT;
            if (level.isClientSide)
                loadChunkAndGetTile(RedstoneIOTileEntity.class, level, linkedPos,
                        (tile, _u) -> tile.link(this.worldPosition));
        } else if (block == OSBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        }
        if (level.isClientSide) {
            if (type.equals(LinkType.SIGNAL)) {
                loadChunkAndGetTile(SignalTileEnity.class, level, linkedPos, this::updateSingle);
                worldLoadOps.loadAndReset(linkedPos);
            }
        }
        linkedBlocks.put(linkedPos, type);
        this.syncClient();
        return true;
    }

    private void updateSingle(final SignalTileEnity signaltile, final Chunk unused) {
        final BlockPos signalPos = signaltile.getBlockPos();
        signals.put(signalPos, signaltile.getSignal());
        syncClient();
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide)
            return;
        signals.clear();
        new Thread(() -> {
            linkedBlocks.forEach((linkedPos, _u) -> loadChunkAndGetTile(SignalTileEnity.class,
                    level, linkedPos, this::updateSingle));
        }).start();
    }

    @Override
    public boolean unlink() {
        signals.keySet().forEach(worldLoadOps::loadAndReset);
        linkedBlocks.entrySet().stream().filter(entry -> !LinkType.SIGNAL.equals(entry.getValue()))
                .forEach(entry -> {
                    loadChunkAndGetTile(RedstoneIOTileEntity.class, level, entry.getKey(),
                            (tile, _u) -> tile.unlink(worldPosition));
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
        if (power && this.level.isClientSide) {
            grid.setPowered(pos);
            syncClient();
        }
    }

    public boolean isBlocked() {
        return !this.clientSyncs.isEmpty();
    }

    @Override
    public boolean isValid(final Player player) {
        if (clientSyncs.isEmpty())
            return false;
        return this.clientSyncs.get(0).getPlayer().equals(player);
    }
}
