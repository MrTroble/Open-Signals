package com.troblecodings.signals.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

public class Monitor extends BasicBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public Monitor() {
        super(Properties.of(Material.STONE));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(LEFT, Boolean.valueOf(false)).setValue(RIGHT, Boolean.valueOf(false))
                .setValue(UP, Boolean.valueOf(false)).setValue(DOWN, Boolean.valueOf(false)));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        Direction direction = ctx.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState();
        switch (direction) {
            case EAST:
                state = state.setValue(FACING, Direction.EAST);
                break;
            case SOUTH:
                state = state.setValue(FACING, Direction.SOUTH);
                break;
            case WEST:
                state = state.setValue(FACING, Direction.WEST);
                break;
            case NORTH:
            default:
                state = state.setValue(FACING, Direction.NORTH);
                break;
        }
        return state;
    }

    @Override
    public BlockState updateShape(final BlockState state, final Direction direction,
            final BlockState otherState, final LevelAccessor world, final BlockPos pos,
            final BlockPos otherPos) {
        BlockState newState = state;
        Direction dir = newState.getValue(FACING);
        switch (dir) {
            case EAST:
                newState = newState.setValue(LEFT, connectsTo(world, pos.south(), newState))
                        .setValue(RIGHT, connectsTo(world, pos.north(), newState))
                        .setValue(UP, connectsTo(world, pos.above(), newState))
                        .setValue(DOWN, connectsTo(world, pos.below(), newState));
                break;
            case SOUTH:
                newState = newState.setValue(LEFT, connectsTo(world, pos.west(), newState))
                        .setValue(RIGHT, connectsTo(world, pos.east(), newState))
                        .setValue(UP, connectsTo(world, pos.above(), newState))
                        .setValue(DOWN, connectsTo(world, pos.below(), newState));
                break;
            case WEST:
                newState = newState.setValue(LEFT, connectsTo(world, pos.north(), newState))
                        .setValue(RIGHT, connectsTo(world, pos.south(), newState))
                        .setValue(UP, connectsTo(world, pos.above(), newState))
                        .setValue(DOWN, connectsTo(world, pos.below(), newState));
                break;
            case NORTH:
            default:
                newState = newState.setValue(LEFT, connectsTo(world, pos.east(), newState))
                        .setValue(RIGHT, connectsTo(world, pos.west(), newState))
                        .setValue(UP, connectsTo(world, pos.above(), newState))
                        .setValue(DOWN, connectsTo(world, pos.below(), newState));
                break;
        }
        return newState;
    }

    private boolean connectsTo(final LevelAccessor level, final BlockPos pos,
            final BlockState thisState) {
        BlockState otherState = level.getBlockState(pos);
        return otherState.getBlock() instanceof Monitor
                && thisState.getValue(FACING).equals(otherState.getValue(FACING));
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirrow) {
        return state.rotate(mirrow.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(
            final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEFT, RIGHT, UP, DOWN);
    }

}
