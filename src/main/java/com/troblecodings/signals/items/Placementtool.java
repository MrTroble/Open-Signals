package com.troblecodings.signals.items;

import java.util.ArrayList;
import java.util.UUID;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.guilib.ecs.interfaces.ITagableItem;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSTabs;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Placementtool extends BlockItem implements IIntegerable<Signal>, ITagableItem, MessageWrapper {

	public static final String BLOCK_TYPE_ID = "blocktypeid";
	public static final String SIGNAL_CUSTOMNAME = "customname";

	public final ArrayList<Signal> signals = new ArrayList<>();

	public Placementtool() {
		super(Blocks.AIR, new Item.Properties().tab(OSTabs.TAB));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
		if (player.isShiftKeyDown()) {
			if (worldIn.isClientSide)
				return InteractionResultHolder.success(player.getItemInHand(hand));
			OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn, player.getOnPos());
			return InteractionResultHolder.success(player.getItemInHand(hand));
		}
		return super.use(worldIn, player, hand);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		final InteractionResult result = super.place(context);
		if(result == InteractionResult.SUCCESS) {
	    	final Player player = context.getPlayer();
	    	final Level worldIn = context.getLevel();
	        final NBTWrapper compound = new NBTWrapper(player.getMainHandItem().getOrCreateTag());
			final Signal signal = (Signal) getPlacementState(context).getBlock();
            final int height = signal.getHeight(signal.getProperties());
            BlockPos lastPos = context.getClickedPos();
            for (int i = 0; i < height; i++) {
                if (!worldIn.isEmptyBlock(lastPos = lastPos.above())) {
                    worldIn.removeBlock(lastPos, true);
                    return InteractionResult.FAIL;
                }
            }
            lastPos = context.getClickedPos();
            for (int i = 0; i < height; i++) {
                worldIn.setBlockAndUpdate(lastPos = lastPos.above(),
                        OSBlocks.GHOST_BLOCK.getStateForPlacement(context));
            }

            final String name = compound.getString(SIGNAL_CUSTOMNAME);
            if (!name.isEmpty())
                sig.setCustomName(name);
		}
		return result;
	}
	
	@Override
	protected BlockState getPlacementState(BlockPlaceContext context) {
    	final Player player = context.getPlayer();
        final NBTWrapper compound = new NBTWrapper(player.getMainHandItem().getOrCreateTag());
        if(!compound.contains(BLOCK_TYPE_ID)) {
            translateMessageWrapper(player, "pt.itemnotset");
            return null;
        }
        return this.signals.get(compound.getInteger(BLOCK_TYPE_ID)).getStateForPlacement(context);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public String getNamedObj(final int obj) {
		return I18n.get("property." + this.getName() + ".name") + ": " + this.getObjFromID(obj);
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

	public void addSignal(Signal signal) {
		this.signals.add(signal);
	}

}
