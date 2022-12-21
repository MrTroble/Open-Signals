package com.troblecodings.signals.blocks;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneInput extends RedstoneIO {

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        if (worldIn.isClientSide)
            return;
        if (worldIn.hasNeighborSignal(pos)) {
            if (state.getValue(RedstoneIO.POWER) != true) {
                worldIn.setBlockAndUpdate(pos, state.setValue(RedstoneIO.POWER, true));
                final BlockEntity entity = worldIn.getBlockEntity(pos);
                if (entity instanceof RedstoneIOTileEntity)
                    ((RedstoneIOTileEntity) entity).sendToAll();
            }
        } else {
            worldIn.setBlockAndUpdate(pos, state.setValue(RedstoneIO.POWER, false));
        }
    }

    @Override
    public int getWeakPower(final BlockState blockState, final LevelAccessor blockAccess,
            final BlockPos pos, final Direction side) {
        return 0;
    }

    @Override
    public boolean canProvidePower(final BlockState state) {
        return true;
    }
}
