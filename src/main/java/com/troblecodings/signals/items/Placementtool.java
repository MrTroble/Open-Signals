package com.troblecodings.signals.items;

import java.util.ArrayList;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.guilib.ecs.interfaces.ITagableItem;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.property.ExtendedBlockState;

public class Placementtool extends Item implements IIntegerable<Signal>, ITagableItem {

    public static final String BLOCK_TYPE_ID = "blocktypeid";
    public static final String SIGNAL_CUSTOMNAME = "customname";

    public final ArrayList<Integer> signalids = new ArrayList<>();

    public Placementtool() {
    	super(new Item.Properties().tab(OSTabs.TAB));
    }

    public void addSignal(final Signal sig) {
        signalids.add(sig.getID());
    }
    
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    	Player player = context.getPlayer();
    	Level worldIn = context.getLevel();
    	BlockPos pos = context.getClickedPos();
        if (player.isShiftKeyDown()) {
            if (!worldIn.isClientSide)
                return InteractionResult.SUCCESS;
            OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn, pos);
            return InteractionResult.SUCCESS;
        } else {
            if (worldIn.isClientSide)
                return InteractionResult.SUCCESS;
            final BlockPos setPosition = pos.offset(facing);
            if (!worldIn.isEmptyBlock(setPosition))
                return InteractionResult.FAIL;

            final CompoundTag compound = player.getMainHandItem().getOrCreateTag();
            if(!compound.hasKey(BLOCK_TYPE_ID)) {
                player.sendMessage(new TextComponentTranslation("pt.itemnotset"));
                return InteractionResult.FAIL;
            }
            final Signal block = Signal.SIGNALLIST.get(compound.getInt(BLOCK_TYPE_ID));

            BlockPos lastPos = setPosition;
            worldIn.setBlockState(setPosition, block.getStateForPlacement(worldIn, lastPos, facing,
                    hitX, hitY, hitZ, 0, player, hand));

            final SignalTileEnity sig = (SignalTileEnity) worldIn.getTileEntity(setPosition);
            final ExtendedBlockState ebs = ((ExtendedBlockState) block.getBlockState());
            ebs.getUnlistedProperties().forEach(iup -> {
                final SEProperty sep = SEProperty.cst(iup);
                if (sep.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
                    sig.setProperty(sep, sep.getDefault());
                    return;
                }
                if (!compound.hasKey(iup.getName()))
                    return;
                if (sep.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
                    sig.setProperty(sep, sep.getObjFromID(compound.getInt(iup.getName())));
                } else if (sep.isChangabelAtStage(ChangeableStage.APISTAGE)
                        && compound.getInt(iup.getName()) == 1) {
                    sig.setProperty(sep, sep.getDefault());
                }
            });

            final int height = block.getHeight(sig.getProperties());
            for (int i = 0; i < height; i++)
                if (!worldIn.isAirBlock(lastPos = lastPos.up())) {
                    worldIn.setBlockToAir(setPosition);
                    return InteractionResult.FAIL;
                }
            lastPos = setPosition;
            for (int i = 0; i < height; i++)
                worldIn.setBlockState(lastPos = lastPos.up(),
                        OSBlocks.GHOST_BLOCK.getDefaultState());

            final String str = compound.getString(SIGNAL_CUSTOMNAME);
            if (!str.isEmpty())
                sig.setCustomName(str);
            worldIn.notifyBlockUpdate(setPosition, ebs.getBaseState(), ebs.getBaseState(), 3);
            return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getNamedObj(final int obj) {
        return I18n.get("property." + this.getName() + ".name") + ": "
                + this.getObjFromID(obj);
    }

    @Override
    public Signal getObjFromID(final int obj) {
        return Signal.SIGNALLIST.get(signalids.get(obj));
    }

    @Override
    public int count() {
        return signalids.size();
    }

    @Override
    public String getName() {
        return "signaltype";
    }

}
