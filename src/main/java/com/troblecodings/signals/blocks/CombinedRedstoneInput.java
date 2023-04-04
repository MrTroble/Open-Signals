package com.troblecodings.signals.blocks;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CombinedRedstoneInput extends RedstoneInput {

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        if (worldIn.isClientSide)
            return;
        final boolean currentState = state.getValue(POWER);
        final boolean hasNeighborSignal = worldIn.hasNeighborSignal(pos);
        if (currentState && !hasNeighborSignal) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWER, false));
            ((RedstoneIOTileEntity) worldIn.getBlockEntity(pos)).sendToAll();
        } else if (hasNeighborSignal && !currentState) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWER, true));
            ((RedstoneIOTileEntity) worldIn.getBlockEntity(pos)).sendToAll();
        } else if (!hasNeighborSignal) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWER, false));
        }
    }
}