package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;

public class ClientMonitorNetworkHandler implements INetworkSync {

    private static final Map<StateInfo, ReadBuffer> updates = new HashMap<>();

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            final World world = mc.world;
            final BlockPos pos = buf.getBlockPos();
            TileEntity entity = world.getTileEntity(pos);
            if (entity == null) {
                updates.put(new StateInfo(world, pos), buf);
                return;
            }
            if (!(entity instanceof MonitorTileEntity))
                return;
            executeUpdate((MonitorTileEntity) entity, buf);
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
        final ReadBuffer buf = updates.remove(new StateInfo(tile.getWorld(), tile.getPos()));
        if (buf != null) {
            executeUpdate(tile, buf);
        }
    }

    @SubscribeEvent
    public void serverEvent(final ClientCustomPacketEvent event) {
        deserializeClient(event.getPacket().payload().nioBuffer());
    }

}