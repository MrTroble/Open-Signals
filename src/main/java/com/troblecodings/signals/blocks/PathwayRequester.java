package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.PathwayRequesterTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PathwayRequester extends BasicBlock {

    public static final PropertyBool POWERD = PropertyBool.create("powerd");

    public PathwayRequester() {
        super(Material.ROCK);
    }

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos) {
        if (worldIn.isRemote)
            return;
        if (worldIn.isBlockPowered(pos)) {
            if (!state.getValue(POWERD)) {
                worldIn.setBlockState(pos, state.withProperty(POWERD, true));
                final TileEntity entity = worldIn.getTileEntity(pos);
                if (entity != null) {
                    ((PathwayRequesterTileEntity) entity).requestPathway();
                }
            } else {
                worldIn.setBlockState(pos, state.withProperty(POWERD, false));
            }
        }
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(POWERD) ? 0 : 1;
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(POWERD, meta == 1);
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final Item item = playerIn.getHeldItemMainhand().getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) || item.equals(OSItems.MULTI_LINKING_TOOL))) {
            OpenSignalsMain.handler.invokeGui(PathwayRequester.class, playerIn, worldIn, pos,
                    "pathwayrequester");
            return true;
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                POWERD
        });
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("pathwayrequeseter");
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new PathwayRequesterTileEntity();
    }
}