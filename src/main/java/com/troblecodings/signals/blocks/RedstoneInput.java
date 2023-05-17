package com.troblecodings.signals.blocks;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class RedstoneInput extends RedstoneIO {

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        if (worldIn.isClientSide)
            return;
        if (worldIn.hasNeighborSignal(pos)) {
            if (!state.getValue(RedstoneIO.POWER)) {
                worldIn.setBlockAndUpdate(pos, state.setValue(RedstoneIO.POWER, true));
                final TileEntity entity = worldIn.getBlockEntity(pos);
                if (entity instanceof RedstoneIOTileEntity)
                    ((RedstoneIOTileEntity) entity).sendToAll();
            }
        } else {
            worldIn.setBlockAndUpdate(pos, state.setValue(RedstoneIO.POWER, false));
        }
    }

    @Override
    public int getDirectSignal(final BlockState blockState, final IBlockReader world,
            final BlockPos pos, final Direction direction) {
        return 0;
    }

    @Override
    public boolean isSignalSource(final BlockState blockState) {
        return true;
    }
}