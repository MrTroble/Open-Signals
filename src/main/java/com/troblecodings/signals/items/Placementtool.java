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
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.guis.ContainerPlacementtool;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSTabs;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Placementtool extends Item
        implements IIntegerable<Signal>, ITagableItem, MessageWrapper {

    public static final String BLOCK_TYPE_ID = "blocktypeid";
    public static final String SIGNAL_CUSTOMNAME = "customname";

    public final ArrayList<Signal> signals = new ArrayList<>();

    public Placementtool() {
        setCreativeTab(OSTabs.TAB);
        setMaxDamage(100);
        setNoRepair();
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getNamedObj(final int obj) {
        return I18n.format("property." + this.getName() + ".name") + ": "
                + I18n.format(this.getObjFromID(obj).toString());
    }

    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world,
            final BlockPos pos, final EnumFacing side, final float hitX, final float hitY,
            final float hitZ, final EnumHand hand) {
        return onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World worldIn,
            final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX,
            final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            if (worldIn.isRemote)
                return EnumActionResult.SUCCESS;
            OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn, pos,
                    "placementtool");
            return EnumActionResult.SUCCESS;
        }
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(player.getHeldItemMainhand());
        if (!wrapper.contains(BLOCK_TYPE_ID)) {
            wrapper.putInteger(BLOCK_TYPE_ID, 0);
        }
        final BlockPos placePos = pos.offset(facing);
        final Signal signal = getObjFromID(wrapper.getInteger(BLOCK_TYPE_ID));

        final List<SEProperty> properties = signal.getProperties();
        final Map<SEProperty, String> signalProperties = new HashMap<>();
        int cost = signal.getDefaultDamage();

        for (final SEProperty property : properties) {
            final String name = property.getName();
            if (wrapper.contains(name)) {
                cost += property.getItemDamage();

                if (!property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
                    signalProperties.put(property, wrapper.getString(name));
                } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
                    signalProperties.put(property, property.getDefault());
                }
            } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)
                    && property.testMap(signalProperties)) {
                signalProperties.put(property, property.getDefault());
            } else if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)
                    || property.isChangabelAtStage(ChangeableStage.AUTOMATICSTAGE)) {
                signalProperties.put(property, property.getDefault());
            }
        }
        final SignalStateInfo info = new SignalStateInfo(worldIn, placePos, signal);
        SignalStateHandler.createStates(info, signalProperties);

        final String signalName = wrapper.getString(ContainerPlacementtool.SIGNAL_NAME);
        final NameStateInfo nameInfo = new NameStateInfo(worldIn, pos);
        if (!(signalName == null || signalName.isEmpty())) {
            signalProperties.put(Signal.CUSTOMNAME, "true");
            NameHandler.createName(nameInfo, signalName);
        } else {
            signalProperties.put(Signal.CUSTOMNAME, "false");
            NameHandler.createName(nameInfo, signal.getSignalTypeName());
        }

        worldIn.setBlockState(placePos, signal.getStateForPlacement(worldIn, placePos, facing, hitX,
                hitY, hitZ, 0, player, hand), 3);

        final ItemStack item = player.getHeldItemMainhand();
        item.damageItem(Math.abs(cost), player);

        final int height = signal.getHeight(signalProperties);
        BlockPos checkPos = placePos.up();
        for (int i = 0; i < height; i++) {
            if (!worldIn.isAirBlock(checkPos)) {
                if (!worldIn.isRemote)
                    translateMessageWrapper(player, "pt.blockinway");
                return EnumActionResult.FAIL;
            }
            checkPos = checkPos.up();
        }

        BlockPos ghostPos = placePos.up();
        for (int i = 0; i < height; i++) {
            worldIn.setBlockState(ghostPos, OSBlocks.GHOST_BLOCK.getDefaultState(), 3);
            ghostPos = ghostPos.up();
        }
        final ExtendedBlockState ebs = (ExtendedBlockState) signal.getBlockState();
        worldIn.notifyBlockUpdate(placePos, ebs.getBaseState(), ebs.getBaseState(), 3);
        return EnumActionResult.SUCCESS;
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