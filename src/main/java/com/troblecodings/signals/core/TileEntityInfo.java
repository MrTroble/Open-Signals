package com.troblecodings.signals.core;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class TileEntityInfo {

    public TileEntityType<?> type;
    public final BlockPos pos;
    public final IBlockReader state;

    public TileEntityInfo(final BlockPos pos, final IBlockReader state) {
        this.pos = pos;
        this.state = state;
    }

    public TileEntityInfo with(final TileEntityType<?> type) {
        this.type = type;
        return this;
    }
}