package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.WriteBuffer;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

public final class SignalStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES = new HashMap<>();
    private static final Map<IChunk, List<SignalStateInfo>> CURRENTLY_LOADED_CHUNKS = new HashMap<>();
    private static final Map<World, SignalStateFile> ALL_LEVEL_FILES = new HashMap<>();
    private static final Map<SignalStateInfo, Integer> SIGNAL_COUNTER = new HashMap<>();
    private static final Map<SignalStateInfo, List<SignalStateListener>> ALL_LISTENERS = new HashMap<>();
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;

    private SignalStateHandler() {
    }

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "signalstatehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new SignalStateHandler());
        MinecraftForge.EVENT_BUS.register(SignalStateHandler.class);
    }

    public static void add(final Object object) {
        channel.registerObject(object);
    }

    public static void createStates(final SignalStateInfo info,
            final Map<SEProperty, String> states) {
        if (info.world.isClientSide)
            return;
        new Thread(() -> {
            synchronized (CURRENTLY_LOADED_STATES) {
                CURRENTLY_LOADED_STATES.put(info, ImmutableMap.copyOf(states));
            }
            synchronized (CURRENTLY_LOADED_CHUNKS) {
                final List<SignalStateInfo> allSignals = CURRENTLY_LOADED_CHUNKS
                        .get(info.world.getChunk(info.pos));
                if (!allSignals.contains(info))
                    allSignals.add(info);
            }
            createToFile(info, states);
            loadSignal(info);
        }, "OSSignalStateHandler:createStates").start();
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
        SignalStatePos pos = file.find(info.pos);
        if (pos == null) {
            pos = file.create(info.pos);
        }
        synchronized (file) {
            final ByteBuffer buffer = file.read(pos);
            statesToBuffer(info.signal, states, buffer.array());
            file.write(pos, buffer);
        }
    }

    public static void setStates(final SignalStateInfo info, final Map<SEProperty, String> states) {
        if (info.world.isClientSide || states == null || states.isEmpty()) {
            return;
        }
        final AtomicBoolean contains = new AtomicBoolean(false);
        synchronized (CURRENTLY_LOADED_STATES) {
            if (CURRENTLY_LOADED_STATES.containsKey(info)) {
                contains.set(true);
                final Map<SEProperty, String> oldStates = new HashMap<>(
                        CURRENTLY_LOADED_STATES.get(info));
                oldStates.putAll(states);
                CURRENTLY_LOADED_STATES.put(info, ImmutableMap.copyOf(oldStates));
            }
        }
        new Thread(() -> {
            sendPropertiesToClient(info, states);
            updateListeners(info, false);
            if (!contains.get())
                createToFile(info, states);
            info.signal.getUpdate(info.world, info.pos);
        }, "OSSignalStateHandler:setStates").start();
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
                return new HashMap<>();
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
        info.signal.getUpdate(info.world, info.pos);
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
        if (file == null)
            return new HashMap<>();
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
            final String value = property.getObjFromID(typeID - 1);
            map.put(property, value);
        }
        return map;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        final IChunk chunk = event.getChunk();
        final World world = (World) chunk.getWorldForge();
        if (world == null || world.isClientSide())
            return;
        synchronized (ALL_LEVEL_FILES) {
            if (!ALL_LEVEL_FILES.containsKey(world)) {
                ALL_LEVEL_FILES.put(world,
                        new SignalStateFile(Paths.get("osfiles/signalfiles/"
                                + ((ServerWorld) world).getServer().getLevelName().replace(":", "")
                                        .replace("/", "").replace("\\", "")
                                + "/" + world.getDimension().toString().replace(":", ""))));
            }
        }
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
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        final IChunk chunk = event.getChunk();
        final World level = (World) chunk.getWorldForge();
        if (level.isClientSide())
            return;
        chunk.getBlockEntitiesPos().forEach(pos -> {
            final Block block = chunk.getBlockState(pos).getBlock();
            if (block instanceof SignalBox) {
                SignalBoxHandler.unloadSignals(new PosIdentifier(pos, level));
            }
        });
        List<SignalStateInfo> states;
        synchronized (CURRENTLY_LOADED_CHUNKS) {
            states = CURRENTLY_LOADED_CHUNKS.remove(chunk);
        }
        unloadSignals(states);
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save save) {
        final World world = (World) save.getWorld();
        if (world.isClientSide())
            return;
        final Map<SignalStateInfo, Map<SEProperty, String>> maps;
        synchronized (CURRENTLY_LOADED_STATES) {
            maps = ImmutableMap.copyOf(CURRENTLY_LOADED_STATES);
        }
        new Thread(() -> {
            synchronized (ALL_LEVEL_FILES) {
                maps.entrySet().stream().filter(entry -> entry.getKey().world.equals(world))
                        .forEach(entry -> createToFile(entry.getKey(), entry.getValue()));
            }
        }, "OSSignalStateHandler:save").start();
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload unload) {
        if (unload.getWorld().isClientSide())
            return;
        synchronized (ALL_LEVEL_FILES) {
            ALL_LEVEL_FILES.remove(unload.getWorld());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final PlayerEntity player = event.getPlayer();
        Map<SignalStateInfo, Map<SEProperty, String>> map;
        synchronized (CURRENTLY_LOADED_STATES) {
            map = ImmutableMap.copyOf(CURRENTLY_LOADED_STATES);
        }
        map.forEach((state, properites) -> sendTo(player, packToByteBuffer(state, properites)));
    }

    public static void setRemoved(final SignalStateInfo info) {
        synchronized (CURRENTLY_LOADED_STATES) {
            CURRENTLY_LOADED_STATES.remove(info);
        }
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
        synchronized (ALL_LISTENERS) {
            ALL_LISTENERS.remove(info);
        }
        final IChunk chunk = info.world.getChunk(info.pos);
        if (chunk == null)
            return;
        synchronized (CURRENTLY_LOADED_CHUNKS) {
            CURRENTLY_LOADED_CHUNKS.get(chunk).remove(info);
        }
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

    @SubscribeEvent
    public static void onChunkWatch(final ChunkWatchEvent.Watch event) {
        final ServerWorld world = event.getWorld();
        final IChunk chunk = world.getChunk(event.getPos().getWorldPosition());
        final ServerPlayerEntity player = event.getPlayer();
        final List<ByteBuffer> toUpdate = new ArrayList<>();
        chunk.getBlockEntitiesPos().forEach(pos -> {
            final Block block = world.getBlockState(pos).getBlock();
            if (block instanceof Signal) {
                final WriteBuffer buffer = new WriteBuffer();
                buffer.putBlockPos(pos);
                buffer.putByte((byte) 0);
                toUpdate.add(buffer.build());
            }
        });
        toUpdate.forEach(buffer -> sendTo(player, buffer));
    }

    public static void loadSignal(final SignalStateInfo info) {
        loadSignals(ImmutableList.of(info));
    }

    public static void loadSignals(final List<SignalStateInfo> signals) {
        if (signals == null || signals.isEmpty())
            return;
        new Thread(() -> {
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
        }, "OSSignalStateHandler:loadSignals").start();
    }

    public static void unloadSignal(final SignalStateInfo info) {
        unloadSignals(ImmutableList.of(info));
    }

    public static void unloadSignals(final List<SignalStateInfo> signals) {
        if (signals == null || signals.isEmpty())
            return;
        new Thread(() -> {
            signals.forEach(info -> {
                if (info.signal != null && info.pos != null && info.world != null) {
                    synchronized (SIGNAL_COUNTER) {
                        Integer count = SIGNAL_COUNTER.get(info);
                        if (count != null && count > 1) {
                            SIGNAL_COUNTER.put(info, --count);
                            return;
                        }
                        SIGNAL_COUNTER.remove(info);
                    }
                    Map<SEProperty, String> properties;
                    synchronized (CURRENTLY_LOADED_STATES) {
                        properties = CURRENTLY_LOADED_STATES.remove(info);
                    }
                    if (properties == null)
                        return;
                    createToFile(info, properties);
                }
            });
        }, "OSSignalStateHandler:unloadSignals").start();
    }

    private static void sendTo(final PlayerEntity player, final ByteBuffer buf) {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.copiedBuffer(buf.array()));
        if (player instanceof ServerPlayerEntity) {
            final ServerPlayerEntity server = (ServerPlayerEntity) player;
            server.connection.send(new SCustomPayloadPlayPacket(channelName, buffer));
        } else {
            final Minecraft mc = Minecraft.getInstance();
            mc.getConnection().send(new CCustomPayloadPacket(channelName, buffer));
        }
    }

    @SubscribeEvent
    public void clientEvent(final ClientCustomPayloadEvent event) {
        deserializeServer(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }
}
