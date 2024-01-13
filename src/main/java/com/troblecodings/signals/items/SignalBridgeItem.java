package com.troblecodings.signals.items;

import com.troblecodings.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSTabs;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SignalBridgeItem extends Item {

    public SignalBridgeItem() {
        super(new Item.Properties().tab(OSTabs.TAB).durability(100).setNoRepair());
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        final Level worldIn = context.getLevel();
        if (player.isShiftKeyDown()) {
            OpenSignalsMain.handler.invokeGui(SignalBridgeBasicBlock.class, player, worldIn,
                    player.getOnPos(), "signalbridge");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

}
