package com.troblecodings.signals.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.troblecodings.signals.core.TileEntitySupplierWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;

public class BasicBlock extends Block {

    private static final Map<TileEntitySupplierWrapper, String> BLOCK_NAMES = new HashMap<>();
    private static final Map<TileEntitySupplierWrapper, Set<BasicBlock>> BLOCK_SUPPLIER = new HashMap<>();
    public static final Map<TileEntitySupplierWrapper, TileEntityType<?>> BLOCK_ENTITYS = new HashMap<>();

    public BasicBlock(final Properties properties) {
        super(properties);
        final Optional<TileEntitySupplierWrapper> optional = getSupplierWrapper();
        getSupplierWrapperName().ifPresent(name -> {
            optional.ifPresent(supplier -> {
                BLOCK_SUPPLIER.computeIfAbsent(supplier, _u -> new HashSet<>()).add(this);
                BLOCK_NAMES.computeIfAbsent(supplier, _u -> name);
            });
        });
    }

    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.empty();
    }

    public Optional<String> getSupplierWrapperName() {
        return Optional.empty();
    }

    public Optional<TileEntityType<?>> getBlockEntityType() {
        return getSupplierWrapper().map(BLOCK_ENTITYS::get);
    }

    public static void prepare() {
        BLOCK_SUPPLIER.forEach((wrapper, blocks) -> {
            final String name = BLOCK_NAMES.get(wrapper);
            final Supplier<TileEntity> supplier = () -> wrapper.supply(null);
            final TileEntityType<TileEntity> type = TileEntityType.Builder
                    .of(supplier, (Block[]) blocks.toArray()).build(null);
            type.setRegistryName(name);
            BLOCK_ENTITYS.put(wrapper, type);
        });
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return BLOCK_ENTITYS.get(getSupplierWrapper().get()).create();
    }
}