package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler.SignalBoxNetworkListener;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public final class MonitorNetworkHandler {

    private MonitorNetworkHandler() {
    }

    public static final byte NETWORK_TILE_DATA = 0;
    public static final byte NETWORK_UPDATE = 1;

    private static final Map<StateInfo, SignalBoxNetworkListener> listeners = new HashMap<>();

    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "monitor_net");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new MonitorNetworkHandler());
    }

    public static void registerToNetworkChannel(final Object obj) {
        channel.registerObject(obj);
    }

    public static void registerMonitorToBox(final StateInfo info) {
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(info);
        if (network == null)
            return;
        final SignalBoxNetworkListener listener =
                new SignalBoxNetworkListener(info, b -> sendGridUpdate(info, b));
        network.addListener(listener, true);
        listeners.put(info, listener);
    }

    public static void unregisterMonitorFromBox(final StateInfo info) {
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(info);
        if (network == null)
            return;
        network.removeListener(listeners.remove(info));
    }

    public static void onMonitorLoad(final MonitorTileEntity tile, final StateInfo signalBox) {
        registerMonitorToBox(signalBox);
        sendTileData(tile);
    }

    private static SignalBoxNetworkHandler getNetworkFromSignalBox(final StateInfo info) {
        final BlockEntity tile = info.world.getBlockEntity(info.pos);
        if (tile == null || !(tile instanceof SignalBoxTileEntity))
            return null;
        final SignalBoxTileEntity signalTile = (SignalBoxTileEntity) tile;
        return signalTile.getSignalBoxGrid().getNetwork();
    }

    private static void sendTileData(final MonitorTileEntity tile) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(NETWORK_TILE_DATA);
        buffer.putBlockPos(tile.getBlockPos());
        tile.getRenderStart().writeNetwork(buffer);
        tile.getRenderEnd().writeNetwork(buffer);
        tile.getLevel().players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    private static void sendGridUpdate(final StateInfo info, final WriteBuffer networkBuffer) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(NETWORK_UPDATE);
        buffer.putBlockPos(info.pos);
        buffer.putBuffer(networkBuffer.getBuildedBuffer());
        info.world.players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));

    }

    private static void sendTo(final Player player, final ByteBuffer buf) {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(buf.position(0)));
        if (player instanceof ServerPlayer) {
            final ServerPlayer server = (ServerPlayer) player;
            server.connection.send(new ClientboundCustomPayloadPacket(channelName, buffer));
        } else {
            final Minecraft mc = Minecraft.getInstance();
            mc.getConnection().send(new ServerboundCustomPayloadPacket(channelName, buffer));
        }
    }

}
