package com.troblecodings.signals.items;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.guis.SignalBridgeContainer;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SignalBridgeItem extends Item implements MessageWrapper {

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
        if (worldIn.isClientSide)
            return InteractionResult.PASS;
        final NBTWrapper tag = NBTWrapper.getOrCreateWrapper(player.getMainHandItem())
                .getWrapper(SignalBridgeContainer.SIGNALBRIDGE_TAG);
        if (tag.isTagNull())
            return InteractionResult.FAIL;
        final SignalBridgeBuilder builder = new SignalBridgeBuilder();
        builder.read(tag);
        final BlockPos startPos = context.getClickedPos().above();
        final Map<BlockPos, BasicBlock> blocks = calculatePositions(builder, startPos,
                context.getHorizontalDirection().getOpposite());
        if (blocks.isEmpty()) {
            translateMessageWrapper(player, "signalbridge.noblock");
        }
        for (final BlockPos pos : blocks.keySet()) {
            if (!worldIn.isEmptyBlock(pos)) {
                translateMessageWrapper(player, "pt.blockinway");
                return InteractionResult.FAIL;
            }
        }
        blocks.forEach((pos, block) -> worldIn.setBlock(pos,
                block.getStateForPlacement(new BlockPlaceContext(context)), 3));
        return InteractionResult.SUCCESS;
    }

    private static Map<BlockPos, BasicBlock> calculatePositions(final SignalBridgeBuilder builder,
            final BlockPos startPos, final Direction direction) {
        final Map<BlockPos, BasicBlock> blocks = new HashMap<>();
        final Direction.Axis axis = direction.getAxis();
        builder.getRelativesToStart().forEach(entry -> {
            final Vec3i vec = translateVec3i(entry.getKey(), direction);
            BlockPos pos = startPos.offset(vec);
            if (axis.equals(Direction.Axis.X)) {
                final int distanceX = pos.getX() - startPos.getX();
                pos = new BlockPos(pos.getX() - distanceX, pos.getY(), pos.getZ() + distanceX);
            } else if (axis.equals(Direction.Axis.Z)) {
                final int distanceZ = pos.getZ() - startPos.getZ();
                pos = new BlockPos(pos.getX() + distanceZ, pos.getY(), pos.getZ() - distanceZ);
            }
            blocks.put(pos, entry.getValue());
        });
        return blocks;
    }

    private static Vec3i translateVec3i(final Vec3i vec, final Direction direction) {
        Vec3i returnVec = vec;
        if (direction.equals(Direction.WEST) || direction.equals(Direction.SOUTH)) {
            returnVec = new Vec3i(-vec.getX(), vec.getY(), -vec.getZ());
        }
        return returnVec;
    }
}