package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class SignalController extends BasicBlock {

	public static final TileEntitySupplierWrapper SUPPLIER = SignalBoxTileEntity::new;

    public SignalController() {
        super(Properties.of(Material.METAL));
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player playerIn, final InteractionHand hand, final BlockHitResult hit) {
        if (!playerIn.getItemInHand(hand).getItem().equals(OSItems.LINKING_TOOL)) {
            if (worldIn.isClientSide)
                return InteractionResult.SUCCESS;
            OpenSignalsMain.handler.invokeGui(SignalController.class, playerIn, worldIn, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof SignalControllerTileEntity) {
            ((SignalControllerTileEntity) tile).redstoneUpdate();
        }
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
    	return Optional.of(SUPPLIER);
    }

}
