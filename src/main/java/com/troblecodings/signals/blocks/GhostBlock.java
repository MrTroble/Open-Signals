package com.troblecodings.signals.blocks;

import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.DestroyHelper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class GhostBlock extends BasicBlock {

    public GhostBlock() {
        super(Material.GLASS);
        setLightLevel(ConfigHandler.lightEmission / 15.0F);
    }

    @Override
    public boolean isTranslucent(final IBlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightValue(final IBlockState state) {
        return 1.0F;
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target,
            final World world, final BlockPos pos, final EntityPlayer player) {
        final BlockPos downPos = pos.down();
        final Block lowerBlock = world.getBlockState(downPos).getBlock();
        return lowerBlock.getPickBlock(state, target, world, downPos, player);
    }

    @Override
    public boolean shouldSideBeRendered(final IBlockState blockState,
            final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
        return false;
    }

    @Override
    public boolean shouldHaveItem() {
        return false;
    }

    @Override
    public boolean shouldBeDestroyedWithOtherBlocks() {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source,
            final BlockPos pos) {
        return Block.FULL_BLOCK_AABB;
    }

    public static void destroyUpperBlock(final World worldIn, final BlockPos pos) {
        final BlockPos posUp = pos.up();
        final IBlockState state = worldIn.getBlockState(posUp);
        final Block blockUp = state.getBlock();
        if (blockUp instanceof GhostBlock) {
            worldIn.destroyBlock(posUp, false);
        }
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        DestroyHelper.checkAndDestroyOtherBlocks(worldIn, pos, state);
    }
}