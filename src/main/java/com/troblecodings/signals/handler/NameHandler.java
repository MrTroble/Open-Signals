package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.BufferFactory;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public final class NameHandler implements INetworkSync {

    private static final Map<NameStateInfo, String> ALL_NAMES = new HashMap<>();
    private static final Map<NameStateInfo, String> CLIENT_NAMES = new HashMap<>();
    private static final Map<ChunkAccess, List<NameStateInfo>> CURRENTLY_LOADED_CHUNKS = new HashMap<>();
    private static final Map<Level, NameHandlerFile> ALL_LEVEL_FILES = new HashMap<>();
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;
    private static ExecutorService service;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "namehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new NameHandler());
        service = Executors.newFixedThreadPool(3);
    }

    @SubscribeEvent
    public static void shutdown(final ServerStoppingEvent event) {
        service.shutdown();
        try {
            service.awaitTermination(1, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        service = Executors.newFixedThreadPool(3);
    }

    public static void setName(final NameStateInfo info, final String name) {
        if (info.world.isClientSide)
            return;
        synchronized (ALL_NAMES) {
            ALL_NAMES.put(info, name);
        }
        sendNameToClient(info, name);
        createToFile(info, name);
    }

    public static String getName(final NameStateInfo info) {
        if (info.world.isClientSide)
            return getClientName(info);
        synchronized (ALL_NAMES) {
            final String name = ALL_NAMES.get(info);
            if (name == null)
                return "";
            return name;
        }
    }

    public static String getClientName(final NameStateInfo info) {
        if (!info.world.isClientSide)
            return getName(info);
        synchronized (CLIENT_NAMES) {
            final String name = CLIENT_NAMES.get(info);
            if (name == null)
                return "";
            return name;
        }
    }

    private static void sendNameToClient(final NameStateInfo info, final String name) {
        final ByteBuffer buffer = packToBuffer(info.pos, name);
        info.world.players().forEach(player -> {
            sendTo(player, buffer);
        });
    }

    private static ByteBuffer packToBuffer(final BlockPos pos, final String name) {
        final byte[] bytes = name.getBytes();
        final BufferFactory buffer = new BufferFactory();
        buffer.putInt(pos.getX());
        buffer.putInt(pos.getY());
        buffer.putInt(pos.getZ());
        buffer.putByte((byte) name.length());
        for (final byte b : bytes) {
            buffer.putByte(b);
        }
        return buffer.build();
    }

    public static void setRemoved(final NameStateInfo info) {
        service.execute(() -> {
            synchronized (ALL_NAMES) {
                ALL_NAMES.remove(info);
            }
            NameHandlerFile file;
            synchronized (ALL_LEVEL_FILES) {
                file = ALL_LEVEL_FILES.get(info.world);
            }
            file.deleteIndex(info.pos);
        });
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final Minecraft mc = Minecraft.getInstance();
        service.execute(() -> {
            final BlockPos pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
            final int byteLength = Byte.toUnsignedInt(buf.get());
            final byte[] array = new byte[byteLength];
            for (int i = 0; i < byteLength; i++) {
                array[i] = buf.get();
            }
            synchronized (CLIENT_NAMES) {
                CLIENT_NAMES.put(new NameStateInfo(mc.level, pos), new String(array));
            }
        });
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save event) {
        if (event.getWorld().isClientSide())
            return;
        service.execute(() -> {
            Map<NameStateInfo, String> map;
            synchronized (ALL_NAMES) {
                map = ImmutableMap.copyOf(ALL_NAMES);
            }
            map.forEach(NameHandler::createToFile);
        });
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload unload) {
        if (unload.getWorld().isClientSide())
            return;
        service.execute(() -> {
            ALL_LEVEL_FILES.remove(unload.getWorld());
        });
    }

    private static void createToFile(final NameStateInfo info, final String name) {
        service.execute(() -> {
            final NameHandlerFile file = ALL_LEVEL_FILES.get(info.world);
            SignalStatePos posInFile = file.find(info.pos);
            if (posInFile == null) {
                posInFile = file.createState(info.pos, name);
                return;
            }
            synchronized (posInFile) {
                file.writeString(posInFile, name);
            }
        });
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        final ChunkAccess chunk = event.getChunk();
        final Level world = (Level) chunk.getWorldForge();
        if (world.isClientSide())
            return;
        synchronized (ALL_LEVEL_FILES) {
            if (!ALL_LEVEL_FILES.containsKey(world)) {
                ALL_LEVEL_FILES.put(world,
                        new NameHandlerFile(Paths.get("osnamefiles/"
                                + ((ServerLevel) world).getServer().getWorldData().getLevelName()
                                        .replace(":", "").replace("/", "").replace("\\", "")
                                + "/" + world.dimension().location().toString().replace(":", ""))));
            }
        }
        service.submit(() -> {
            final List<NameStateInfo> states = new ArrayList<>();
            chunk.getBlockEntitiesPos().forEach(pos -> {
                final Block block = chunk.getBlockState(pos).getBlock();
                if (!(block instanceof Signal) || !(block instanceof RedstoneIO)) {
                    return;
                }
                final NameStateInfo info = new NameStateInfo(world, pos);
                final String name = ALL_LEVEL_FILES.get(world).getString(pos);
                if (name.isEmpty())
                    return;
                synchronized (ALL_NAMES) {
                    ALL_NAMES.put(info, name);
                }
                states.add(info);
                sendNameToClient(info, name);
            });
            synchronized (CURRENTLY_LOADED_CHUNKS) {
                CURRENTLY_LOADED_CHUNKS.put(chunk, states);
            }
        });
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        final ChunkAccess chunk = event.getChunk();
        final Level level = (Level) chunk.getWorldForge();
        if (level.isClientSide())
            return;
        service.submit(() -> {
            List<NameStateInfo> states;
            synchronized (CURRENTLY_LOADED_CHUNKS) {
                states = CURRENTLY_LOADED_CHUNKS.remove(chunk);
            }
            states.forEach(stateInfo -> {
                String name;
                synchronized (ALL_NAMES) {
                    name = ALL_NAMES.remove(stateInfo);
                }
                createToFile(stateInfo, name);
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getPlayer();
        ALL_NAMES.forEach((info, name) -> {
            sendTo(player, packToBuffer(info.pos, name));
        });
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
        deserializeServer(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }
}