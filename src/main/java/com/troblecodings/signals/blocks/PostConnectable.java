package com.troblecodings.signals.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class PostConnectable extends Post {

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    private static final double PIXEL = 0.0625;
    private static final AxisAlignedBB CENTER_BB =
            new AxisAlignedBB(7 * PIXEL, 7 * PIXEL, 7 * PIXEL, 9 * PIXEL, 9 * PIXEL, 9 * PIXEL);
    private static final AxisAlignedBB NORTH_BB =
            new AxisAlignedBB(7 * PIXEL, 7 * PIXEL, 0, 9 * PIXEL, 9 * PIXEL, 7 * PIXEL);
    private static final AxisAlignedBB EAST_BB =
            new AxisAlignedBB(9 * PIXEL, 7 * PIXEL, 7 * PIXEL, 16 * PIXEL, 9 * PIXEL, 9 * PIXEL);
    private static final AxisAlignedBB SOUTH_BB =
            new AxisAlignedBB(7 * PIXEL, 7 * PIXEL, 9 * PIXEL, 9 * PIXEL, 9 * PIXEL, 16 * PIXEL);
    private static final AxisAlignedBB WEST_BB =
            new AxisAlignedBB(0, 7 * PIXEL, 7 * PIXEL, 7 * PIXEL, 9 * PIXEL, 9 * PIXEL);
    private static final AxisAlignedBB UP_BB =
            new AxisAlignedBB(7 * PIXEL, 9 * PIXEL, 7 * PIXEL, 9 * PIXEL, 16 * PIXEL, 9 * PIXEL);
    private static final AxisAlignedBB DOWN_BB =
            new AxisAlignedBB(7 * PIXEL, 0, 7 * PIXEL, 9 * PIXEL, 7 * PIXEL, 9 * PIXEL);

    public PostConnectable() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(NORTH, Boolean.valueOf(false))
                .withProperty(EAST, Boolean.valueOf(false))
                .withProperty(SOUTH, Boolean.valueOf(false))
                .withProperty(WEST, Boolean.valueOf(false)).withProperty(UP, Boolean.valueOf(false))
                .withProperty(DOWN, Boolean.valueOf(false)));
    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState finalstate, final IBlockAccess source,
            final BlockPos pos) {
        IBlockState state = finalstate;
        state = this.getActualState(state, source, pos);
        AxisAlignedBB shape = CENTER_BB;
        if (state.getValue(NORTH)) {
            shape = shape.union(NORTH_BB);
        }
        if (state.getValue(EAST)) {
            shape = shape.union(EAST_BB);
        }
        if (state.getValue(SOUTH)) {
            shape = shape.union(SOUTH_BB);
        }
        if (state.getValue(WEST)) {
            shape = shape.union(WEST_BB);
        }
        if (state.getValue(UP)) {
            shape = shape.union(UP_BB);
        }
        if (state.getValue(DOWN)) {
            shape = shape.union(DOWN_BB);
        }
        return shape;
    }

    private boolean connectsTo(final IBlockState state, final boolean sturdy,
            final EnumFacing direction) {
        Block block = state.getBlock();
        boolean isSignalBlock = block instanceof Signal || block instanceof GhostBlock;
        boolean isPostBlock = block instanceof PostConnectable;
        return sturdy || isSignalBlock || isPostBlock;
    }

    @SuppressWarnings("deprecation")
    private boolean isFaceSturdy(final IBlockAccess world, final BlockPos pos,
            final EnumFacing facing) {
        return isBlockNormalCube(world.getBlockState(pos));

    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world,
            final BlockPos pos) {
        BlockPos blockposNorth = pos.north();
        BlockPos blockposEast = pos.east();
        BlockPos blockposSouth = pos.south();
        BlockPos blockposWest = pos.west();
        BlockPos blockposUp = pos.up();
        BlockPos blockposDown = pos.down();
        IBlockState blockstateNorth = world.getBlockState(blockposNorth);
        IBlockState blockstateEast = world.getBlockState(blockposEast);
        IBlockState blockstateSouth = world.getBlockState(blockposSouth);
        IBlockState blockstateWest = world.getBlockState(blockposWest);
        IBlockState blockstateUp = world.getBlockState(blockposUp);
        IBlockState blockstateDown = world.getBlockState(blockposDown);
        return super.getActualState(state, world, pos)
                .withProperty(NORTH, Boolean.valueOf(this.connectsTo(blockstateNorth,
                        isFaceSturdy(world, blockposNorth, EnumFacing.SOUTH), EnumFacing.SOUTH)))
                .withProperty(EAST, Boolean.valueOf(this.connectsTo(blockstateEast,
                        isFaceSturdy(world, blockposEast, EnumFacing.WEST), EnumFacing.WEST)))
                .withProperty(SOUTH, Boolean.valueOf(this.connectsTo(blockstateSouth,
                        isFaceSturdy(world, blockposSouth, EnumFacing.NORTH), EnumFacing.NORTH)))
                .withProperty(WEST, Boolean.valueOf(this.connectsTo(blockstateWest,
                        isFaceSturdy(world, blockposWest, EnumFacing.EAST), EnumFacing.EAST)))
                .withProperty(UP,
                        Boolean.valueOf(this.connectsTo(blockstateUp,
                                isFaceSturdy(world, blockposUp, EnumFacing.DOWN), EnumFacing.DOWN)))
                .withProperty(DOWN, Boolean.valueOf(this.connectsTo(blockstateDown,
                        isFaceSturdy(world, blockposDown, EnumFacing.UP), EnumFacing.UP)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                NORTH, EAST, SOUTH, WEST, UP, DOWN
        });
    }

}
