package com.troblecodings.signals.blocks;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class SignalBox extends Block implements EntityBlock {

    public SignalBox() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player playerIn, final InteractionHand hand, final BlockHitResult hit) {
        if (!playerIn.getItemInHand(hand).getItem().equals(OSItems.LINKING_TOOL)) {
            if (worldIn.isClientSide)
                return InteractionResult.SUCCESS;
            final BlockEntity entity = worldIn.getBlockEntity(pos);
            if ((entity instanceof SignalBoxTileEntity)
                    && !((SignalBoxTileEntity) entity).isBlocked()) {
                OpenSignalsMain.handler.invokeGui(SignalBox.class, playerIn, worldIn, pos);
            } else {
                playerIn.sendMessage(new TranslatableComponent("msg.isblocked"),
                        playerIn.getUUID());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos p_153215_, final BlockState p_153216_) {
        return new SignalBoxTileEntity();
    }

}
