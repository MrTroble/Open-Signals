package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.handler.ClientRenderUpdate;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class SignalController extends BasicBlock {

    public static final TileEntitySupplierWrapper SUPPLIER = SignalControllerTileEntity::new;

    public SignalController() {
        super(Properties.of(Material.METAL));
    }

    @Override
    public ActionResultType use(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity playerIn, final Hand hand, final BlockRayTraceResult hit) {
        if (!playerIn.getItemInHand(Hand.MAIN_HAND).getItem().equals(OSItems.LINKING_TOOL)) {
            OpenSignalsMain.handler.invokeGui(SignalController.class, playerIn, worldIn, pos,
                    "signalcontroller");
            return ActionResultType.SUCCESS;
        }

        //TODO: FIX

        return ActionResultType.FAIL;
    }
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
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        final TileEntity tile = world.getBlockEntity(pos);
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
}