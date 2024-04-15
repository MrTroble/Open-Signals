package com.troblecodings.signals.core;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DestroyHelper {

    private DestroyHelper() {
    }

    public static void checkAndDestroyOtherBlocks(final World worldIn, final BlockPos pos,
            final IBlockState state, final Predicate<Block> predicate) {
        checkAndDestroyBlockInDirection(worldIn, pos, state, EnumFacing.values(), predicate);
    }

    public static void checkAndDestroyBlockInDirection(final World worldIn, final BlockPos basePos,
            final IBlockState baseState, final EnumFacing[] directions,
            final Predicate<Block> predicate) {
        for (final EnumFacing direction : directions) {
            final BlockPos thisPos = basePos.offset(direction);
            final Block otherBlock = worldIn.getBlockState(thisPos).getBlock();
            if (predicate.test(otherBlock)) {
                worldIn.destroyBlock(thisPos, false);
                otherBlock.breakBlock(worldIn, thisPos, baseState);
            }
        }
    }
}