package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CombinedRedstoneInput extends RedstoneInput {

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos) {
        if (worldIn.isRemote)
            return;
        final boolean currentState = state.getValue(POWER);
        final boolean hasNeighborSignal = worldIn.isBlockPowered(pos);
        final RedstoneIOTileEntity tile = (RedstoneIOTileEntity) worldIn.getTileEntity(pos);
        if (currentState && !hasNeighborSignal) {
            worldIn.setBlockState(pos, state.withProperty(POWER, false));
            tile.sendToAll();
        } else if (hasNeighborSignal && !currentState) {
            worldIn.setBlockState(pos, state.withProperty(POWER, true));
            tile.sendToAll();
        } else if (!hasNeighborSignal) {
            worldIn.setBlockState(pos, state.withProperty(POWER, false));
        }
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("combiredstoneinput");
    }
}