package com.troblecodings.signals.core;

import com.troblecodings.signals.blocks.BasicBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DestroyHelper {

    public static void checkAndDestroyOtherBlocks(final World worldIn, final BlockPos pos,
            final IBlockState state) {
        for (final EnumFacing direction : EnumFacing.values())
            checkAndDestroyBlockInDirection(worldIn, pos, state, direction);
    }

    private static void checkAndDestroyBlockInDirection(final World worldIn, final BlockPos basePos,
            final IBlockState baseState, final EnumFacing direction) {
        final BlockPos thisPos = basePos.offset(direction);
        final Block otherBlock = worldIn.getBlockState(thisPos).getBlock();
        if (otherBlock instanceof BasicBlock
                && ((BasicBlock) otherBlock).shouldBeDestroyedWithOtherBlocks()) {
            worldIn.destroyBlock(thisPos, false);
            otherBlock.breakBlock(worldIn, thisPos, baseState);
        }
    }

}