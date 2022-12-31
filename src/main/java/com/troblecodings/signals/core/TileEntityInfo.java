package com.troblecodings.signals.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityInfo {
	public BlockEntityType<?> type;
	public BlockPos pos;
	public BlockState state;

	public TileEntityInfo(BlockPos pos, BlockState state) {
		super();
		this.pos = pos;
		this.state = state;
	}

	public TileEntityInfo with(BlockEntityType<?> type) {
		this.type = type;
		return this;
	}
}
