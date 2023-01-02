package com.troblecodings.signals.core;

import com.troblecodings.signals.blocks.BasicBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

public interface TileEntitySupplierWrapper extends BlockEntitySupplier<BlockEntity> {
    @Override
    default BlockEntity create(final BlockPos pos, final BlockState state) {
        return supply(new TileEntityInfo(pos, state)
                .with(((BasicBlock) state.getBlock()).getBlockEntityType().get()));
    }

    BlockEntity supply(final TileEntityInfo info);
}