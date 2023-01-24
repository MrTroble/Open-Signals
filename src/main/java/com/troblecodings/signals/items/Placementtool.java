package com.troblecodings.signals.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.guilib.ecs.interfaces.ITagableItem;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.statehandler.SignalStateHandler;
import com.troblecodings.signals.statehandler.SignalStateInfo;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Placementtool extends Item
        implements IIntegerable<Signal>, ITagableItem, MessageWrapper {

    public static final String BLOCK_TYPE_ID = "blocktypeid";
    public static final String SIGNAL_CUSTOMNAME = "customname";

    public final ArrayList<Signal> signals = new ArrayList<>();

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
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player player,
            final InteractionHand hand) {
        if (!worldIn.isClientSide) {
            OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn,
                    player.getOnPos(), "placementtool");
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                worldIn.isClientSide);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        final Level worldIn = context.getLevel();
        if (player.isShiftKeyDown()) {
            if (!worldIn.isClientSide) {
                OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn,
                        player.getOnPos(), "placementtool");
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(player.getMainHandItem());
        if (!wrapper.contains(BLOCK_TYPE_ID)) {
            translateMessageWrapper(player, "pt.itemnotset");
            return InteractionResult.FAIL;
        }
        final Signal signal = getObjFromID(wrapper.getInteger(BLOCK_TYPE_ID));
        final List<SEProperty> properties = signal.getProperties();
        final Map<SEProperty, String> signalProperties = new HashMap<>();
        properties.forEach(property -> {
            final String name = property.getName();
            if(wrapper.contains(name))
                signalProperties.put(property, wrapper.getString(name));
        });
        final int height = signal.getHeight(signalProperties);
        final BlockPos pos = context.getClickedPos().above();
        BlockPos checkPos = pos;
        for (int i = 0; i < height; i++) {
            if (!worldIn.isEmptyBlock(checkPos = checkPos.above())) {
                if (!worldIn.isClientSide)
                    translateMessageWrapper(player, "pt.blockinway");
                return InteractionResult.FAIL;
            }
        }
        BlockPos ghostPos = pos.above();
        for (int i = 0; i < height; i++) {
            worldIn.setBlock(ghostPos, OSBlocks.GHOST_BLOCK.defaultBlockState(), 3);
            ghostPos = ghostPos.above();
        }
        worldIn.setBlock(pos, signal.getStateForPlacement(new BlockPlaceContext(context)), 3);
        final SignalStateInfo info = new SignalStateInfo(worldIn, pos);
        SignalStateHandler.createStates(info, signalProperties);
        return InteractionResult.SUCCESS;
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
