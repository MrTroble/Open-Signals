package com.troblecodings.signals.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.guis.ContainerSignalBridge;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBridgeItem extends Item implements MessageWrapper {

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World worldIn,
            final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX,
            final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            OpenSignalsMain.handler.invokeGui(SignalBridgeBasicBlock.class, player, worldIn,
                    player.getPosition(), "signalbridge");
            return EnumActionResult.SUCCESS;
        }
        if (worldIn.isRemote)
            return EnumActionResult.PASS;
        final NBTWrapper tag = NBTWrapper.getOrCreateWrapper(player.getHeldItemMainhand())
                .getWrapper(ContainerSignalBridge.SIGNALBRIDGE_TAG);
        if (tag.isTagNull())
            return EnumActionResult.FAIL;
        final SignalBridgeBuilder builder = new SignalBridgeBuilder();
        builder.read(tag);
        final BlockPos startPos = pos.up();
        final Map<BlockPos, BasicBlock> blocks = calculatePositions(builder, startPos, facing);
        if (blocks.isEmpty()) {
            translateMessageWrapper(player, "signalbridge.nostartblock");
            return EnumActionResult.FAIL;
        }
        for (final BlockPos position : blocks.keySet()) {
            if (!worldIn.isAirBlock(position)) {
                translateMessageWrapper(player, "pt.blockinway");
                return EnumActionResult.FAIL;
            }
        }
        blocks.forEach((blockPos, block) -> worldIn.setBlockState(blockPos,
                block.getStateForPlacement(worldIn, blockPos, facing, hitX, hitY, hitZ,
                        maxStackSize, player, hand),
                3));

        final Map<String, Entry<Signal, Map<SEProperty, Integer>>> allSignals = new HashMap<>();
        (tag.isTagNull() ? new NBTWrapper() : tag).getList(ContainerSignalBridge.SIGNALPROPERTIES)
                .forEach(wrapper -> {
                    final Map<SEProperty, Integer> properties = new HashMap<>();
                    final String name = wrapper.getString(ContainerSignalBridge.SIGNAL_NAME);
                    final Signal signal =
                            Signal.SIGNALS.get(wrapper.getString(ContainerSignalBridge.SIGNAL_ID));
                    signal.getProperties().forEach(
                            property -> property.readFromNBT(wrapper).ifPresent(value -> properties
                                    .put(property, property.getParent().getIDFromValue(value))));
                    allSignals.put(name, Maps.immutableEntry(signal, properties));
                });
        builder.getAllSignalsInRelativeToStart().forEach((entry, vec) -> {
            final BlockPos signalPos = calculatePosForVectorAndDirection(vec, facing, startPos);
            createSignal(entry.getValue(), worldIn, player, entry.getKey(),
                    allSignals.get(entry.getKey()).getValue(), signalPos);
        });
        return EnumActionResult.SUCCESS;
    }

    private void createSignal(final Signal signal, final World worldIn, final EntityPlayer player,
            final String signalName, final Map<SEProperty, Integer> propertiesToInt,
            final BlockPos posToSet) {
        final List<SEProperty> properties = signal.getProperties();
        final Map<SEProperty, String> signalProperties = new HashMap<>();

        for (final SEProperty property : properties) {
            if (propertiesToInt.containsKey(property)) {
                if (!property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
                    signalProperties.put(property,
                            property.getObjFromID(propertiesToInt.get(property)));
                } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)
                        && propertiesToInt.get(property) > 0) {
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

        final int height = signal.getHeight(signalProperties);
        final BlockPos pos = posToSet.toImmutable();
        BlockPos ghostPos = pos.up();
        for (int i = 0; i < height; i++) {
            worldIn.setBlockState(ghostPos, OSBlocks.GHOST_BLOCK.getDefaultState(), 3);
            ghostPos = ghostPos.up();
        }
        String nametoSet = "";
        if (!(signalName == null || signalName.isEmpty())) {
            signalProperties.put(Signal.CUSTOMNAME, "true");
            nametoSet = signalName;
        } else {
            signalProperties.put(Signal.CUSTOMNAME, "false");
            nametoSet = signal.getSignalTypeName();
        }
        final SignalStateInfo info = new SignalStateInfo(worldIn, posToSet, signal);
        SignalStateHandler.createStates(info, signalProperties, player);
        NameHandler.createName(info, nametoSet);
    }

    private static Map<BlockPos, BasicBlock> calculatePositions(final SignalBridgeBuilder builder,
            final BlockPos startPos, final EnumFacing direction) {
        final Map<BlockPos, BasicBlock> blocks = new HashMap<>();
        builder.getRelativesToStart()
                .forEach(entry -> blocks.put(
                        calculatePosForVectorAndDirection(entry.getKey(), direction, startPos),
                        entry.getValue()));
        return blocks;
    }

    private static BlockPos calculatePosForVectorAndDirection(final VectorWrapper blockVec,
            final EnumFacing direction, final BlockPos startPos) {
        VectorWrapper vec = new VectorWrapper(blockVec.getX(), blockVec.getY(), blockVec.getZ());
        switch (direction) {
            case NORTH: {
                break;
            }
            case SOUTH: {
                vec = new VectorWrapper(-vec.getX(), vec.getY(), -vec.getZ());
                break;
            }
            case EAST: {
                vec = new VectorWrapper(-vec.getZ(), vec.getY(), vec.getX());
                break;
            }
            case WEST: {
                vec = new VectorWrapper(vec.getZ(), vec.getY(), -vec.getX());
                break;
            }
            default:
                break;
        }
        return vec.addToPos(startPos);
    }
}