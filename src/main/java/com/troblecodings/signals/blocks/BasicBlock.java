package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;

public class BasicBlock extends Block {

    private static final Map<TileEntitySupplierWrapper, String> BLOCK_NAMES = new HashMap<>();
    private static final Map<TileEntitySupplierWrapper, List<BasicBlock>> BLOCK_SUPPLIER =
            new HashMap<>();
    public static final Map<TileEntitySupplierWrapper, TileEntityType<?>> BLOCK_ENTITYS =
            new HashMap<>();

    public BasicBlock(final Properties properties) {
        super(properties);
        final Optional<TileEntitySupplierWrapper> optional = getSupplierWrapper();
        getSupplierWrapperName().ifPresent(name -> {
            optional.ifPresent(supplier -> {
                BLOCK_SUPPLIER.computeIfAbsent(supplier, _u -> new ArrayList<>()).add(this);
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

    public boolean shouldHaveItem() {
        return true;
    }

    public static void prepare() {
        BLOCK_SUPPLIER.forEach((wrapper, blocks) -> {
            final String name = BLOCK_NAMES.get(wrapper);
            final Block[] allBlocks = new Block[blocks.size()];
            final AtomicReference<TileEntityType<TileEntity>> type = new AtomicReference<>();
            for (int i = 0; i < blocks.size(); i++) {
                allBlocks[i] = blocks.get(i);
            }
            final Supplier<TileEntity> supplier =
                    () -> wrapper.supply(new TileEntityInfo().with(type.get()));
            type.set(TileEntityType.Builder.of(supplier, allBlocks).build(null));
            type.get().setRegistryName(name);
            BLOCK_ENTITYS.put(wrapper, type.get());
        });
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return getSupplierWrapper().isPresent() && getBlockEntityType().isPresent();
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return BLOCK_ENTITYS.get(getSupplierWrapper().get()).create();
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final Builder builder) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this.getBlock().asItem()));
        return drops;
    }
}