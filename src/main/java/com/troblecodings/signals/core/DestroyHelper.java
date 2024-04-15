package com.troblecodings.signals.core;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DestroyHelper {

    private DestroyHelper() {
    }

    public static void checkAndDestroyOtherBlocks(final LevelAccessor worldIn, final BlockPos pos,
            final BlockState state, final Predicate<Block> predicate) {
        checkAndDestroyBlockInDirection(worldIn, pos, state, Direction.values(), predicate);
    }

    public static void checkAndDestroyBlockInDirection(final LevelAccessor worldIn,
            final BlockPos basePos, final BlockState baseState, final Direction[] directions,
            final Predicate<Block> predicate) {
        for (final Direction direction : directions) {
            final BlockPos thisPos = basePos.relative(direction);
            final Block otherBlock = worldIn.getBlockState(thisPos).getBlock();
            if (predicate.test(otherBlock)) {
                worldIn.destroyBlock(thisPos, false);
                otherBlock.destroy(worldIn, thisPos, baseState);
            }
        }
    }

}
