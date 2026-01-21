package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.handler.ClientRenderUpdate;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class SignalController extends BasicBlock {

    public static final TileEntitySupplierWrapper SUPPLIER = SignalControllerTileEntity::new;

    public SignalController() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote)
            return true;

        final Item item = playerIn.getHeldItemMainhand().getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) || item.equals(OSItems.MULTI_LINKING_TOOL))) {
            if (playerIn.isSneaking()) {
                final TileEntity tile = worldIn.getTileEntity(pos);

                if (!(tile instanceof SignalControllerTileEntity))
                    return false;

                final SignalControllerTileEntity controller = (SignalControllerTileEntity) tile;

                if (controller.hasLink()) {
                    ClientRenderUpdate.INSTANCE.clearHighlights();
                    ClientRenderUpdate.INSTANCE.addHighlight(controller.getLinkedPosition());
                } else {
                    playerIn.sendMessage(new TextComponentString("No Link"));
                }

                return true;
            } else {
                OpenSignalsMain.handler.invokeGui(SignalController.class, playerIn, worldIn, pos,
                        "signalcontroller");
            }
            return true;
        }
        return false;

    }

    @Override
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof SignalControllerTileEntity) {
            ((SignalControllerTileEntity) tile).redstoneUpdate();
        }
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("controller");
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new SignalControllerTileEntity();
    }
}