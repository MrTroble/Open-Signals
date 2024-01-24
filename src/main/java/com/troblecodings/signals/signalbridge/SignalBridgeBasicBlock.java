package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.DestroyHelper;
import com.troblecodings.signals.enums.SignalBridgeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SignalBridgeBasicBlock extends BasicBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final List<SignalBridgeBasicBlock> ALL_SIGNALBRIDGE_BLOCKS = new ArrayList<>();

    private final SignalBridgeBlockProperties properties;
    // Just for Networking
    private final int id;

    public SignalBridgeBasicBlock(final SignalBridgeBlockProperties properties) {
        super(Properties.of(Material.STONE).noOcclusion()
                .lightLevel(u -> ConfigHandler.GENERAL.lightEmission.get())
                .isRedstoneConductor((_u1, _u2, _u3) -> false));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotaion) {
        return state.setValue(FACING, rotaion.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
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
        super.destroy(acess, pos, state);
        DestroyHelper.checkAndDestroyOtherBlocks(acess, pos, state);
    }
}
