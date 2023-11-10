package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class SignalBox extends BasicBlock {

    public static final TileEntitySupplierWrapper SUPPLIER = SignalBoxTileEntity::new;

    public SignalBox() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player playerIn, final InteractionHand hand, final BlockHitResult hit) {
        if (!playerIn.getItemInHand(InteractionHand.MAIN_HAND).getItem()
                .equals(OSItems.LINKING_TOOL)) {
            final BlockEntity entity = worldIn.getBlockEntity(pos);
            if ((entity instanceof SignalBoxTileEntity)
                    && !((SignalBoxTileEntity) entity).isBlocked()) {
                OpenSignalsMain.handler.invokeGui(SignalBox.class, playerIn, worldIn, pos,
                        "signalbox");
            } else {
                playerIn.sendMessage(new TranslatableComponent("msg.isblocked"),
                        playerIn.getUUID());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("signalbox");
    }

    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state,
            final Player player) {
        if (!world.isClientSide) {
            ((SignalBoxTileEntity) world.getBlockEntity(pos)).unlink();
            SignalBoxHandler.removeSignalBox(new StateInfo(world, pos));
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void onPlace(final BlockState state, final Level world, final BlockPos pos,
            final BlockState state2, final boolean bool) {
        SignalBoxHandler.relinkAllRedstoneIOs(new StateInfo(world, pos));
    }
}