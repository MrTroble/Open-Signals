package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.NBTWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBlockEntity extends BlockEntity {

	public BasicBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void saveWrapper(NBTWrapper wrapper) {}

	public void loadWrapper(NBTWrapper wrapper) {}

	@Override
	protected final void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		saveWrapper(new NBTWrapper(tag));
	}	
	
	@Override
	public final void load(CompoundTag tag) {
		this.loadWrapper(new NBTWrapper(tag));
		super.load(tag);
	}
}
