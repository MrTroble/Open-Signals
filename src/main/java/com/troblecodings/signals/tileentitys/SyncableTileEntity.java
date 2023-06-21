package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SyncableTileEntity extends BasicBlockEntity {

    public SyncableTileEntity() {
        super();
    }

    protected final ArrayList<UIClientSync> clientSyncs = new ArrayList<>();
    
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        final NBTWrapper wrapper = new NBTWrapper();
        saveWrapper(wrapper);
        return wrapper.tag;
    }

    @Override
    public void syncClient() {
        syncClient(getWorld(), getPos());
    }

    @Override
    public void syncClient(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public boolean add(final UIClientSync sync) {
        return this.clientSyncs.add(sync);
    }

    public boolean remove(final UIClientSync sync) {
        return this.clientSyncs.removeIf(s -> s.getPlayer().equals(sync.getPlayer()));
    }

    public UIClientSync get(final int id) {
        return clientSyncs.get(id);
    }
}