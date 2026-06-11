package com.troblecodings.signals.handler;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientMonitorNetworkHandler implements INetworkSync {

    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();
    private static final Map<StateInfo, ReadBuffer> UPDATES = new HashMap<>();

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        final Minecraft mc = Minecraft.getInstance();
        mc.doRunTask(() -> {
            final Level world = mc.level;
            final BlockPos pos = buf.getBlockPos();
            final long startTime = Calendar.getInstance().getTimeInMillis();
            SERVICE.execute(() -> {
                BlockEntity entity;
                while ((entity = world.getBlockEntity(pos)) == null) {
                    final long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (currentTime - startTime >= 5000) {
                        UPDATES.put(new StateInfo(world, pos), buf);
                        return;
                    }
                    continue;
                }
                if (!(entity instanceof MonitorTileEntity))
                    return;
                executeUpdate((MonitorTileEntity) entity, buf);
            });
        });
    }

    private static void executeUpdate(final MonitorTileEntity tile, final ReadBuffer buf) {
        final byte networkMode = buf.getByte();
        synchronized (tile) {
            if (networkMode == MonitorNetworkHandler.NETWORK_TILE_DATA) {
                tile.loadRenderPoints(buf);
            }
            if (networkMode == MonitorNetworkHandler.NETWORK_UPDATE) {
                tile.loadBoxUpdate(buf);
            }
        }
    }

    public static void loadUpdate(final MonitorTileEntity tile) {
        final ReadBuffer buf = UPDATES.remove(new StateInfo(tile.getLevel(), tile.getBlockPos()));
        if (buf != null) {
            executeUpdate(tile, buf);
        }
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

}