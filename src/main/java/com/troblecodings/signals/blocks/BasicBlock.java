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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BasicBlock extends Block implements ITileEntityProvider {

    private static final Map<TileEntitySupplierWrapper, String> BLOCK_NAMES = new HashMap<>();
    private static final Map<TileEntitySupplierWrapper, List<BasicBlock>> BLOCK_SUPPLIER =
            new HashMap<>();
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
    public boolean canRenderInLayer(final IBlockState state, final BlockRenderLayer layer) {
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

    public boolean shouldHaveItem() {
        return true;
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

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return createNewTileEntity(null, 0) != null;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return null;
    }

    @Override
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos,
            final IBlockState state, final int fortune) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(this.getBlockState().getBlock()));
        return drops;
    }

    @Override
    public boolean canHarvestBlock(final IBlockAccess world, final BlockPos pos,
            final EntityPlayer player) {
        return true;
    }
}