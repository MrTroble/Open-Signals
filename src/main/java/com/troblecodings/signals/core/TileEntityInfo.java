package com.troblecodings.signals.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityInfo {

    public BlockEntityType<?> type;
    public final BlockPos pos;
    public final BlockState state;

    public TileEntityInfo(final BlockPos pos, final BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public TileEntityInfo with(final BlockEntityType<?> type) {
        this.type = type;
        return this;
    }
}