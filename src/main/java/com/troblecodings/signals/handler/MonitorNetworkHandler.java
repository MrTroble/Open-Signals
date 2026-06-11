package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    public static void registerMonitorToBox(final MonitorTileEntity tile) {
        registerMonitorToBox(tile, null);
    }

    public static void registerMonitorToBox(final MonitorTileEntity tile,
            final @Nullable Player player) {
        final StateInfo signalBoxInfo = tile.getStateInfo();
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(signalBoxInfo);
        if (network == null)
            return;
        final StateInfo monitorInfo = new StateInfo(tile.getLevel(), tile.getBlockPos());
        final SignalBoxNetworkListener listener =
                new SignalBoxNetworkListener(monitorInfo, b -> sendGridUpdate(tile, b));
        network.addListener(listener);
        listeners.put(monitorInfo, listener);
        sendInitGridUpdate(tile, player);
    }

    public static void deregisterMonitorFromBox(final MonitorTileEntity tile) {
        final StateInfo info = tile.getStateInfo();
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(info);
        if (network == null)
            return;
        final StateInfo monitorInfo = new StateInfo(tile.getLevel(), tile.getBlockPos());
        network.removeListener(listeners.remove(monitorInfo));
    }

    public static void checkForClientUpdates(final StateInfo signalBoxInfo,
            final SignalBoxNetworkHandler network, final WriteBuffer buffer) {
        if (signalBoxInfo.isWorldNullOrClientSide())
            return;
        network.getListeners().stream()
                .filter(listener -> !listener.info.pos.equals(signalBoxInfo.pos))
                .forEach(listener -> listener.consumer.accept(buffer));
    }

    @SubscribeEvent
    public static void onChunkWatch(final ChunkWatchEvent.Watch event) {
        final ServerLevel world = event.getWorld();
        if (world.isClientSide)
            return;
        final LevelChunk chunk = (LevelChunk) world.getChunk(event.getPos().getWorldPosition());
        final Player player = event.getPlayer();
        ImmutableMap.copyOf(chunk.getBlockEntities()).forEach((pos, tile) -> {
            if (tile instanceof MonitorTileEntity) {
                sendTileData((MonitorTileEntity) tile, ImmutableList.of(player));
                registerMonitorToBox((MonitorTileEntity) tile, player);
            }
        });
    }

    @SubscribeEvent
    public static void onChunkUnWatch(final ChunkWatchEvent.UnWatch event) {
        final ServerLevel world = event.getWorld();
        if (world.isClientSide)
            return;
        final LevelChunk chunk = (LevelChunk) world.getChunk(event.getPos().getWorldPosition());
        ImmutableMap.copyOf(chunk.getBlockEntities()).forEach((pos, tile) -> {
            if (tile instanceof MonitorTileEntity) {
                deregisterMonitorFromBox((MonitorTileEntity) tile);
            }
        });
    }

    private static SignalBoxNetworkHandler getNetworkFromSignalBox(final StateInfo info) {
        final BlockEntity tile = info.world.getBlockEntity(info.pos);
        if (tile == null || !(tile instanceof SignalBoxTileEntity))
            return null;
        final SignalBoxTileEntity signalTile = (SignalBoxTileEntity) tile;
        return signalTile.getSignalBoxGrid().getNetwork();
    }

    public static void sendTileData(final MonitorTileEntity tile,
            final List<? extends Player> list) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(tile.getPos());
        buffer.putByte(NETWORK_TILE_DATA);
        buffer.putInt(tile.getMonitorSizeX());
        buffer.putInt(tile.getMonitorSizeY());
        tile.getRenderStart().writeNetwork(buffer);
        tile.getRenderEnd().writeNetwork(buffer);
        list.forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    private static void sendInitGridUpdate(final MonitorTileEntity tile, final Player player) {
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(tile.getStateInfo());
        if (network == null)
            return;
        final SignalBoxNetworkListener listener =
                new SignalBoxNetworkListener(tile.getStateInfo(), b -> {
                    final WriteBuffer buffer = new WriteBuffer();
                    buffer.putBlockPos(tile.getPos());
                    buffer.putByte(NETWORK_UPDATE);
                    buffer.putBuffer(b);
                    if (player != null)
                        sendTo(player, buffer.getBuildedBuffer());
                    else
                        tile.getLevel().players()
                                .forEach(p -> sendTo(p, buffer.getBuildedBuffer()));
                });
        network.sendAllTo(tile, listener);
    }

    private static void sendGridUpdate(final MonitorTileEntity tile,
            final WriteBuffer networkBuffer) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(tile.getPos());
        buffer.putByte(NETWORK_UPDATE);
        buffer.putBuffer(networkBuffer);
        tile.getLevel().players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));

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
