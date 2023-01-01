package com.troblecodings.signals.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.core.TileEntitySupplierWrapper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BasicBlock extends Block {

	private static Map<TileEntitySupplierWrapper, Set<BasicBlock>> BLOCK_SUPPLIER = new HashMap<>();
	public static Map<TileEntitySupplierWrapper, BlockEntityType<?>> BLOCK_ENTITYS = new HashMap<>();

	public BasicBlock(final Properties properties) {
		super(properties);
		Optional<TileEntitySupplierWrapper> optional = getSupplierWrapper();
		optional.ifPresent(supplier -> BLOCK_SUPPLIER.computeIfAbsent(supplier, _u -> new HashSet<>()).add(this));
	}

	public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
		return Optional.empty();
	}

	public Optional<BlockEntityType<?>> getBlockEntityType() {
		return getSupplierWrapper().map(BLOCK_ENTITYS::get);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void prepare() {
		BLOCK_SUPPLIER.forEach((wrapper, blocks) -> {
			final BlockEntityType type = new BlockEntityType(wrapper, blocks, null);
			type.setRegistryName(blocks.iterator().next().getRegistryName());
			BLOCK_ENTITYS.put(wrapper, type);
		});
	}

}
