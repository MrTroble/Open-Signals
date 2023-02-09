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

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.debug.DebugSignalStateFile;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public final class SignalStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> currentlyLoadedStates = new HashMap<>();
    private static final Map<ChunkAccess, List<SignalStateInfo>> currentlyLoadedChunks = new HashMap<>();
    private static final Map<Level, SignalStateFile> allLevelFiles = new HashMap<>();
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "signalstatehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new SignalStateHandler());
    }

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(3);

    public static void createStates(final SignalStateInfo info,
            final Map<SEProperty, String> states) {
        if (info.world.isClientSide)
            return;
        synchronized (currentlyLoadedStates) {
            currentlyLoadedStates.put(info, ImmutableMap.copyOf(states));
        }
        createToFile(info, states);
        sendPropertiesToClient(info, states);
    }

    private static void statesToBuffer(final Signal signal, final Map<SEProperty, String> states,
            byte[] readData) {
        states.forEach((property, string) -> {
            readData[signal.getIDFromProperty(
                    property)] = (byte) (property.getParent().getIDFromValue(string) + 1);
        });
    }

    private static void createToFile(final SignalStateInfo info,
            final Map<SEProperty, String> states) {
        final SignalStateFile file;
        synchronized (allLevelFiles) {
            file = allLevelFiles.get(info.world);
            if (file == null) {
                return;
            }
        }
        SERVICE.submit(() -> {
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
        synchronized (currentlyLoadedStates) {
            if (currentlyLoadedStates.containsKey(info)) {
                currentlyLoadedStates.put(info, ImmutableMap.copyOf(states));
                sendPropertiesToClient(info, states);
                return;
            }
        }
        createToFile(info, states);
    }

    public static Map<SEProperty, String> getStates(final SignalStateInfo info) {
        final Map<SEProperty, String> states;
        synchronized (currentlyLoadedStates) {
            final Map<SEProperty, String> stateVolitile = currentlyLoadedStates.get(info);
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
        Map<SEProperty, String> map = new HashMap<>();
        synchronized (currentlyLoadedStates) {
            final Map<SEProperty, String> savedProperties = currentlyLoadedStates.get(info);
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
        Map<SEProperty, String> map = new HashMap<>();
        SignalStateFile file;
        synchronized (allLevelFiles) {
            file = allLevelFiles.get(stateInfo.world);
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
        byte[] byteArray = buffer.array();
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
        synchronized (allLevelFiles) {
            if (!allLevelFiles.containsKey(world)) {
                allLevelFiles.put(world,
                        new DebugSignalStateFile(Paths.get("ossignalfiles/"
                                + ((ServerLevel) world).getServer().getWorldData().getLevelName()
                                        .replace(":", "").replace("/", "").replace("\\", "")
                                + "/" + world.dimension().location().toString().replace(":", ""))));
            }
        }
        SERVICE.submit(() -> {
            final List<SignalStateInfo> states = new ArrayList<>();
            chunk.getBlockEntitiesPos().forEach(pos -> {
                final Block block = chunk.getBlockState(pos).getBlock();
                if (!(block instanceof Signal)) {
                    return;
                }
                final SignalStateInfo stateInfo = new SignalStateInfo(world, pos);
                final Map<SEProperty, String> map = readAndSerialize(stateInfo);
                synchronized (currentlyLoadedStates) {
                    currentlyLoadedStates.put(stateInfo, map);
                }
                states.add(stateInfo);
                sendPropertiesToClient(stateInfo, map);
            });
            synchronized (currentlyLoadedChunks) {
                currentlyLoadedChunks.put(chunk, states);
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
        synchronized (allLevelFiles) {
            file = allLevelFiles.get(level);
        }
        SERVICE.submit(() -> {
            List<SignalStateInfo> states;
            synchronized (currentlyLoadedChunks) {
                states = currentlyLoadedChunks.remove(chunk);
            }
            states.forEach(stateInfo -> {
                Map<SEProperty, String> properties;
                synchronized (currentlyLoadedStates) {
                    properties = currentlyLoadedStates.remove(stateInfo);
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
        SERVICE.execute(() -> {
            final Map<SignalStateInfo, Map<SEProperty, String>> maps;
            synchronized (currentlyLoadedStates) {
                maps = ImmutableMap.copyOf(currentlyLoadedStates);
            }
            maps.forEach(SignalStateHandler::createToFile);
            if (save.getWorld().isClientSide()) {
                currentlyLoadedStatesClient.clear();
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final Player player = event.getPlayer();
        Map<SignalStateInfo, Map<SEProperty, String>> map;
        synchronized (currentlyLoadedStates) {
            map = ImmutableMap.copyOf(currentlyLoadedStates);
        }
        map.forEach((state, properites) -> {
            final ByteBuffer buffer = packToByteBuffer(state, properites);
            sendTo(player, buffer);
        });
    }

    public static void setRemoved(final SignalStateInfo info) {
        synchronized (currentlyLoadedStates) {
            currentlyLoadedStates.remove(info);
        }
        SERVICE.execute(() -> {
            // TODO remove from Files
        });
    }

    private static void unRenderClients(final SignalStateInfo stateInfo) {
        final ByteBuffer buffer = ByteBuffer.allocate(13);
        buffer.putInt(stateInfo.pos.getX());
        buffer.putInt(stateInfo.pos.getY());
        buffer.putInt(stateInfo.pos.getZ());
        buffer.put((byte) 255);
        stateInfo.world.players().forEach(player -> {
            if (checkInRange(player.blockPosition(), stateInfo.pos)) {
                sendTo(player, buffer);
            }
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
            if (checkInRange(player.blockPosition(), stateInfo.pos)) {
                sendTo(player, buffer);
            }
        });
    }

    /**
     * This is the distance in blocks in which signals get rendered
     */
    private static final int RENDER_DISTANCE = 512;

    private static boolean checkInRange(final BlockPos playerPos, final BlockPos signalPos) {
        return true;
    }

    private static final Map<SignalStateInfo, Map<SEProperty, String>> currentlyLoadedStatesClient = new HashMap<>();

    public static final Map<SEProperty, String> getClientStates(final SignalStateInfo info) {
        return currentlyLoadedStatesClient.computeIfAbsent(info, _u -> new HashMap<>());
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BlockPos signalPos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        final int propertiesSize = Byte.toUnsignedInt(buf.get());
        if (propertiesSize == 255) {
            // TODO inform client to unrender
            return;
        }
        final int[] propertyIDs = new int[propertiesSize];
        final int[] valueIDs = new int[propertiesSize];
        for (int i = 0; i < propertiesSize; i++) {
            propertyIDs[i] = Byte.toUnsignedInt(buf.get());
            valueIDs[i] = Byte.toUnsignedInt(buf.get());
        }
        SERVICE.execute(() -> {
            final Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if(level == null)
                return;
            BlockEntity entity;
            while ((entity = level.getBlockEntity(signalPos)) == null)
                continue;
            final SignalStateInfo stateInfo = new SignalStateInfo(level, signalPos);
            final List<SEProperty> signalProperties = stateInfo.signal.getProperties();
            synchronized (currentlyLoadedStatesClient) {
                final Map<SEProperty, String> properties = currentlyLoadedStatesClient
                        .computeIfAbsent(stateInfo, _u -> new HashMap<>());
                for (int i = 0; i < propertiesSize; i++) {
                    final SEProperty property = signalProperties.get(propertyIDs[i]);
                    final String value = property.getObjFromID(valueIDs[i]);
                    properties.put(property, value);
                }
            }
            entity.requestModelDataUpdate();
            mc.levelRenderer.blockChanged(null, signalPos, null, null, 8);
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
