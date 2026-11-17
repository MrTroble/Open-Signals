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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MonitorSelectionItem extends Item implements MessageWrapper {

    public MonitorSelectionItem() {
        setCreativeTab(OSTabs.TAB);
        setMaxDamage(100);
        setNoRepair();
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World worldIn,
            final BlockPos pos, final EnumHand hand, final EnumFacing face, final float hitX,
            final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            if (!worldIn.isRemote) {
                OpenSignalsMain.handler.invokeGui(Monitor.class, player, worldIn, pos, "monitor");
            }
            return EnumActionResult.SUCCESS;
        }
        if (worldIn.isRemote)
            return EnumActionResult.PASS;
        final EnumFacing facing = player.getHorizontalFacing().getOpposite();
        final BlockPos placePos = pos.offset(face);
        if (!worldIn.isAirBlock(placePos))
            return EnumActionResult.FAIL;

        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(player.getHeldItemMainhand());
        final int monitorID = wrapper.getInteger(ContainerMonitorSelection.MONITOR_TYPE_ID);
        final Monitor monitor = Monitor.MONITORS.get(monitorID);

        final int sizeX = wrapper.getInteger(ContainerMonitorSelection.SIZE_X);
        final int sizeY = wrapper.getInteger(ContainerMonitorSelection.SIZE_Y);

        final List<BlockPos> allMonitorPos = getMonitorPos(pos.up(), facing, sizeX, sizeY);

        if (allMonitorPos.isEmpty())
            return EnumActionResult.FAIL;

        for (final BlockPos monitorPos : allMonitorPos) {
            if (!worldIn.isAirBlock(monitorPos)) {
                if (!worldIn.isRemote) {
                    translateMessageWrapper(player, "pt.blockinway");
                }
                return EnumActionResult.FAIL;
            }
        }
        final BlockPos firstPos = allMonitorPos.remove(0);
        worldIn.setBlockState(firstPos, monitor.getTileEntityMonitorBlock().getStateForPlacement(
                worldIn, firstPos, facing, hitX, hitY, hitZ, 0, player, hand), 3);
        final MonitorTileEntity tile = (MonitorTileEntity) worldIn.getTileEntity(firstPos);
        tile.loadFromItem(sizeX, sizeY);

        for (final BlockPos monitorPos : allMonitorPos) {
            worldIn.setBlockState(monitorPos, monitor.getStateForPlacement(worldIn, monitorPos,
                    facing, hitX, hitY, hitZ, 0, player, hand), 3);
        }
        MonitorNetworkHandler.sendTileData(tile, worldIn.playerEntities);
        return EnumActionResult.SUCCESS;
    }

    private List<BlockPos> getMonitorPos(final BlockPos start, final EnumFacing direction,
            final int sizeX, final int sizeY) {
        final List<BlockPos> list = new ArrayList<>();
        for (int i = 0; i < sizeY; i++) {
            final BlockPos yPos = start.add(0, i, 0);
            for (int j = 0; j < sizeX; j++) {
                switch (direction) {
                    case NORTH:
                    case DOWN:
                    case UP:
                        list.add(yPos.add(-j, 0, 0));
                        break;
                    case EAST:
                        list.add(yPos.add(0, 0, -j));
                        break;
                    case SOUTH:
                        list.add(yPos.add(j, 0, 0));
                        break;
                    case WEST:
                        list.add(yPos.add(0, 0, j));
                        break;
                }
            }
        }
        return list;
    }
}
