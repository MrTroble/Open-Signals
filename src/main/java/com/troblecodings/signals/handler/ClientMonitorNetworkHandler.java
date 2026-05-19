package com.troblecodings.signals.handler;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientMonitorNetworkHandler implements INetworkSync {

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        final byte networkMode = buf.getByte();
        final Level world = Minecraft.getInstance().level;
        final BlockPos pos = buf.getBlockPos();
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null || !(tile instanceof MonitorTileEntity))
            return;
        final MonitorTileEntity monitorTile = (MonitorTileEntity) tile;
        if (networkMode == MonitorNetworkHandler.NETWORK_TILE_DATA) {
            monitorTile.loadRenderPoints(buf);
        }
        if (networkMode == MonitorNetworkHandler.NETWORK_UPDATE) {
            monitorTile.loadBoxUpdate(buf);
        }
    }

}
