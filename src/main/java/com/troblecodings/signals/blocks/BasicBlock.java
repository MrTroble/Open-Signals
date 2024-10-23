package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.core.TileEntitySupplierWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;

public class BasicBlock extends Block implements EntityBlock {

    private static final Map<TileEntitySupplierWrapper, String> BLOCK_NAMES = new HashMap<>();
    private static final Map<TileEntitySupplierWrapper, Set<BasicBlock>> BLOCK_SUPPLIER =
            new HashMap<>();
    public static final Map<TileEntitySupplierWrapper, BlockEntityType<?>> BLOCK_ENTITYS =
            new HashMap<>();

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

    public Optional<BlockEntityType<?>> getBlockEntityType() {
        return getSupplierWrapper().map(BLOCK_ENTITYS::get);
    }

    public boolean shouldHaveItem() {
        return true;
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public static void prepare() {
        BLOCK_SUPPLIER.forEach((wrapper, blocks) -> {
            final BlockEntityType type = new BlockEntityType(wrapper, blocks, null);
            type.setRegistryName(BLOCK_NAMES.get(wrapper));
            BLOCK_ENTITYS.put(wrapper, type);
        });
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return getSupplierWrapper().map(type -> type.create(pos, state)).orElse(null);
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this.asBlock().asItem()));
        return drops;
    }
}