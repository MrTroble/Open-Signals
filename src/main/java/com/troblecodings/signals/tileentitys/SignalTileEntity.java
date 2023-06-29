package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;

public class SignalTileEntity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    @Override
    public boolean isValid(final EntityPlayer player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        getSignal().renderOverlay(info.with(this));
    }

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty() ? getSignal().getSignalTypeName() : name;
    }

    public Signal getSignal() {
        return (Signal) getBlockType();
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            final IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }
}