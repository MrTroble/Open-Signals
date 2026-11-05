package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler.SignalBoxNetworkListener;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public final class MonitorNetworkHandler implements IChunkLoadable {

    public static final byte NETWORK_TILE_DATA = 0;
    public static final byte NETWORK_UPDATE = 1;
    public static final byte PATHWAY_ADD_SAVER = 2;
    public static final byte PATHWAY_RESPONSE = 3;

    private static final Map<StateInfo, SignalBoxNetworkListener> LISTENERS = new HashMap<>();

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
        LISTENERS.put(monitorInfo, listener);
        sendInitGridUpdate(tile, player);
    }

    public static void deregisterMonitorFromBox(final MonitorTileEntity tile) {
        final StateInfo info = tile.getStateInfo();
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(info);
        if (network == null)
            return;
        final StateInfo monitorInfo = new StateInfo(tile.getLevel(), tile.getBlockPos());
        network.removeListener(LISTENERS.remove(monitorInfo));
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
        buffer.putBlockPos(tile.getBlockPos());
        buffer.putByte(NETWORK_TILE_DATA);
        buffer.putBlockPos(tile.getLinkedSignalBox());
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
                    buffer.putBlockPos(tile.getBlockPos());
                    buffer.putByte(NETWORK_UPDATE);
                    buffer.putBuffer(b);
                    if (player != null) {
                        sendTo(player, buffer.getBuildedBuffer());
                    } else {
                        tile.getLevel().players()
                                .forEach(p -> sendTo(p, buffer.getBuildedBuffer()));
                    }
                });
        network.sendAllTo(tile, listener);
    }

    private static void sendGridUpdate(final MonitorTileEntity tile,
            final WriteBuffer networkBuffer) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(tile.getBlockPos());
        buffer.putByte(NETWORK_UPDATE);
        buffer.putBuffer(networkBuffer);
        tile.getLevel().players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));

    }

    @OnlyIn(Dist.CLIENT)
    public static void sendPathwayRequest(final Level world, final BlockPos signalBoxPos,
            final Point p1, final Point p2, final PathType type) {
        if (!world.isClientSide)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(signalBoxPos);
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        buffer.putEnumValue(type);

        sendTo(Minecraft.getInstance().player, buffer.getBuildedBuffer());
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

    @SubscribeEvent
    public void clientEvent(final ClientCustomPayloadEvent event) {
        final ServerPlayer player = event.getSource().get().getSender();
        final Level world = player.level;
        if (!(world instanceof ServerLevel))
            return;
        final ReadBuffer buffer = new ReadBuffer(event.getPayload().nioBuffer());
        final BlockPos signalBoxPos = buffer.getBlockPos();
        final Point p1 = Point.of(buffer);
        final Point p2 = Point.of(buffer);
        final PathType type = buffer.getEnumValue(PathType.class);

        loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) world, signalBoxPos,
                (tile, chunk) -> {
                    final SignalBoxGrid grid = tile.getSignalBoxGrid();
                    final PathwayRequestResult request = grid.requestWay(p1, p2, type);
                    if (!request.wasSuccesfull()) {
                        final SignalBoxNode endNode = grid.getNode(p2);
                        if (request.canBeAddedToSaver(type) && !endNode.containsOutConnection()
                                && grid.addNextPathway(p1, p2, type)) {
                            sendResponseToClient(request, player, PATHWAY_ADD_SAVER);
                            return;
                        }
                        sendResponseToClient(request, player, PATHWAY_RESPONSE);
                    }
                });
    }

    private static void sendResponseToClient(final PathwayRequestResult result,
            final ServerPlayer player, final byte mode) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(BlockPos.ZERO);
        buffer.putByte(mode);
        buffer.putEnumValue(result.getMode());
        sendTo(player, buffer.getBuildedBuffer());
    }

}
