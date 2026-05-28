package com.troblecodings.signals.handler;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;

public class ClientMonitorNetworkHandler implements INetworkSync {

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            final byte networkMode = buf.getByte();
            final World world = mc.world;
            final BlockPos pos = buf.getBlockPos();
            TileEntity entity = world.getTileEntity(pos);
            if (!(entity instanceof MonitorTileEntity))
                return;
            final MonitorTileEntity monitorTile = (MonitorTileEntity) entity;
            synchronized (monitorTile) {
                if (networkMode == MonitorNetworkHandler.NETWORK_TILE_DATA) {
                    monitorTile.loadRenderPoints(buf);
                }
                if (networkMode == MonitorNetworkHandler.NETWORK_UPDATE) {
                    monitorTile.loadBoxUpdate(buf);
                }
            }
        });
    }

    @SubscribeEvent
    public void serverEvent(final ClientCustomPacketEvent event) {
        deserializeClient(event.getPacket().payload().nioBuffer());
    }

}