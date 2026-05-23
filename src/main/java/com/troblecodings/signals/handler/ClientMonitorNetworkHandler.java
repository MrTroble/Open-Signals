package com.troblecodings.signals.handler;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientMonitorNetworkHandler implements INetworkSync {

    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        final Minecraft mc = Minecraft.getInstance();
        mc.doRunTask(() -> {
            final byte networkMode = buf.getByte();
            final Level world = mc.level;
            final BlockPos pos = buf.getBlockPos();

            final long startTime = Calendar.getInstance().getTimeInMillis();
            SERVICE.execute(() -> {
                BlockEntity entity;
                while ((entity = world.getBlockEntity(pos)) == null) {
                    final long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (currentTime - startTime >= 5000)
                        return;
                    continue;
                }
                if (!(entity instanceof MonitorTileEntity))
                    return;
                final MonitorTileEntity monitorTile = (MonitorTileEntity) entity;
                if (networkMode == MonitorNetworkHandler.NETWORK_TILE_DATA) {
                    monitorTile.loadRenderPoints(buf);
                }
                if (networkMode == MonitorNetworkHandler.NETWORK_UPDATE) {
                    monitorTile.loadBoxUpdate(buf);
                }
            });
        });
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

}