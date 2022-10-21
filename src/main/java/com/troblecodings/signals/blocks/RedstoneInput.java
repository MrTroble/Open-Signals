package com.troblecodings.signals.blocks;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneInput extends RedstoneIO {

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos,
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
    public int getWeakPower(final IBlockState blockState, final IBlockAccess blockAccess,
            final BlockPos pos, final EnumFacing side) {
        return 0;
    }

    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }
}
