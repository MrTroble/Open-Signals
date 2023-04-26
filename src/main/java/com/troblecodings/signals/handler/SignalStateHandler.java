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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.signalbox.debug.DebugSignalStateFile;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

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
    private static final Map<SignalStateInfo, Integer> SIGNAL_COUNTER = new HashMap<>();
    private static final Map<SignalStateInfo, List<SignalStateListener>> ALL_LISTENERS = new HashMap<>();
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
        service = Executors.newFixedThreadPool(4);
    }

    @SubscribeEvent
    public static void shutdown(final ServerStoppingEvent event) {
        service.shutdown();
        try {
            service.awaitTermination(1, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        service = Executors.newFixedThreadPool(4);
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
        loadSignal(info);
    }

    public static void addListener(final SignalStateInfo info, final SignalStateListener listener) {
        synchronized (ALL_LISTENERS) {
            final List<SignalStateListener> listeners = ALL_LISTENERS.computeIfAbsent(info,
                    _u -> new ArrayList<>());
            listeners.add(listener);
        }
    }

    public static void removeListener(final SignalStateInfo info,
            final SignalStateListener listener) {
        final List<SignalStateListener> listeners;
        synchronized (ALL_LISTENERS) {
            listeners = ALL_LISTENERS.get(info);
        }
        if (listeners == null)
            return;
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            synchronized (ALL_LISTENERS) {
                ALL_LISTENERS.remove(info);
            }
        }
    }

    private static void updateListeners(final SignalStateInfo info, final boolean removed) {
        final List<SignalStateListener> listeners;
        synchronized (ALL_LISTENERS) {
            listeners = ALL_LISTENERS.get(info);
        }
        if (listeners == null)
            return;
        listeners.forEach(listener -> listener.update(info, removed));
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
        if (states == null)
            return;
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
                updateListeners(info, false);
                return;
            }
        }
        createToFile(info, states);
        sendPropertiesToClient(info, states);
        updateListeners(info, false);
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
                OpenSignalsMain.getLogger()
                        .warn("Position [" + stateInfo + "] not found on client!");
                return map;
            } else {
                OpenSignalsMain.getLogger()
                        .warn("Position [" + stateInfo + "] not found in file, recovering!");
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
                map.put(property,
                        NameHandler.getName(new NameStateInfo(stateInfo.world, stateInfo.pos)));
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
                        new DebugSignalStateFile(Paths.get("osfiles/signalfiles/"
                                + ((ServerLevel) world).getServer().getWorldData().getLevelName()
                                        .replace(":", "").replace("/", "").replace("\\", "")
                                + "/" + world.dimension().location().toString().replace(":", ""))));
            }
        }
        service.submit(() -> {
            final List<SignalStateInfo> states = new ArrayList<>();
            chunk.getBlockEntitiesPos().forEach(pos -> {
                final Block block = chunk.getBlockState(pos).getBlock();
                if (block instanceof Signal) {
                    states.add(new SignalStateInfo(world, pos, (Signal) block));
                } else if (block instanceof SignalBox) {
                    SignalBoxHandler.loadSignals(new PosIdentifier(pos, world));
                }

            });
            loadSignals(states);
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
            chunk.getBlockEntitiesPos().forEach(pos -> {
                final Block block = chunk.getBlockState(pos).getBlock();
                if (block instanceof SignalBox) {
                    SignalBoxHandler.unloadSignals(new PosIdentifier(pos, level));
                } else if (block instanceof SignalController) {
                    ((SignalControllerTileEntity) level.getBlockEntity(pos)).unloadSignal();
                }
            });
            List<SignalStateInfo> states;
            synchronized (CURRENTLY_LOADED_CHUNKS) {
                states = CURRENTLY_LOADED_CHUNKS.remove(chunk);
            }
            unloadSignals(states);
        });
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save save) {
        if (save.getWorld().isClientSide())
            return;
        service.execute(() -> {
            final Map<SignalStateInfo, Map<SEProperty, String>> maps;
            synchronized (CURRENTLY_LOADED_STATES) {
                maps = ImmutableMap.copyOf(CURRENTLY_LOADED_STATES);
            }
            maps.forEach(SignalStateHandler::createToFile);
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
            synchronized (SIGNAL_COUNTER) {
                SIGNAL_COUNTER.remove(info);
            }
            sendRemoved(info);
            updateListeners(info, true);
            ALL_LISTENERS.remove(info);
        });
    }

    private static void sendRemoved(final SignalStateInfo info) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(info.pos);
        buffer.putByte((byte) 255);
        info.world.players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    public static ByteBuffer packToByteBuffer(final SignalStateInfo stateInfo,
            final Map<SEProperty, String> properties) {
        if (properties.size() > 254) {
            throw new IllegalStateException("Too many SEProperties!");
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(stateInfo.pos);
        buffer.putByte((byte) properties.size());
        properties.forEach((property, value) -> {
            if (property.equals(Signal.CUSTOMNAME)) {
                buffer.putByte((byte) stateInfo.signal.getIDFromProperty(property));
                buffer.putByte((byte) (value.isEmpty() ? 0 : 1));
                NameHandler.setName(new NameStateInfo(stateInfo.world, stateInfo.pos), value);
                return;
            }
            buffer.putByte((byte) stateInfo.signal.getIDFromProperty(property));
            buffer.putByte((byte) property.getParent().getIDFromValue(value));
        });
        return buffer.build();
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

    public static void loadSignal(final SignalStateInfo info) {
        loadSignals(ImmutableList.of(info));
    }

    public static void loadSignals(final List<SignalStateInfo> signals) {
        service.execute(() -> {
            signals.forEach(info -> {
                synchronized (SIGNAL_COUNTER) {
                    Integer count = SIGNAL_COUNTER.get(info);
                    if (count != null && count > 0) {
                        SIGNAL_COUNTER.put(info, ++count);
                        return;
                    }
                    SIGNAL_COUNTER.put(info, 1);
                }
                Map<SEProperty, String> properties;
                synchronized (CURRENTLY_LOADED_STATES) {
                    properties = CURRENTLY_LOADED_STATES.get(info);
                }
                if (properties == null) {
                    properties = readAndSerialize(info);
                    synchronized (CURRENTLY_LOADED_STATES) {
                        CURRENTLY_LOADED_STATES.put(info, properties);
                    }
                }
                sendPropertiesToClient(info, properties);
            });
        });
    }

    public static void unloadSignal(final SignalStateInfo info) {
        unloadSignals(ImmutableList.of(info));
    }

    public static void unloadSignals(final List<SignalStateInfo> signals) {
        service.execute(() -> {
            if (signals == null)
                return;
            signals.forEach(info -> {
                if (info.signal != null && info.pos != null && info.world != null) {
                    synchronized (SIGNAL_COUNTER) {
                        int count = SIGNAL_COUNTER.get(info);
                        if (count > 1) {
                            SIGNAL_COUNTER.put(info, --count);
                            return;
                        }
                        SIGNAL_COUNTER.remove(info);
                    }
                    synchronized (CURRENTLY_LOADED_STATES) {
                        createToFile(info, CURRENTLY_LOADED_STATES.remove(info));
                    }
                }
            });
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
}
