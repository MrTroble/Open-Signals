package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.core.TileEntityInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class SyncableTileEntity extends BasicBlockEntity {

    public SyncableTileEntity(final TileEntityInfo info) {
        super(info);
    }

    protected final ArrayList<UIClientSync> clientSyncs = new ArrayList<>();

    @Override
    public CompoundTag getUpdateTag() {
        final NBTWrapper wrapper = new NBTWrapper();
        saveWrapper(wrapper);
        return wrapper.tag;
    }

    public void syncClient() {
        syncClient(getLevel(), getBlockPos());
    }

    public void syncClient(final Level world, final BlockPos pos) {
        world.setBlockAndUpdate(pos, getBlockState());
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