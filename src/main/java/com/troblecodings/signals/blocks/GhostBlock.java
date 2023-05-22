package com.troblecodings.signals.blocks;

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
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world,
            BlockPos pos, EntityPlayer player) {
        final BlockPos downPos = pos.down();
        final Block lowerBlock = world.getBlockState(downPos).getBlock();
        return lowerBlock.getPickBlock(state, target, world, downPos, player);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess,
            BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
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
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        destroyUpperBlock(worldIn, pos);

        final BlockPos posdown = pos.down();
        final Block lowerBlock = worldIn.getBlockState(posdown).getBlock();
        if (lowerBlock instanceof GhostBlock || lowerBlock instanceof Signal) {
            worldIn.destroyBlock(posdown, false);
            lowerBlock.breakBlock(worldIn, posdown, state);
        }
    }
}