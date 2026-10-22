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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class MonitorNetworkHandler implements IChunkLoadable {

    public static final byte NETWORK_TILE_DATA = 0;
    public static final byte NETWORK_UPDATE = 1;
    public static final byte PATHWAY_ADD_SAVER = 2;
    public static final byte PATHWAY_RESPONSE = 3;

    private static final Map<StateInfo, SignalBoxNetworkListener> LISTENERS = new HashMap<>();

    private static final String CHANNELNAME = "monitor_net";
    private static FMLEventChannel channel;
    private static MonitorNetworkHandler instance;

    public MonitorNetworkHandler() {
        instance = this;
    }

    public static void init() {
        new MonitorNetworkHandler();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELNAME);
        channel.register(instance);
    }

    public static void registerToNetworkChannel(final Object obj) {
        channel.register(obj);
    }

    public static void registerMonitorToBox(final MonitorTileEntity tile) {
        registerMonitorToBox(tile, null);
    }

    public static void registerMonitorToBox(final MonitorTileEntity tile,
            final @Nullable EntityPlayer player) {
        final StateInfo signalBoxInfo = tile.getStateInfo();
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(signalBoxInfo);
        if (network == null)
            return;
        final StateInfo monitorInfo = new StateInfo(tile.getWorld(), tile.getPos());
        final SignalBoxNetworkListener listener =
                new SignalBoxNetworkListener(monitorInfo, b -> sendGridUpdate(tile, b));
        network.addListener(listener);
        LISTENERS.put(monitorInfo, listener);
        sendTileData(tile, monitorInfo.world.playerEntities);
        sendInitGridUpdate(tile, player);
    }

    public static void deregisterMonitorFromBox(final MonitorTileEntity tile) {
        final StateInfo info = tile.getStateInfo();
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(info);
        if (network == null)
            return;
        final StateInfo monitorInfo = new StateInfo(tile.getWorld(), tile.getPos());
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
        final Chunk chunk = event.getChunkInstance();
        final World world = chunk.getWorld();
        if (world.isRemote)
            return;
        final EntityPlayer player = event.getPlayer();
        ImmutableMap.copyOf(chunk.getTileEntityMap()).forEach((pos, tile) -> {
            if (tile instanceof MonitorTileEntity) {
                sendTileData((MonitorTileEntity) tile, ImmutableList.of(player));
                registerMonitorToBox((MonitorTileEntity) tile, player);
            }
        });
    }

    @SubscribeEvent
    public static void onChunkUnWatch(final ChunkWatchEvent.UnWatch event) {
        final Chunk chunk = event.getChunkInstance();
        final World world = chunk.getWorld();
        if (world.isRemote)
            return;
        ImmutableMap.copyOf(chunk.getTileEntityMap()).forEach((pos, tile) -> {
            if (tile instanceof MonitorTileEntity) {
                deregisterMonitorFromBox((MonitorTileEntity) tile);
            }
        });
    }

    private static SignalBoxNetworkHandler getNetworkFromSignalBox(final StateInfo info) {
        final TileEntity tile = info.world.getTileEntity(info.pos);
        if (tile == null || !(tile instanceof SignalBoxTileEntity))
            return null;
        final SignalBoxTileEntity signalTile = (SignalBoxTileEntity) tile;
        return signalTile.getSignalBoxGrid().getNetwork();
    }

    public static void sendTileData(final MonitorTileEntity tile, final List<EntityPlayer> list) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(tile.getPos());
        buffer.putByte(NETWORK_TILE_DATA);
        buffer.putBlockPos(tile.getLinkedSignalBox());
        buffer.putInt(tile.getMonitorSizeX());
        buffer.putInt(tile.getMonitorSizeY());
        tile.getRenderStart().writeNetwork(buffer);
        tile.getRenderEnd().writeNetwork(buffer);
        list.forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    private static void sendInitGridUpdate(final MonitorTileEntity tile,
            final EntityPlayer player) {
        final SignalBoxNetworkHandler network = getNetworkFromSignalBox(tile.getStateInfo());
        if (network == null)
            return;
        final SignalBoxNetworkListener listener =
                new SignalBoxNetworkListener(tile.getStateInfo(), b -> {
                    final WriteBuffer buffer = new WriteBuffer();
                    buffer.putBlockPos(tile.getPos());
                    buffer.putByte(NETWORK_UPDATE);
                    buffer.putBuffer(b);
                    if (player != null) {
                        sendTo(player, buffer.getBuildedBuffer());
                    } else {
                        tile.getWorld().playerEntities
                                .forEach(p -> sendTo(p, buffer.getBuildedBuffer()));
                    }
                });
        network.sendAllTo(tile, listener);
    }

    private static void sendGridUpdate(final MonitorTileEntity tile,
            final WriteBuffer networkBuffer) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(tile.getPos());
        buffer.putByte(NETWORK_UPDATE);
        buffer.putBuffer(networkBuffer);
        tile.getWorld().playerEntities.forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    @SideOnly(Side.CLIENT)
    public static void sendPathwayRequest(final World world, final BlockPos signalBoxPos,
            final Point p1, final Point p2, final PathType type) {
        if (!world.isRemote)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(signalBoxPos);
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        buffer.putEnumValue(type);

        sendTo(Minecraft.getMinecraft().player, buffer.getBuildedBuffer());
    }

    public static void sendTo(final EntityPlayer player, final ByteBuffer buf) {
        final PacketBuffer buffer =
                new PacketBuffer(Unpooled.copiedBuffer((ByteBuffer) buf.position(0)));
        if (player instanceof EntityPlayerMP) {
            final EntityPlayerMP server = (EntityPlayerMP) player;
            channel.sendTo(new FMLProxyPacket(buffer, CHANNELNAME), server);
        } else {
            channel.sendToServer(new FMLProxyPacket(new CPacketCustomPayload(CHANNELNAME, buffer)));
        }
    }

    @SubscribeEvent
    public void clientEvent(final ServerCustomPacketEvent event) {
        final NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
        handler.player.mcServer.addScheduledTask(() -> {
            final World world = handler.player.getEntityWorld();
            final ReadBuffer buffer = new ReadBuffer(event.getPacket().payload().nioBuffer());
            final BlockPos signalBoxPos = buffer.getBlockPos();
            final Point p1 = Point.of(buffer);
            final Point p2 = Point.of(buffer);
            final PathType type = buffer.getEnumValue(PathType.class);

            loadChunkAndGetTile(SignalBoxTileEntity.class, world, signalBoxPos, (tile, chunk) -> {
                final SignalBoxGrid grid = tile.getSignalBoxGrid();
                final PathwayRequestResult request = grid.requestWay(p1, p2, type);
                if (!request.wasSuccesfull()) {
                    final SignalBoxNode endNode = grid.getNode(p2);
                    if (request.canBeAddedToSaver(type) && !endNode.containsOutConnection()
                            && grid.addNextPathway(p1, p2, type)) {
                        sendResponseToClient(request, handler.player, PATHWAY_ADD_SAVER);
                        return;
                    }
                    sendResponseToClient(request, handler.player, PATHWAY_RESPONSE);
                }
            });
        });
    }

    private static void sendResponseToClient(final PathwayRequestResult result,
            final EntityPlayerMP player, final byte mode) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(BlockPos.ORIGIN);
        buffer.putByte(mode);
        buffer.putEnumValue(result.getMode());
        sendTo(player, buffer.getBuildedBuffer());
    }

}
