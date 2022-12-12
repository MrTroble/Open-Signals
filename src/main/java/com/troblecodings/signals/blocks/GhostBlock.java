package com.troblecodings.signals.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class GhostBlock extends Block {

    public GhostBlock() {
        super(Properties.of(Material.GLASS));
    }
    

    public static void destroyUpperBlock(final LevelAccessor worldIn, final BlockPos pos) {
        final BlockPos posup = pos.above();
        final Block upperBlock = worldIn.getBlockState(posup).getBlock();
        if (upperBlock instanceof GhostBlock) {
            worldIn.destroyBlock(posup, false);
        }
    }

    @Override
    public void destroy(LevelAccessor worldIn, BlockPos pos, BlockState stat) {
    	super.destroy(worldIn, pos, stat);
    	
        if (worldIn.isClientSide())
            return;
        destroyUpperBlock(worldIn, pos);

        final BlockPos posdown = pos.below();
        final Block lowerBlock = worldIn.getBlockState(posdown).getBlock();
        if (lowerBlock instanceof GhostBlock || lowerBlock instanceof Signal) {
            worldIn.destroyBlock(posdown, false);
        }
    }

}
