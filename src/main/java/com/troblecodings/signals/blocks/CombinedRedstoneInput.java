package com.troblecodings.signals.blocks;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CombinedRedstoneInput extends RedstoneInput {

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        if (worldIn.isClientSide)
            return;
        final boolean currentState = state.getValue(POWER);
        final boolean hasNeighborSignal = worldIn.hasNeighborSignal(pos);
        final RedstoneIOTileEntity tile = (RedstoneIOTileEntity) worldIn.getBlockEntity(pos);
        if (currentState && !hasNeighborSignal) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWER, false));
            tile.sendToAll();
        } else if (hasNeighborSignal && !currentState) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWER, true));
            tile.sendToAll();
        } else if (!hasNeighborSignal) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWER, false));
        }
    }
}