package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneIO extends BasicBlock {

    public static final PropertyBool POWER = PropertyBool.create("power");
    public static final TileEntitySupplierWrapper SUPPLIER = RedstoneIOTileEntity::new;

    public RedstoneIO() {
        super(Material.ROCK);
        this.setDefaultState(getDefaultState().withProperty(POWER, false));
    }

    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ,
            final int meta, final EntityLivingBase placer, final EnumHand hand) {
        if (!world.isRemote)
            NameHandler.createName(new NameStateInfo(world, pos), getLocalizedName());
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
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
            final BlockPos pos, final EnumFacing side) {
        return getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess blockAccess,
            final BlockPos pos, final EnumFacing side) {
        return blockState.getValue(POWER) ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!playerIn.getHeldItemMainhand().getItem().equals(OSItems.LINKING_TOOL)) {
            OpenSignalsMain.handler.invokeGui(RedstoneIO.class, playerIn, worldIn, pos,
                    "redstoneio");
            return true;
        }
        return false;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("redstoneio");
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new RedstoneIOTileEntity();
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (!worldIn.isRemote) {
            new Thread(() -> {
                NameHandler.setRemoved(new NameStateInfo(worldIn, pos));
                SignalBoxHandler.onPosRemove(new PosIdentifier(pos, worldIn));
            }, "RedstoneIO:breakBlock").start();
        }
        super.breakBlock(worldIn, pos, state);
    }
}
