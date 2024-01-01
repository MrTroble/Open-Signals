package com.troblecodings.signals.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.core.TileEntitySupplierWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBlock extends Block implements EntityBlock {

    private static final Map<TileEntitySupplierWrapper, String> BLOCK_NAMES = new HashMap<>();
    private static final Map<TileEntitySupplierWrapper, Set<BasicBlock>> BLOCK_SUPPLIER = new HashMap<>();
    public static final Map<TileEntitySupplierWrapper, BlockEntityType<?>> BLOCK_ENTITYS = new HashMap<>();
    public static final Map<BlockEntityType<?>, String> TYPE_TO_NAME = new HashMap<>();

    private String name = "";

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

    public void setBlockName(final String name) {
        this.name = name;
    }

    public String getBlockName() {
        return name;
    }

    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.empty();
    }

    public Optional<String> getSupplierWrapperName() {
        return Optional.empty();
    }

    public Optional<BlockEntityType<?>> getBlockEntityType() {
        return getSupplierWrapper().map(BLOCK_ENTITYS::get);
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public static void prepare() {
        BLOCK_SUPPLIER.forEach((wrapper, blocks) -> {
            final BlockEntityType type = new BlockEntityType(wrapper, blocks, null);
            BLOCK_ENTITYS.put(wrapper, type);
            TYPE_TO_NAME.put(type, BLOCK_NAMES.get(wrapper));
        });
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return getSupplierWrapper().map(type -> type.create(pos, state)).orElse(null);
    }
}