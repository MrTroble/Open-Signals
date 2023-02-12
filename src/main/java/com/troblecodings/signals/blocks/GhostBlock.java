package com.troblecodings.signals.blocks;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GhostBlock extends BasicBlock {

    public GhostBlock() {
        super(Properties.of(Material.GLASS).noOcclusion().lightLevel(u -> 1));
        registerDefaultState(defaultBlockState());
    }

    @Override
    public RenderShape getRenderShape(final BlockState p_60550_) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(final BlockState p_60555_, final BlockGetter p_60556_,
            final BlockPos p_60557_, final CollisionContext p_60558_) {
        return Shapes.block();
    }

    public static void destroyUpperBlock(final LevelAccessor worldIn, final BlockPos pos) {
        final BlockPos posUp = pos.above();
        final BlockState state = worldIn.getBlockState(posUp);
        final Block blockUp = state.getBlock();
        if (blockUp instanceof GhostBlock) {
            worldIn.destroyBlock(posUp, false);
            blockUp.destroy(worldIn, posUp, state);
        }
    }

    @Override
    public void destroy(final LevelAccessor worldIn, final BlockPos pos, final BlockState state) {
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
    public StateDefinition<Block, BlockState> getStateDefinition() {
        if (!Minecraft.getInstance().isLocalServer()) {
            CustomModelLoader.INSTANCE.prepare();
        }
        return super.getStateDefinition();
    }

}
