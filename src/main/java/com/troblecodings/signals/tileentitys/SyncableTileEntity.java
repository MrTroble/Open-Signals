package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;

import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SyncableTileEntity extends BlockEntity {

    protected final ArrayList<UIClientSync> clientSyncs = new ArrayList<>();

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        final BlockPos pos = getPos();
        getLevel().markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return writeToNBT(new CompoundTag());
    }

    public void syncClient() {
        syncClient(getLevel(), getPos());
    }

    public void syncClient(final Level world, final BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public boolean add(final UIClientSync sync) {
        return this.clientSyncs.add(sync);
    }

    public boolean remove(final UIClientSync sync) {
        return this.clientSyncs.removeIf(s -> s.getPlayer().equals(sync.getPlayer()));
    }

    public void sendToAll(final CompoundTag compound) {
        this.clientSyncs.forEach(sync -> GuiSyncNetwork.sendToClient(compound, sync.getPlayer()));
    }

}
