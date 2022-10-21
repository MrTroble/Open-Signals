package com.troblecodings.signals.blocks;

import com.troblecodings.signals.init.SignaIItems;
import com.troblecodings.signals.init.SignalTabs;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import eu.gir.guilib.ecs.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneIO extends Block implements ITileEntityProvider {

    public static final PropertyBool POWER = PropertyBool.create("power");

    public RedstoneIO() {
        super(Material.ROCK);
        setCreativeTab(SignalTabs.TAB);
        this.setDefaultState(getDefaultState().withProperty(POWER, false));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(POWER) ? 0 : 1;
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(POWER, meta == 1);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                POWER
        });
    }

    @Override
    public int getStrongPower(final IBlockState blockState, final IBlockAccess blockAccess,
            final net.minecraft.util.math.BlockPos pos, final EnumFacing side) {
        return getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess blockAccess,
            final net.minecraft.util.math.BlockPos pos, final EnumFacing side) {
        return blockState.getValue(POWER) ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new RedstoneIOTileEntity();
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!playerIn.getHeldItemMainhand().getItem().equals(SignaIItems.LINKING_TOOL)) {
            if (worldIn.isRemote)
                GuiHandler.invokeGui(RedstoneIO.class, playerIn, worldIn, pos);
            return true;
        }
        return false;
    }
}
