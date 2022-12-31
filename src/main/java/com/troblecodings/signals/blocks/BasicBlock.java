package com.troblecodings.signals.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.core.TileEntityInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBlock extends Block {

	public static interface TileEntitySupplierWrapper extends BlockEntitySupplier<BlockEntity> {
		@Override
		default BlockEntity create(BlockPos pos, BlockState state) {
			return supply(
					new TileEntityInfo(pos, state).with(((BasicBlock) state.getBlock()).getBlockEntityType().get()));
		}

		BlockEntity supply(TileEntityInfo info);
	}

	private static Map<TileEntitySupplierWrapper, Set<BasicBlock>> BLOCK_SUPPLIER = new HashMap<>();
	public static Map<TileEntitySupplierWrapper, BlockEntityType<BlockEntity>> BLOCK_ENTITYS = new HashMap<>();

	public BasicBlock(Properties properties) {
		super(properties);
		Optional<TileEntitySupplierWrapper> optional = getSupplierWrapper();
		optional.ifPresent(supplier -> BLOCK_SUPPLIER.computeIfAbsent(supplier, _u -> new HashSet<>()).add(this));
	}

	public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
		return Optional.empty();
	}

	public Optional<BlockEntityType<BlockEntity>> getBlockEntityType() {
		return getSupplierWrapper().map(BLOCK_ENTITYS::get);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void prepare() {
		BLOCK_SUPPLIER
				.forEach((wrapper, blocks) -> BLOCK_ENTITYS.put(wrapper, new BlockEntityType(wrapper, blocks, null)));
	}

}
