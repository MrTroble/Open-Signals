package com.troblecodings.signals.items;

import java.util.ArrayList;
import java.util.Map;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.guilib.ecs.interfaces.ITagableItem;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.statehandler.SignalStateHandler;
import com.troblecodings.signals.statehandler.SignalStateInfo;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
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

public class Placementtool extends BlockItem
        implements IIntegerable<Signal>, ITagableItem, MessageWrapper {

    public static final String BLOCK_TYPE_ID = "blocktypeid";
    public static final String SIGNAL_CUSTOMNAME = "customname";

    public final ArrayList<Signal> signals = new ArrayList<>();

    public Placementtool() {
        super(Blocks.AIR, new Item.Properties().tab(OSTabs.TAB));
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
    public InteractionResult place(BlockPlaceContext context) {
        final InteractionResult result = super.place(context);
        if (result == InteractionResult.SUCCESS) {
            final Player player = context.getPlayer();
            final Level worldIn = context.getLevel();
            final NBTWrapper compound = new NBTWrapper(player.getMainHandItem().getOrCreateTag());
            final BlockPos pos = context.getClickedPos();
            final SignalTileEntity tile = (SignalTileEntity) worldIn.getBlockEntity(pos);
            final Signal signal = (Signal) tile.getBlockState().getBlock();

            // TODO set signal

            final int height = signal
                    .getHeight(SignalStateHandler.getStates(new SignalStateInfo(worldIn, pos)));
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
            // TODO Custom name
        }
        return result;
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
    }

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        final Player player = context.getPlayer();
        final NBTWrapper compound = new NBTWrapper(player.getMainHandItem().getOrCreateTag());
        if (!compound.contains(BLOCK_TYPE_ID)) {
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
