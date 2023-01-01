package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.core.TileEntityInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;

public class SyncableTileEntity extends BasicBlockEntity {

    public SyncableTileEntity(final TileEntityInfo info) {
        super(info);
    }

    protected final ArrayList<UIClientSync> clientSyncs = new ArrayList<>();

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt) {
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
