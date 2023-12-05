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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.StateInfo;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

public final class NameHandler implements INetworkSync {

    private static final Map<StateInfo, String> ALL_NAMES = new HashMap<>();
    private static final Map<World, NameHandlerFile> ALL_LEVEL_FILES = new HashMap<>();
    private static final Map<StateInfo, Integer> LOAD_COUNTER = new HashMap<>();
    private static ExecutorService writeService = Executors.newFixedThreadPool(5);
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "namehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new NameHandler());
    }
    
    @SubscribeEvent
    public static void onServerStop(final FMLServerStoppingEvent event) {
        Map<StateInfo, String> map;
        synchronized (ALL_NAMES) {
            map = ImmutableMap.copyOf(ALL_NAMES);
        }
        writeService.execute(() -> map.entrySet()
                .forEach(entry -> createToFile(entry.getKey(), entry.getValue())));
        writeService.shutdown();
        try {
            writeService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        writeService = null;
    }

    public static void registerToNetworkChannel(final Object obj) {
        channel.registerObject(obj);
    }

    public static void createName(final StateInfo info, final String name) {
        if (info.world.isClientSide || name == null)
            return;
        new Thread(() -> {
            setNameForNonSignal(info, name);
            createToFile(info, name);
        }, "OSNameHandler:createName").start();
    }

    public static void setNameForSignal(final StateInfo info, final String name) {
        if (info.world.isClientSide || name == null)
            return;
        setNameForNonSignal(info, name);
        final Block block = info.world.getBlockState(info.pos).getBlock();
        if (block instanceof Signal) {
            SignalStateHandler.setState(new SignalStateInfo(info.world, info.pos, (Signal) block),
                    Signal.CUSTOMNAME, "true");
        }
    }

    public static void setNameForNonSignal(final StateInfo info, final String name) {
        if (info.world.isClientSide || name == null)
            return;
        new Thread(() -> {
            synchronized (ALL_NAMES) {
                ALL_NAMES.put(info, name);
            }
            sendToAll(info, name);
        }, "OSNameHandler:setName").start();
    }

    public static String getName(final StateInfo info) {
        if (info.world.isClientSide)
            return "";
        synchronized (ALL_NAMES) {
            final String name = ALL_NAMES.get(info);
            if (name == null)
                return "";
            return name;
        }
    }

    private static void sendToAll(final StateInfo info, final String name) {
        final ByteBuffer buffer = packToBuffer(info.pos, name);
        info.world.players().forEach(player -> sendTo(player, buffer));
    }

    private static ByteBuffer packToBuffer(final BlockPos pos, final String name) {
        final byte[] bytes = name.getBytes();
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(pos);
        buffer.putByte((byte) bytes.length);
        for (final byte b : bytes) {
            buffer.putByte(b);
        }
        return buffer.build();
    }

    public static void setRemoved(final StateInfo info) {
        synchronized (ALL_NAMES) {
            ALL_NAMES.remove(info);
        }
        NameHandlerFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(info.world);
        }
        synchronized (file) {
            file.deleteIndex(info.pos);
        }
        sendRemoved(info);
    }

    private static void sendRemoved(final StateInfo info) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(info.pos);
        buffer.putByte((byte) 255);
        info.world.players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    @SubscribeEvent
    public static void onWorldLoad(final WorldEvent.Load load) {
        if (load.getWorld().isClientSide() || writeService != null)
            return;
        writeService = Executors.newFixedThreadPool(5);
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save event) {
        final World world = (World) event.getWorld();
        if (world.isClientSide)
            return;
        Map<StateInfo, String> map;
        synchronized (ALL_NAMES) {
            map = ImmutableMap.copyOf(ALL_NAMES);
        }
        if (writeService != null)
            writeService.execute(() -> map.entrySet().stream()
                    .filter(entry -> entry.getKey().world.equals(world))
                    .forEach(entry -> createToFile(entry.getKey(), entry.getValue())));
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload unload) {
        if (unload.getWorld().isClientSide())
            return;
        synchronized (ALL_LEVEL_FILES) {
            ALL_LEVEL_FILES.remove(unload.getWorld());
        }
    }

    private static void createToFile(final StateInfo info, final String name) {
        NameHandlerFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(info.world);
        }
        if (file == null)
            return;
        SignalStatePos posInFile = file.find(info.pos);
        synchronized (file) {
            if (posInFile == null) {
                posInFile = file.createState(info.pos, name);
                return;
            }
            file.writeString(posInFile, name);
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(final ChunkWatchEvent.Watch event) {
        final ServerWorld world = event.getWorld();
        if (world.isClientSide)
            return;
        final IChunk chunk = world.getChunk(event.getPos().getWorldPosition());
        final PlayerEntity player = event.getPlayer();
        final List<StateInfo> states = new ArrayList<>();
        synchronized (ALL_LEVEL_FILES) {
            if (!ALL_LEVEL_FILES.containsKey(world)) {
                ALL_LEVEL_FILES.put(world,
                        new NameHandlerFile(Paths.get("osfiles/namefiles/"
                                + world.getServer().getWorldData().getLevelName().replace(":", "")
                                        .replace("/", "").replace("\\", "")
                                + "/" + world.dimension().location().toString().replace(":", ""))));
            }
        }
        chunk.getBlockEntitiesPos().forEach(pos -> {
            final Block block = chunk.getBlockState(pos).getBlock();
            if (block instanceof Signal || block instanceof RedstoneIO) {
                final StateInfo info = new StateInfo(world, pos);
                states.add(info);
            }
        });
        loadNames(states, player);
    }

    @SubscribeEvent
    public static void onChunkUnWatch(final ChunkWatchEvent.UnWatch event) {
        final ServerWorld world = event.getWorld();
        if (world.isClientSide)
            return;
        final IChunk chunk = world.getChunk(event.getPos().getWorldPosition());
        final List<StateInfo> states = new ArrayList<>();
        chunk.getBlockEntitiesPos().forEach(pos -> {
            final Block block = chunk.getBlockState(pos).getBlock();
            if (block instanceof Signal || block instanceof RedstoneIO) {
                states.add(new StateInfo(world, pos));
            }
        });
        unloadNames(states);
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final PlayerEntity player = event.getPlayer();
        Map<StateInfo, String> map;
        synchronized (ALL_NAMES) {
            map = ImmutableMap.copyOf(ALL_NAMES);
        }
        map.forEach((state, name) -> sendTo(player, packToBuffer(state.pos, name)));
    }

    private static void loadNames(final List<StateInfo> infos,
            final @Nullable PlayerEntity player) {
        if (infos == null || infos.isEmpty())
            return;
        new Thread(() -> {
            infos.forEach(info -> {
                boolean isLoaded = false;
                synchronized (LOAD_COUNTER) {
                    Integer count = LOAD_COUNTER.get(info);
                    if (count != null && count > 0) {
                        LOAD_COUNTER.put(info, ++count);
                        isLoaded = true;
                    } else {
                        LOAD_COUNTER.put(info, 1);
                    }
                }
                if (isLoaded) {
                    if (player == null)
                        return;
                    String name;
                    synchronized (ALL_NAMES) {
                        name = ALL_NAMES.getOrDefault(info, "");
                    }
                    if (name.isEmpty())
                        return;
                    sendTo(player, packToBuffer(info.pos, name));
                    return;
                }
                NameHandlerFile file;
                synchronized (ALL_LEVEL_FILES) {
                    file = ALL_LEVEL_FILES.computeIfAbsent(info.world,
                            _u -> new NameHandlerFile(Paths.get("osfiles/namefiles/"
                                    + info.world.getServer().getWorldData().getLevelName()
                                            .replace(":", "").replace("/", "").replace("\\", "")
                                    + "/" + info.world.dimension().location().toString()
                                            .replace(":", ""))));
                }
                String name;
                synchronized (file) {
                    name = file.getString(info.pos);
                }
                synchronized (ALL_NAMES) {
                    ALL_NAMES.put(info, name);
                }
                sendToAll(info, name);
            });
        }, "NameHandler:loadNames").start();
    }

    private static void unloadNames(final List<StateInfo> infos) {
        if (infos == null || infos.isEmpty() || writeService == null)
            return;
        writeService.execute(() -> {
            infos.forEach(info -> {
                synchronized (LOAD_COUNTER) {
                    Integer count = LOAD_COUNTER.get(info);
                    if (count != null && count > 1) {
                        LOAD_COUNTER.put(info, --count);
                        return;
                    }
                    LOAD_COUNTER.remove(info);
                    String name;
                    synchronized (ALL_NAMES) {
                        name = ALL_NAMES.remove(info);
                    }
                    createToFile(info, name);
                }
            });
        });
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