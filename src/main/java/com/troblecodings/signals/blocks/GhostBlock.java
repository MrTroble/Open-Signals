package com.troblecodings.signals.blocks;

import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.DestroyHelper;
import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter getter, final BlockPos pos,
            final CollisionContext context) {
        final BlockPos lowerPos = pos.below();
        final BlockState lowerState = getter.getBlockState(lowerPos);
        return lowerState.getBlock().getShape(lowerState, getter, lowerPos, context).move(0, -1, 0);
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter getter,
            final BlockPos pos, final CollisionContext context) {
        return getShape(state, getter, pos, context);
    }

    @Override
    public void destroy(final LevelAccessor worldIn, final BlockPos pos, final BlockState state) {
        super.destroy(worldIn, pos, state);
        DestroyHelper.checkAndDestroyBlockInDirection(worldIn, pos, state, new Direction[] {
                Direction.UP, Direction.DOWN
        }, block -> block instanceof GhostBlock || block instanceof Signal);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public StateDefinition<Block, BlockState> getStateDefinition() {
        if (!Minecraft.getInstance().isLocalServer()) {
            CustomModelLoader.INSTANCE.prepare();
        }
        return super.getStateDefinition();
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target,
            final BlockGetter level, final BlockPos pos, final Player player) {
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult result) {
        final BlockPos lowerPos = pos.below();
        final BlockState lowerState = world.getBlockState(lowerPos);
        return lowerState.getBlock().use(lowerState, world, lowerPos, player, hand,
                result.withPosition(lowerPos));
    }
}