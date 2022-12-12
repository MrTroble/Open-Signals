package com.troblecodings.signals.blocks;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneInput extends RedstoneIO {

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos) {
        if (worldIn.isRemote)
            return;
        if (worldIn.isBlockPowered(pos)) {
            if (state.getValue(RedstoneIO.POWER) != true) {
                worldIn.setBlockState(pos, state.withProperty(RedstoneIO.POWER, true));
                final TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof RedstoneIOTileEntity)
                    ((RedstoneIOTileEntity) entity).sendToAll();
            }
        } else {
            worldIn.setBlockState(pos, state.withProperty(RedstoneIO.POWER, false));
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
