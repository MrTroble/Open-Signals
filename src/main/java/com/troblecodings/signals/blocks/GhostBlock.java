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
import net.minecraft.util.EnumHand;
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

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source,
            final BlockPos pos) {
        final BlockPos downPos = pos.down();
        final IBlockState lowerState = source.getBlockState(downPos);
        final Block lowerBlock = lowerState.getBlock();
        if (isRightBlock(lowerBlock))
            return lowerBlock.getBoundingBox(lowerState, source, downPos).offset(0, -1, 0);
        return FULL_BLOCK_AABB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState,
            final IBlockAccess worldIn, final BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target,
            final World world, final BlockPos pos, final EntityPlayer player) {
        final BlockPos downPos = pos.down();
        final IBlockState lowerState = world.getBlockState(downPos);
        final Block lowerBlock = lowerState.getBlock();
        if (isRightBlock(lowerBlock))
            return lowerBlock.getPickBlock(lowerState, target, world, downPos, player);
        return super.getPickBlock(state, target, world, pos, player);
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
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final BlockPos lowerPos = pos.down();
        final IBlockState lowerState = worldIn.getBlockState(lowerPos);
        final Block lowerBlock = lowerState.getBlock();
        if (isRightBlock(lowerBlock))
            return lowerBlock.onBlockActivated(worldIn, lowerPos, lowerState, playerIn, hand,
                    facing, hitX, hitY, hitZ);
        return false;
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        DestroyHelper.checkAndDestroyBlockInDirection(worldIn, pos, state, new EnumFacing[] {
                EnumFacing.UP, EnumFacing.DOWN
        }, block -> block instanceof GhostBlock || block instanceof Signal);
    }

    private static boolean isRightBlock(final Block block) {
        return block instanceof Signal || block instanceof GhostBlock;
    }
}