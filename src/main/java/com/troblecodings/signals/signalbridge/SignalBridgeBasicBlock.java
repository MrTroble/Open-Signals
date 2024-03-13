package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.DestroyHelper;
import com.troblecodings.signals.enums.SignalBridgeType;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class SignalBridgeBasicBlock extends BasicBlock {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final List<SignalBridgeBasicBlock> ALL_SIGNALBRIDGE_BLOCKS = new ArrayList<>();

    private final SignalBridgeBlockProperties properties;
    // Just for Networking
    private final int id;

    public SignalBridgeBasicBlock(final SignalBridgeBlockProperties properties) {
        super(Material.ROCK);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING,
                placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                FACING
        });
    }

    @Override
    public boolean shouldHaveItem() {
        return ConfigHandler.enableSignalBridgeItems;
    }

    @Override
    public boolean shouldBeDestroyedWithOtherBlocks() {
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world,
            BlockPos pos, EntityPlayer player) {
        return null;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        DestroyHelper.checkAndDestroyOtherBlocks(worldIn, pos, state);
    }
}