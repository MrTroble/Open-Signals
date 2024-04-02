package com.troblecodings.signals.core;

import com.troblecodings.signals.blocks.BasicBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DestroyHelper {

    private DestroyHelper() {
    }

    public static void checkAndDestroyOtherBlocks(final LevelAccessor worldIn, final BlockPos pos,
            final BlockState state) {
        for (final Direction direction : Direction.values()) {
            checkAndDestroyBlockInDirection(worldIn, pos, state, direction);
        }
    }

    private static void checkAndDestroyBlockInDirection(final LevelAccessor acess,
            final BlockPos basePos, final BlockState baseState, final Direction direction) {
        final BlockPos thisPos = basePos.relative(direction);
        final Block otherBlock = acess.getBlockState(thisPos).getBlock();
        if (otherBlock instanceof BasicBlock
                && ((BasicBlock) otherBlock).shouldBeDestroyedWithOtherBlocks()) {
            acess.destroyBlock(thisPos, false);
            otherBlock.destroy(acess, thisPos, baseState);
        }
    }

}
