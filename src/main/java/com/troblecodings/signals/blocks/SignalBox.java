package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class SignalBox extends BasicBlock {

    public static final TileEntitySupplierWrapper SUPPLIER = SignalBoxTileEntity::new;

    public SignalBox() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public ActionResultType use(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity playerIn, final Hand hand, final BlockRayTraceResult hit) {
        if (!playerIn.getItemInHand(Hand.MAIN_HAND).getItem().equals(OSItems.LINKING_TOOL)) {
            final TileEntity entity = worldIn.getBlockEntity(pos);
            if ((entity instanceof SignalBoxTileEntity)
                    && !((SignalBoxTileEntity) entity).isBlocked()) {
                OpenSignalsMain.handler.invokeGui(SignalBox.class, playerIn, worldIn, pos,
                        "signalbox");
            } else {
                playerIn.sendMessage(new TranslationTextComponent("msg.isblocked"),
                        playerIn.getUUID());
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
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
    public void playerWillDestroy(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player) {
        if (!world.isClientSide) {
            ((SignalBoxTileEntity) world.getBlockEntity(pos)).unlink();
            SignalBoxHandler.removeSignalBox(new PosIdentifier(pos, world));
        }
        super.playerWillDestroy(world, pos, state, player);
    }
    
    @Override
    public void onPlace(final BlockState state, final World world, final BlockPos pos,
            final BlockState state2, final boolean bool) {
        SignalBoxHandler.relinkAllRedstoneIOs(new PosIdentifier(pos, world));
    }
}