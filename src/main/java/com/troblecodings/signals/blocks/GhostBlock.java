package com.troblecodings.signals.blocks;

import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GhostBlock extends BasicBlock {

    public GhostBlock() {
        super(Properties.of(Material.GLASS).noOcclusion().lightLevel(u -> 1));
        registerDefaultState(defaultBlockState());
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

    public static void destroyUpperBlock(final IWorld worldIn, final BlockPos pos) {
        final BlockPos posUp = pos.above();
        final BlockState state = worldIn.getBlockState(posUp);
        final Block blockUp = state.getBlock();
        if (blockUp instanceof GhostBlock) {
            worldIn.destroyBlock(posUp, false);
            blockUp.destroy(worldIn, posUp, state);
        }
    }

    @Override
    public void destroy(final IWorld worldIn, final BlockPos pos, final BlockState state) {
        super.destroy(worldIn, pos, state);
        destroyUpperBlock(worldIn, pos);

        final BlockPos posdown = pos.below();
        final Block lowerBlock = worldIn.getBlockState(posdown).getBlock();
        if (lowerBlock instanceof GhostBlock || lowerBlock instanceof Signal) {
            worldIn.destroyBlock(posdown, false);
            lowerBlock.destroy(worldIn, posdown, state);
        }
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
}