package com.troblecodings.signals.blocks;

import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.DestroyHelper;
import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GhostBlock extends BasicBlock {

    public GhostBlock() {
        super(Properties.of(Material.GLASS).noOcclusion()
                .lightLevel(u -> ConfigHandler.GENERAL.lightEmission.get()));
        registerDefaultState(defaultBlockState());
    }

    @Override
    public boolean shouldHaveItem() {
        return false;
    }

    @Override
    public BlockRenderType getRenderShape(final BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader getter,
            final BlockPos pos, final ISelectionContext context) {
        return VoxelShapes.block();
    }

    @Override
    public void destroy(final IWorld worldIn, final BlockPos pos, final BlockState state) {
        super.destroy(worldIn, pos, state);
        DestroyHelper.checkAndDestroyBlockInDirection(worldIn, pos, state, new Direction[] {
                Direction.UP, Direction.DOWN
        }, block -> block instanceof GhostBlock || block instanceof Signal);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public StateContainer<Block, BlockState> getStateDefinition() {
        if (!Minecraft.getInstance().isLocalServer()) {
            CustomModelLoader.INSTANCE.prepare();
        }
        return super.getStateDefinition();
    }

    @Override
    public ItemStack getCloneItemStack(final IBlockReader reader, final BlockPos pos,
            final BlockState state) {
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult result) {
        final BlockPos lowerPos = pos.below();
        final BlockState lowerState = world.getBlockState(lowerPos);
        return lowerState.getBlock().use(lowerState, world, lowerPos, player, hand,
                result.withPosition(lowerPos));
    }
}