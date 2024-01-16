package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.enums.SignalBridgeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SignalBridgeBasicBlock extends BasicBlock {

    public static final List<SignalBridgeBasicBlock> ALL_SIGNALBRIDGE_BLOCKS = new ArrayList<>();

    private final SignalBridgeBlockProperties properties;
    // Just for Networking
    private final int id;

    public SignalBridgeBasicBlock(final SignalBridgeBlockProperties properties) {
        super(Properties.of(Material.STONE).noOcclusion()
                .lightLevel(u -> ConfigHandler.GENERAL.lightEmission.get())
                .isRedstoneConductor((_u1, _u2, _u3) -> false));
        registerDefaultState(defaultBlockState());
        this.properties = properties;
        this.id = ALL_SIGNALBRIDGE_BLOCKS.size();
        ALL_SIGNALBRIDGE_BLOCKS.add(this);
    }

    public int getID() {
        return id;
    }

    public SignalBridgeType getType() {
        return properties.getType();
    }

    @Override
    public boolean shouldHaveItem() {
        return false;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter getter, final BlockPos pos,
            final CollisionContext context) {
        return Shapes.create(Shapes.block().bounds().expandTowards(properties.extentionX,
                properties.extentionY, properties.extentionZ));
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target,
            final BlockGetter level, final BlockPos pos, final Player player) {
        return null;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter getter,
            final BlockPos pos, final CollisionContext context) {
        return getShape(state, getter, pos, context);
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
