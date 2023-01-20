package com.troblecodings.signals.items;

import java.util.ArrayList;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.guilib.ecs.interfaces.ITagableItem;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.guis.GuiPlacementtool;
import com.troblecodings.signals.init.OSTabs;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Placementtool extends Item
        implements IIntegerable<Signal>, ITagableItem, MessageWrapper {

    public static final String BLOCK_TYPE_ID = "blocktypeid";
    public static final String SIGNAL_CUSTOMNAME = "customname";

    public final ArrayList<Signal> signals = new ArrayList<>();
    public GuiPlacementtool guiPlacementtool;

    public Placementtool() {
        super(new Item.Properties().tab(OSTabs.TAB));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getNamedObj(final int obj) {
        return I18n.get("property." + this.getName() + ".name") + ": "
                + I18n.get(this.getObjFromID(obj).toString());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player,
            InteractionHand hand) {
        if (!worldIn.isClientSide) {
            OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn,
                    player.getOnPos(), "placementtool");
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                worldIn.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Player player = context.getPlayer();
        final Level worldIn = context.getLevel();
        if (player.isShiftKeyDown()) {
            if (!worldIn.isClientSide) {
                OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn,
                        player.getOnPos(), "placementtool");
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        return super.useOn(context);
    }

    @Override
    public Signal getObjFromID(final int obj) {
        return signals.get(obj);
    }

    @Override
    public int count() {
        return signals.size();
    }

    @Override
    public String getName() {
        return "signaltype";
    }

    public void addSignal(final Signal signal) {
        this.signals.add(signal);
    }

}
