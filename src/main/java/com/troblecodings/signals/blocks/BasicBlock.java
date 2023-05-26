package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSTabs;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicBlock extends Block implements ITileEntityProvider {

    private static final Map<TileEntitySupplierWrapper, String> BLOCK_NAMES = new HashMap<>();
    private static final Map<TileEntitySupplierWrapper, List<BasicBlock>> BLOCK_SUPPLIER = new HashMap<>();
    public static final Map<TileEntitySupplierWrapper, TileEntity> BLOCK_ENTITYS = new HashMap<>();

    public BasicBlock(final Material material) {
        super(material);
        setCreativeTab(OSTabs.TAB);
        final Optional<TileEntitySupplierWrapper> optional = getSupplierWrapper();
        getSupplierWrapperName().ifPresent(name -> {
            optional.ifPresent(supplier -> {
                BLOCK_SUPPLIER.computeIfAbsent(supplier, _u -> new ArrayList<>()).add(this);
                BLOCK_NAMES.computeIfAbsent(supplier, _u -> name);
            });
        });
    }

    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos,
            final IBlockState state, final float chance, final int fortune) {
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer.equals(BlockRenderLayer.CUTOUT_MIPPED);
    }

    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.empty();
    }

    public Optional<String> getSupplierWrapperName() {
        return Optional.empty();
    }

    public Optional<TileEntity> getBlockEntityType() {
        return getSupplierWrapper().map(BLOCK_ENTITYS::get);
    }

    public static void prepare() {
        BLOCK_SUPPLIER.forEach((wrapper, blocks) -> BLOCK_ENTITYS.put(wrapper, wrapper.supply()));
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    public boolean hasTileEntity() {
        return getSupplierWrapper().isPresent() && getBlockEntityType().isPresent();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return BLOCK_ENTITYS.get(getSupplierWrapper().get());
    }
}