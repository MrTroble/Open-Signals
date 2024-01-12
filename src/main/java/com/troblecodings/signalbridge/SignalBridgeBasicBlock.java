package com.troblecodings.signalbridge;

import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.enums.SignalBridgeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SignalBridgeBasicBlock extends BasicBlock {

    private final SignalBridgeType type;

    public SignalBridgeBasicBlock(final SignalBridgeType type) {
        super(Properties.of(Material.STONE).noOcclusion()
                .lightLevel(u -> ConfigHandler.GENERAL.lightEmission.get())
                .isRedstoneConductor((_u1, _u2, _u3) -> false));
        registerDefaultState(defaultBlockState());
        this.type = type;
    }

    public SignalBridgeType getType() {
        return type;
    }

    @Override
    public boolean shouldHaveItem() {
        // true for debugging
        return true;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter getter, final BlockPos pos,
            final CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void destroy(final LevelAccessor acess, final BlockPos pos, final BlockState state) {
        for (final Direction direction : Direction.values()) {
            checkAndDestroyBlockInDirection(acess, pos, state, direction);
        }
    }

    private void checkAndDestroyBlockInDirection(final LevelAccessor acess, final BlockPos basePos,
            final BlockState baseState, final Direction direction) {
        final BlockPos thisPos = basePos.relative(direction);
        final Block otherBlock = acess.getBlockState(thisPos).getBlock();
        if (otherBlock instanceof SignalBridgeBasicBlock) {
            acess.destroyBlock(thisPos, false);
            otherBlock.destroy(acess, thisPos, baseState);
        }
    }

}
