package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.core.DestroyHelper;
import com.troblecodings.signals.core.MonitorBlockProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Monitor extends BasicBlock {

    public static final List<Monitor> MONITORS = new ArrayList<>();

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool LEFT = PropertyBool.create("left");
    public static final PropertyBool RIGHT = PropertyBool.create("right");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    private final MonitorBlockProperties prop;
    private final MonitorTEBlock teMonitor;
    private int id = -1;

    public Monitor(final MonitorBlockProperties prop, final MonitorTEBlock teMonitor) {
        super(Material.ROCK);
        this.setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(LEFT, Boolean.valueOf(false))
                .withProperty(RIGHT, Boolean.valueOf(false))
                .withProperty(UP, Boolean.valueOf(false))
                .withProperty(DOWN, Boolean.valueOf(false)));
        this.prop = prop;
        this.teMonitor = teMonitor;
        if (this instanceof MonitorTEBlock)
            return;
        this.id = MONITORS.size();
        MONITORS.add(this);
    }

    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos,
            final EnumFacing face, final float hitX, final float hitY, final float hitZ,
            final int meta, final EntityLivingBase placer, final EnumHand hand) {
        IBlockState state = getDefaultState();
        switch (face) {
            case EAST:
                state = state.withProperty(FACING, EnumFacing.EAST);
                break;
            case SOUTH:
                state = state.withProperty(FACING, EnumFacing.SOUTH);
                break;
            case WEST:
                state = state.withProperty(FACING, EnumFacing.WEST);
                break;
            case NORTH:
            default:
                state = state.withProperty(FACING, EnumFacing.NORTH);
                break;
        }
        return state;
    }

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world,
            final BlockPos pos) {
        IBlockState newState = state;
        EnumFacing dir = newState.getValue(FACING);
        switch (dir) {
            case EAST:
                newState = newState.withProperty(LEFT, connectsTo(world, pos.south(), newState))
                        .withProperty(RIGHT, connectsTo(world, pos.north(), newState))
                        .withProperty(UP, connectsTo(world, pos.up(), newState))
                        .withProperty(DOWN, connectsTo(world, pos.down(), newState));
                break;
            case SOUTH:
                newState = newState.withProperty(LEFT, connectsTo(world, pos.west(), newState))
                        .withProperty(RIGHT, connectsTo(world, pos.east(), newState))
                        .withProperty(UP, connectsTo(world, pos.up(), newState))
                        .withProperty(DOWN, connectsTo(world, pos.down(), newState));
                break;
            case WEST:
                newState = newState.withProperty(LEFT, connectsTo(world, pos.north(), newState))
                        .withProperty(RIGHT, connectsTo(world, pos.south(), newState))
                        .withProperty(UP, connectsTo(world, pos.up(), newState))
                        .withProperty(DOWN, connectsTo(world, pos.down(), newState));
                break;
            case NORTH:
            default:
                newState = newState.withProperty(LEFT, connectsTo(world, pos.east(), newState))
                        .withProperty(RIGHT, connectsTo(world, pos.west(), newState))
                        .withProperty(UP, connectsTo(world, pos.up(), newState))
                        .withProperty(DOWN, connectsTo(world, pos.down(), newState));
                break;
        }
        return newState;
    }

    private boolean connectsTo(final IBlockAccess level, final BlockPos pos,
            final IBlockState thisState) {
        IBlockState otherState = level.getBlockState(pos);
        return otherState.getBlock() instanceof Monitor
                && thisState.getValue(FACING).equals(otherState.getValue(FACING));
    }

    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                FACING, LEFT, RIGHT, UP, DOWN
        });
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        DestroyHelper.checkAndDestroyOtherBlocks(worldIn, pos, state,
                block -> block instanceof Monitor);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source,
            final BlockPos pos) {
        final BlockPos downPos = pos.down();
        final IBlockState downState = source.getBlockState(downPos);
        final Block downBlock = downState.getBlock();
        if (downBlock instanceof Monitor)
            return downBlock.getBoundingBox(downState, source, downPos).offset(0, -1, 0);
        final BlockPos nextPos = getLeftPos(getActualState(state, source, pos), pos);
        final Vec3i relative = nextPos.subtract(pos);
        final IBlockState nextState = source.getBlockState(nextPos);
        final Block nextBlock = nextState.getBlock();
        if (!relative.equals(BlockPos.ORIGIN) && nextBlock instanceof Monitor)
            return nextBlock.getBoundingBox(nextState, source, nextPos).offset(relative.getX(),
                    relative.getY(), relative.getZ());
        return FULL_BLOCK_AABB;
    }

    private static BlockPos getLeftPos(final IBlockState state, final BlockPos pos) {
        if (!(state.getBlock() instanceof Monitor) || !state.getValue(LEFT))
            return pos;
        EnumFacing dir = state.getValue(FACING);
        switch (dir) {
            case EAST:
                return pos.south();
            case SOUTH:
                return pos.west();
            case WEST:
                return pos.north();
            case NORTH:
            default:
                return pos.east();
        }
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final EnumFacing blockFacing = state.getValue(FACING);
        final float u = horizontalHit(blockFacing, hitX, hitZ);
        return forwardClick(worldIn, pos, state, facing, playerIn, hand, u, hitY);
    }

    protected boolean forwardClick(final World world, final BlockPos pos, final IBlockState state,
            final EnumFacing facing, final EntityPlayer player, final EnumHand hand, final float u,
            final float v) {
        final BlockPos downPos = pos.down();
        final IBlockState downState = world.getBlockState(downPos);
        if (downState.getBlock() instanceof Monitor) {
            return ((Monitor) downState.getBlock()).forwardClick(world, downPos, downState, facing,
                    player, hand, u, v + 1);
        }
        final BlockPos nextPos = getLeftPos(getActualState(state, world, pos), pos);
        if (!nextPos.equals(pos)) {
            final IBlockState nextState = world.getBlockState(nextPos);
            if (nextState.getBlock() instanceof Monitor) {
                return ((Monitor) nextState.getBlock()).forwardClick(world, nextPos, nextState,
                        facing, player, hand, u + 1, v);
            }
        }
        return onScreenClick(world, pos, state, facing, player, hand, u, v);
    }

    protected boolean onScreenClick(final World world, final BlockPos pos, final IBlockState state,
            final EnumFacing facing, final EntityPlayer player, final EnumHand hand, final float u,
            final float v) {
        return false;
    }

    protected static float horizontalHit(final EnumFacing facing, final float hitX,
            final float hitZ) {
        switch (facing) {
            case NORTH:
                return 1f - hitX;
            case SOUTH:
                return hitX;
            case EAST:
                return 1f - hitZ;
            case WEST:
                return hitZ;
            default:
                return hitX;
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState,
            final IBlockAccess worldIn, final BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public boolean shouldHaveItem() {
        return false;
    }

    public MonitorTEBlock getTileEntityMonitorBlock() {
        return teMonitor;
    }

    public MonitorBlockProperties getMonitorProperties() {
        return prop;
    }

    public int getID() {
        return id;
    }

}
