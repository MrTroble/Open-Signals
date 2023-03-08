package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.debug.DebugSignalStateFile;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public final class SignalStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES = new HashMap<>();
    private static final Map<ChunkAccess, List<SignalStateInfo>> CURRENTLY_LOADED_CHUNKS = new HashMap<>();
    private static final Map<Level, SignalStateFile> ALL_LEVEL_FILES = new HashMap<>();
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;
    private static ExecutorService service;

    private SignalStateHandler() {
    }

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "signalstatehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new SignalStateHandler());
        MinecraftForge.EVENT_BUS.register(SignalStateHandler.class);
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

    public static void add(final Object object) {
        channel.registerObject(object);
    }

    public static void createStates(final SignalStateInfo info,
            final Map<SEProperty, String> states) {
        if (info.world.isClientSide)
            return;
        synchronized (CURRENTLY_LOADED_STATES) {
            CURRENTLY_LOADED_STATES.put(info, ImmutableMap.copyOf(states));
        }
        createToFile(info, states);
        sendPropertiesToClient(info, states);
    }

    private static void statesToBuffer(final Signal signal, final Map<SEProperty, String> states,
            final byte[] readData) {
        states.forEach((property, string) -> {
            readData[signal.getIDFromProperty(
                    property)] = (byte) (property.getParent().getIDFromValue(string) + 1);
        });
    }

    private static void createToFile(final SignalStateInfo info,
            final Map<SEProperty, String> states) {
        final SignalStateFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(info.world);
            if (file == null) {
                return;
            }
        }
        service.submit(() -> {
            SignalStatePos pos = file.find(info.pos);
            if (pos == null) {
                pos = file.create(info.pos);
            }
            synchronized (file) {
                final ByteBuffer buffer = file.read(pos);
                statesToBuffer(info.signal, states, buffer.array());
                file.write(pos, buffer);
            }
        });
    }

    public static void setStates(final SignalStateInfo info, final Map<SEProperty, String> states) {
        if (info.world.isClientSide || states == null || states.isEmpty()) {
            return;
        }
        synchronized (CURRENTLY_LOADED_STATES) {
            if (CURRENTLY_LOADED_STATES.containsKey(info)) {
                final Map<SEProperty, String> oldStates = new HashMap<>(getStates(info));
                oldStates.putAll(states);
                CURRENTLY_LOADED_STATES.put(info, ImmutableMap.copyOf(oldStates));
                sendPropertiesToClient(info, states);
                return;
            }
        }
        createToFile(info, states);
    }

    public static Map<SEProperty, String> getStates(final SignalStateInfo info) {
        final Map<SEProperty, String> states;
        synchronized (CURRENTLY_LOADED_STATES) {
            final Map<SEProperty, String> stateVolitile = CURRENTLY_LOADED_STATES.get(info);
            states = stateVolitile == null ? null : ImmutableMap.copyOf(stateVolitile);
        }
        if (states != null) {
            return states;
        } else {
            if (info.world.isClientSide) {
                return Map.of();
            }
            return readAndSerialize(info);
        }
    }

    public static void setState(final SignalStateInfo info, final SEProperty property,
            final String value) {
        final Map<SEProperty, String> map = new HashMap<>();
        synchronized (CURRENTLY_LOADED_STATES) {
            final Map<SEProperty, String> savedProperties = CURRENTLY_LOADED_STATES.get(info);
            map.putAll(savedProperties == null ? new HashMap<>() : savedProperties);
        }
        map.put(property, value);
        setStates(info, map);
    }

    public static Optional<String> getState(final SignalStateInfo info, final SEProperty property) {
        final Map<SEProperty, String> properties = getStates(info);
        return Optional.ofNullable(properties.get(property));
    }

    private static Map<SEProperty, String> readAndSerialize(final SignalStateInfo stateInfo) {
        final Map<SEProperty, String> map = new HashMap<>();
        SignalStateFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(stateInfo.world);
        }
        SignalStatePos pos = file.find(stateInfo.pos);
        if (pos == null) {
            if (stateInfo.world.isClientSide) {
                OpenSignalsMain.getLogger().warn("Position not found on client!");
                return map;
            } else {
                OpenSignalsMain.getLogger().warn("Position not found in file, recovering!");
                pos = file.create(stateInfo.pos);
            }
        }
        final ByteBuffer buffer = file.read(pos);
        final List<SEProperty> properties = stateInfo.signal.getProperties();
        final byte[] byteArray = buffer.array();
        for (int i = 0; i < properties.size(); i++) {
            final SEProperty property = properties.get(i);
            final int typeID = Byte.toUnsignedInt(byteArray[i]);
            if (typeID <= 0)
                continue;
            if (property.equals(Signal.CUSTOMNAME)) {
                map.put(property, NameHandler.getName(stateInfo.pos));
            }
            final String value = property.getObjFromID(typeID - 1);
            map.put(property, value);
        }
        return map;
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
                        new DebugSignalStateFile(Paths.get("ossignalfiles/"
                                + ((ServerLevel) world).getServer().getWorldData().getLevelName()
                                        .replace(":", "").replace("/", "").replace("\\", "")
                                + "/" + world.dimension().location().toString().replace(":", ""))));
            }
        }
        service.submit(() -> {
            final List<SignalStateInfo> states = new ArrayList<>();
            chunk.getBlockEntitiesPos().forEach(pos -> {
                final Block block = chunk.getBlockState(pos).getBlock();
                if (!(block instanceof Signal)) {
                    return;
                }
                final SignalStateInfo stateInfo = new SignalStateInfo(world, pos);
                final Map<SEProperty, String> map = readAndSerialize(stateInfo);
                synchronized (CURRENTLY_LOADED_STATES) {
                    CURRENTLY_LOADED_STATES.put(stateInfo, map);
                }
                states.add(stateInfo);
                sendPropertiesToClient(stateInfo, map);
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
        SignalStateFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(level);
        }
        service.submit(() -> {
            List<SignalStateInfo> states;
            synchronized (CURRENTLY_LOADED_CHUNKS) {
                states = CURRENTLY_LOADED_CHUNKS.remove(chunk);
            }
            states.forEach(stateInfo -> {
                Map<SEProperty, String> properties;
                synchronized (CURRENTLY_LOADED_STATES) {
                    properties = CURRENTLY_LOADED_STATES.remove(stateInfo);
                }
                final SignalStatePos pos = file.find(stateInfo.pos);
                final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
                statesToBuffer(stateInfo.signal, properties, buffer.array());
                file.write(pos, buffer);
                unRenderClients(stateInfo);
            });
        });
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save save) {
        service.execute(() -> {
            final Map<SignalStateInfo, Map<SEProperty, String>> maps;
            synchronized (CURRENTLY_LOADED_STATES) {
                maps = ImmutableMap.copyOf(CURRENTLY_LOADED_STATES);
            }
            maps.forEach(SignalStateHandler::createToFile);
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getPlayer();
        Map<SignalStateInfo, Map<SEProperty, String>> map;
        synchronized (CURRENTLY_LOADED_STATES) {
            map = ImmutableMap.copyOf(CURRENTLY_LOADED_STATES);
        }
        map.forEach((state, properites) -> {
            final ByteBuffer buffer = packToByteBuffer(state, properites);
            sendTo(player, buffer);
        });
    }

    public static void setRemoved(final SignalStateInfo info) {
        synchronized (CURRENTLY_LOADED_STATES) {
            CURRENTLY_LOADED_STATES.remove(info);
        }
        service.execute(() -> {
            SignalStateFile file;
            synchronized (ALL_LEVEL_FILES) {
                file = ALL_LEVEL_FILES.get(info.world);
            }
            file.deleteIndex(info.pos);
        });
    }

    private static void unRenderClients(final SignalStateInfo stateInfo) {
        final ByteBuffer buffer = ByteBuffer.allocate(13);
        buffer.putInt(stateInfo.pos.getX());
        buffer.putInt(stateInfo.pos.getY());
        buffer.putInt(stateInfo.pos.getZ());
        buffer.put((byte) 255);
        stateInfo.world.players().forEach(player -> {
            sendTo(player, buffer);
        });
    }

    public static ByteBuffer packToByteBuffer(final SignalStateInfo stateInfo,
            final Map<SEProperty, String> properties) {
        final ByteBuffer buffer = ByteBuffer.allocate(13 + properties.size() * 2);
        buffer.putInt(stateInfo.pos.getX());
        buffer.putInt(stateInfo.pos.getY());
        buffer.putInt(stateInfo.pos.getZ());
        if (properties.size() > 254) {
            throw new IllegalStateException("Too many SEProperties!");
        }
        buffer.put((byte) properties.size());
        properties.forEach((property, value) -> {
            if (property.equals(Signal.CUSTOMNAME)) {
                buffer.put((byte) stateInfo.signal.getIDFromProperty(property));
                buffer.put((byte) (value.isEmpty() ? 0 : 1));
                NameHandler.setName(stateInfo.world, stateInfo.pos, value);
                return;
            }
            buffer.put((byte) stateInfo.signal.getIDFromProperty(property));
            buffer.put((byte) property.getParent().getIDFromValue(value));
        });
        return buffer;
    }

    private static void sendPropertiesToClient(final SignalStateInfo stateInfo,
            final Map<SEProperty, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        final ByteBuffer buffer = packToByteBuffer(stateInfo, properties);
        stateInfo.world.players().forEach(player -> {
            sendTo(player, buffer);
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
}
