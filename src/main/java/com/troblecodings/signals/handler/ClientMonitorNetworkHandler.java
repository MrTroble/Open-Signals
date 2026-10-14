package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientMonitorNetworkHandler implements INetworkSync {

    private static final Map<StateInfo, ReadBuffer> UPDATES = new HashMap<>();

    @SideOnly(Side.CLIENT)
    @Override
    public void deserializeClient(final ReadBuffer buf) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            final World world = mc.world;
            final BlockPos pos = buf.getBlockPos();
            if (pos.equals(BlockPos.ORIGIN)) {
                sendInfoMessageToPlayer(buf);
                return;
            }
            TileEntity entity = world.getTileEntity(pos);
            if (entity == null) {
                UPDATES.put(new StateInfo(world, pos), buf);
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

    private static void sendInfoMessageToPlayer(final ReadBuffer buffer) {
        final EntityPlayerSP player = Minecraft.getMinecraft().player;
        final byte mode = buffer.getByte();
        final PathwayRequestMode requestMode = buffer.getEnumValue(PathwayRequestMode.class);
        if (mode == MonitorNetworkHandler.PATHWAY_ADD_SAVER) {
            player.sendMessage(
                    new TextComponentString("[" + I18Wrapper.format("tile.signalbox.name") + "]: "
                            + I18Wrapper.format("error." + requestMode.getName()) + " - "
                            + I18Wrapper.format("info.pathwaysaver")));
        }
        if (mode == MonitorNetworkHandler.PATHWAY_RESPONSE) {
            player.sendMessage(
                    new TextComponentString("[" + I18Wrapper.format("tile.signalbox.name") + "]: "
                            + I18Wrapper.format("error." + requestMode.getName())));
        }
    }

    public static void loadUpdate(final MonitorTileEntity tile) {
        final ReadBuffer buf = UPDATES.remove(new StateInfo(tile.getWorld(), tile.getPos()));
        if (buf != null) {
            executeUpdate(tile, buf);
        }
    }

    @SubscribeEvent
    public void serverEvent(final ClientCustomPacketEvent event) {
        deserializeClient(event.getPacket().payload().nioBuffer());
    }

}