package com.troblecodings.signals.items;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Monitor;
import com.troblecodings.signals.guis.ContainerMonitorSelection;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MonitorSelectionItem extends Item implements MessageWrapper {

    public MonitorSelectionItem() {
        super(new Item.Properties().tab(OSTabs.TAB).durability(100).setNoRepair());
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        final Level worldIn = context.getLevel();
        if (worldIn.isEmptyBlock(context.getClickedPos()))
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown()) {
            if (!worldIn.isClientSide) {
                OpenSignalsMain.handler.invokeGui(Monitor.class, player, worldIn, player.getOnPos(),
                        "monitor");
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        if (worldIn.isClientSide)
            return InteractionResult.CONSUME;
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(player.getMainHandItem());
        final int monitorID = wrapper.getInteger(ContainerMonitorSelection.MONITOR_TYPE_ID);
        final Monitor monitor = Monitor.MONITORS.get(monitorID);

        final int sizeX = wrapper.getInteger(ContainerMonitorSelection.SIZE_X);
        final int sizeY = wrapper.getInteger(ContainerMonitorSelection.SIZE_Y);

        final List<BlockPos> allMonitorPos = getMonitorPos(context.getClickedPos().above(),
                context.getHorizontalDirection().getOpposite(), sizeX, sizeY);

        if (allMonitorPos.isEmpty())
            return InteractionResult.CONSUME;

        for (final BlockPos pos : allMonitorPos) {
            if (!worldIn.isEmptyBlock(pos)) {
                if (!worldIn.isClientSide) {
                    translateMessageWrapper(player, "pt.blockinway");
                }
                return InteractionResult.FAIL;
            }
        }
        final BlockPlaceContext placeContext = new BlockPlaceContext(context);
        final BlockPos firstPos = allMonitorPos.remove(0);
        worldIn.setBlock(firstPos,
                monitor.getTileEntityMonitorBlock().getStateForPlacement(placeContext), 3);
        final MonitorTileEntity tile = (MonitorTileEntity) worldIn.getBlockEntity(firstPos);
        tile.loadFromItem(sizeX, sizeY);

        for (final BlockPos pos : allMonitorPos) {
            worldIn.setBlock(pos, monitor.getStateForPlacement(placeContext), 3);
        }
        MonitorNetworkHandler.sendTileData(tile, worldIn.players());
        return InteractionResult.SUCCESS;
    }

    private List<BlockPos> getMonitorPos(final BlockPos start, final Direction direction,
            final int sizeX, final int sizeY) {
        final List<BlockPos> list = new ArrayList<>();
        for (int i = 0; i < sizeY; i++) {
            final BlockPos yPos = start.relative(Direction.Axis.Y, i);
            for (int j = 0; j < sizeX; j++) {
                switch (direction) {
                    case NORTH:
                    case DOWN:
                    case UP:
                        list.add(yPos.relative(Direction.Axis.X, -j));
                        break;
                    case EAST:
                        list.add(yPos.relative(Direction.Axis.Z, -j));
                        break;
                    case SOUTH:
                        list.add(yPos.relative(Direction.Axis.X, j));
                        break;
                    case WEST:
                        list.add(yPos.relative(Direction.Axis.Z, j));
                        break;
                }
            }
        }
        return list;
    }

}
