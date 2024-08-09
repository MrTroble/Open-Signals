package com.troblecodings.signals.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class PostConnectable extends Post {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final double PIXEL = 0.0625;
    private static final VoxelShape CENTER_BB = VoxelShapes.create(
            new AxisAlignedBB(7 * PIXEL, 7 * PIXEL, 7 * PIXEL, 9 * PIXEL, 9 * PIXEL, 9 * PIXEL));
    private static final VoxelShape NORTH_BB = VoxelShapes
            .create(new AxisAlignedBB(7 * PIXEL, 7 * PIXEL, 0, 9 * PIXEL, 9 * PIXEL, 7 * PIXEL));
    private static final VoxelShape EAST_BB = VoxelShapes.create(
            new AxisAlignedBB(9 * PIXEL, 7 * PIXEL, 7 * PIXEL, 16 * PIXEL, 9 * PIXEL, 9 * PIXEL));
    private static final VoxelShape SOUTH_BB = VoxelShapes.create(
            new AxisAlignedBB(7 * PIXEL, 7 * PIXEL, 9 * PIXEL, 9 * PIXEL, 9 * PIXEL, 16 * PIXEL));
    private static final VoxelShape WEST_BB = VoxelShapes
            .create(new AxisAlignedBB(0, 7 * PIXEL, 7 * PIXEL, 7 * PIXEL, 9 * PIXEL, 9 * PIXEL));
    private static final VoxelShape UP_BB = VoxelShapes.create(
            new AxisAlignedBB(7 * PIXEL, 9 * PIXEL, 7 * PIXEL, 9 * PIXEL, 16 * PIXEL, 9 * PIXEL));
    private static final VoxelShape DOWN_BB = VoxelShapes
            .create(new AxisAlignedBB(7 * PIXEL, 0, 7 * PIXEL, 9 * PIXEL, 7 * PIXEL, 9 * PIXEL));

    public PostConnectable() {
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false))
                .setValue(DOWN, Boolean.valueOf(false)));
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader getter,
            final BlockPos pos, final ISelectionContext context) {
        VoxelShape shape = CENTER_BB;
        if (state.getValue(NORTH)) {
            shape = VoxelShapes.or(shape, NORTH_BB);
        }
        if (state.getValue(EAST)) {
            shape = VoxelShapes.or(shape, EAST_BB);
        }
        if (state.getValue(SOUTH)) {
            shape = VoxelShapes.or(shape, SOUTH_BB);
        }
        if (state.getValue(WEST)) {
            shape = VoxelShapes.or(shape, WEST_BB);
        }
        if (state.getValue(UP)) {
            shape = VoxelShapes.or(shape, UP_BB);
        }
        if (state.getValue(DOWN)) {
            shape = VoxelShapes.or(shape, DOWN_BB);
        }
        return shape;
    }

    private boolean connectsTo(final BlockState state, final boolean sturdy,
            final Direction direction) {
        Block block = state.getBlock();
        boolean isSignalBlock = block instanceof Signal || block instanceof GhostBlock;
        boolean isPostBlock = block instanceof PostConnectable;
        return sturdy || isSignalBlock || isPostBlock;
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        World blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockposNorth = blockpos.north();
        BlockPos blockposEast = blockpos.east();
        BlockPos blockposSouth = blockpos.south();
        BlockPos blockposWest = blockpos.west();
        BlockPos blockposUp = blockpos.above();
        BlockPos blockposDown = blockpos.below();
        BlockState blockstateNorth = blockgetter.getBlockState(blockposNorth);
        BlockState blockstateEast = blockgetter.getBlockState(blockposEast);
        BlockState blockstateSouth = blockgetter.getBlockState(blockposSouth);
        BlockState blockstateWest = blockgetter.getBlockState(blockposWest);
        BlockState blockstateUp = blockgetter.getBlockState(blockposUp);
        BlockState blockstateDown = blockgetter.getBlockState(blockposDown);
        return super.getStateForPlacement(context)
                .setValue(NORTH,
                        Boolean.valueOf(this.connectsTo(blockstateNorth,
                                blockstateNorth.isFaceSturdy(blockgetter, blockposNorth,
                                        Direction.SOUTH),
                                Direction.SOUTH)))
                .setValue(EAST,
                        Boolean.valueOf(this.connectsTo(blockstateEast,
                                blockstateEast.isFaceSturdy(blockgetter, blockposEast,
                                        Direction.WEST),
                                Direction.WEST)))
                .setValue(SOUTH,
                        Boolean.valueOf(this.connectsTo(blockstateSouth,
                                blockstateSouth.isFaceSturdy(blockgetter, blockposSouth,
                                        Direction.NORTH),
                                Direction.NORTH)))
                .setValue(WEST,
                        Boolean.valueOf(this.connectsTo(blockstateWest,
                                blockstateWest.isFaceSturdy(blockgetter, blockposWest,
                                        Direction.EAST),
                                Direction.EAST)))
                .setValue(UP,
                        Boolean.valueOf(this.connectsTo(blockstateUp,
                                blockstateUp.isFaceSturdy(blockgetter, blockposUp, Direction.DOWN),
                                Direction.DOWN)))
                .setValue(DOWN, Boolean.valueOf(this.connectsTo(blockstateDown,
                        blockstateDown.isFaceSturdy(blockgetter, blockposDown, Direction.UP),
                        Direction.UP)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction direction,
            final BlockState newState, final IWorld world, final BlockPos pos,
            final BlockPos posOther) {
        BlockPos blockposNorth = pos.north();
        BlockPos blockposEast = pos.east();
        BlockPos blockposSouth = pos.south();
        BlockPos blockposWest = pos.west();
        BlockPos blockposUp = pos.above();
        BlockPos blockposDown = pos.below();
        BlockState blockstateNorth = world.getBlockState(blockposNorth);
        BlockState blockstateEast = world.getBlockState(blockposEast);
        BlockState blockstateSouth = world.getBlockState(blockposSouth);
        BlockState blockstateWest = world.getBlockState(blockposWest);
        BlockState blockstateUp = world.getBlockState(blockposUp);
        BlockState blockstateDown = world.getBlockState(blockposDown);
        return super.updateShape(state, direction, newState, world, pos, posOther)
                .setValue(NORTH,
                        Boolean.valueOf(
                                this.connectsTo(blockstateNorth,
                                        blockstateNorth.isFaceSturdy(world, blockposNorth,
                                                Direction.SOUTH),
                                        Direction.SOUTH)))
                .setValue(EAST,
                        Boolean.valueOf(this.connectsTo(blockstateEast,
                                blockstateEast.isFaceSturdy(world, blockposEast, Direction.WEST),
                                Direction.WEST)))
                .setValue(SOUTH,
                        Boolean.valueOf(
                                this.connectsTo(blockstateSouth,
                                        blockstateSouth.isFaceSturdy(world, blockposSouth,
                                                Direction.NORTH),
                                        Direction.NORTH)))
                .setValue(WEST,
                        Boolean.valueOf(this.connectsTo(blockstateWest,
                                blockstateWest.isFaceSturdy(world, blockposWest, Direction.EAST),
                                Direction.EAST)))
                .setValue(UP,
                        Boolean.valueOf(
                                this.connectsTo(blockstateUp,
                                        blockstateUp.isFaceSturdy(world, blockposUp,
                                                Direction.DOWN),
                                        Direction.DOWN)))
                .setValue(DOWN,
                        Boolean.valueOf(this.connectsTo(blockstateDown,
                                blockstateDown.isFaceSturdy(world, blockposDown, Direction.UP),
                                Direction.UP)));
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }
}
