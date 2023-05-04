package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
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
        return ActionResultType.FAIL;
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