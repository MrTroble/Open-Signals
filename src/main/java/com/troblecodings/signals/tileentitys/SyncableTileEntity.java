package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;

import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SyncableTileEntity extends BasicBlockEntity {

    public SyncableTileEntity(final BlockEntityType<?> p_155228_, final BlockPos p_155229_,
            final BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    protected final ArrayList<UIClientSync> clientSyncs = new ArrayList<>();

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    	super.onDataPacket(net, pkt);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
    	NBTWrapper wrapper = new NBTWrapper();
    	saveWrapper(wrapper);
        return wrapper.tag;
    }

    public void syncClient() {
        syncClient(getLevel(), getBlockPos());
    }

    public void syncClient(final Level world, final BlockPos pos) {
        // TODO Client notification
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
