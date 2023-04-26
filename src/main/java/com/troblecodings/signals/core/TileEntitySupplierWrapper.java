package com.troblecodings.signals.core;

import com.troblecodings.signals.blocks.BasicBlock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface TileEntitySupplierWrapper {

    default TileEntity create(final BlockPos pos, final IBlockReader state) {
        return supply(new TileEntityInfo(pos, state)
                .with(state.getB));
    }

    TileEntity supply(final TileEntityInfo info);
}