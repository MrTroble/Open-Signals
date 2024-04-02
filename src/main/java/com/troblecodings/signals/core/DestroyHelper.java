package com.troblecodings.signals.core;

import com.troblecodings.signals.blocks.BasicBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public final class DestroyHelper {

    private DestroyHelper() {
    }

    public static void checkAndDestroyOtherBlocks(final IWorld worldIn, final BlockPos pos,
            final BlockState state) {
        for (final Direction direction : Direction.values()) {
            checkAndDestroyBlockInDirection(worldIn, pos, state, direction);
        }
    }

    private static void checkAndDestroyBlockInDirection(final IWorld acess, final BlockPos basePos,
            final BlockState baseState, final Direction direction) {
        final BlockPos thisPos = basePos.relative(direction);
        final Block otherBlock = acess.getBlockState(thisPos).getBlock();
        if (otherBlock instanceof BasicBlock
                && ((BasicBlock) otherBlock).shouldBeDestroyedWithOtherBlocks()) {
            acess.destroyBlock(thisPos, false);
            otherBlock.destroy(acess, thisPos, baseState);
        }
    }

}