package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;
import net.minecraftforge.network.event.EventNetworkChannel;

public final class NameHandler implements INetworkSync {

    private static final Map<BlockPos, String> allNames = new HashMap<>();
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "namehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new NameHandler());
    }

    public static void setName(final Level world, final BlockPos pos, final String name) {
        if (world.isClientSide || name.isEmpty())
            return;
        synchronized (allNames) {
            allNames.put(pos, name);
        }
        sendNameToClient(world, pos, name);
        // TODO add system to save into files
    }

    public static String getName(final BlockPos pos) {
        synchronized (allNames) {
            return allNames.get(pos);
        }
    }

    private static void sendNameToClient(final Level world, final BlockPos pos, final String name) {
        final ByteBuffer buffer = packToBuffer(pos, name);
        world.players().forEach(player -> {
            sendTo(player, buffer);
        });
    }

    private static ByteBuffer packToBuffer(final BlockPos pos, final String name) {
        final byte[] bytes = name.getBytes();
        final ByteBuffer buffer = ByteBuffer.allocate(13 + bytes.length);
        buffer.putInt(pos.getX());
        buffer.putInt(pos.getY());
        buffer.putInt(pos.getZ());
        buffer.put((byte) name.length());
        for (final byte b : bytes) {
            buffer.put(b);
        }
        return buffer;
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BlockPos pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        final int byteLength = Byte.toUnsignedInt(buf.get());
        final byte[] array = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            array[i] = buf.get();
        }
        synchronized (allNames) {
            allNames.put(pos, new String(array));
        }
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save event) {
        Map<BlockPos, String> map;
        synchronized (allNames) {
            map = ImmutableMap.copyOf(allNames);
        }
        map.forEach((pos, name) -> {
            // TODO write into files
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getPlayer();
        allNames.forEach((pos, name) -> {
            sendTo(player, packToBuffer(pos, name));
        });

    }

    public static void sendTo(final Player player, final ByteBuffer buf) {
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
        deserializeServer(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }
}