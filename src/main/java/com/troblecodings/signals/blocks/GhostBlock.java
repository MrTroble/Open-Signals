package com.troblecodings.signals.blocks;

import com.troblecodings.signals.OpenSignalsConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GhostBlock extends Block implements IConfigUpdatable {

    public GhostBlock() {
        super(Material.GLASS);
    }

    @Override
    public boolean isTranslucent(final IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getAmbientOcclusionLightValue(final IBlockState state) {
        return 1.0F;
    }

    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos,
            final IBlockState state, final float chance, final int fortune) {
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target,
            final World world, final BlockPos pos, final EntityPlayer player) {
        final BlockPos downPos = pos.down();
        final Block lowerBlock = world.getBlockState(downPos).getBlock();
        return lowerBlock.getPickBlock(state, target, world, downPos, player);
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(final IBlockState blockState,
            final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(final IBlockState state, final BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    public static void destroyUpperBlock(final World worldIn, final BlockPos pos) {
        final BlockPos posup = pos.up();
        final Block upperBlock = worldIn.getBlockState(posup).getBlock();
        if (upperBlock instanceof GhostBlock) {
            worldIn.destroyBlock(posup, false);
        }
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (worldIn.isRemote)
            return;
        destroyUpperBlock(worldIn, pos);

        final BlockPos posdown = pos.down();
        final Block lowerBlock = worldIn.getBlockState(posdown).getBlock();
        if (lowerBlock instanceof GhostBlock || lowerBlock instanceof Signal) {
            worldIn.destroyBlock(posdown, false);
        }
    }

    @Override
    public void updateConfigValues() {
        setLightLevel(OpenSignalsConfig.signalLightValue / 15.0f);
    }
}
