package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.Signal;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

public final class NameHandler implements INetworkSync {

    private static final Map<NameStateInfo, String> ALL_NAMES = new HashMap<>();
    private static final Map<IChunk, List<NameStateInfo>> CURRENTLY_LOADED_CHUNKS = new HashMap<>();
    private static final Map<World, NameHandlerFile> ALL_LEVEL_FILES = new HashMap<>();
    private static EventNetworkChannel channel;
    private static ResourceLocation channelName;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "namehandler");
        channel = NetworkRegistry.newEventChannel(channelName, () -> OpenSignalsMain.MODID,
                OpenSignalsMain.MODID::equalsIgnoreCase, OpenSignalsMain.MODID::equalsIgnoreCase);
        channel.registerObject(new NameHandler());
    }

    public static void add(final Object obj) {
        channel.registerObject(obj);
    }

    public static void createName(final NameStateInfo info, final String name) {
        if (info.world.isClientSide || name == null)
            return;
        new Thread(() -> {
            setNameForSignal(info, name);
            createToFile(info, name);
        }, "OSNameHandler:createName").start();
    }

    public static void setNameForSignal(final NameStateInfo info, final String name) {
        if (info.world.isClientSide || name == null)
            return;
        setNameForNonSignal(info, name);
        final Block block = info.world.getBlockState(info.pos).getBlock();
        if (block instanceof Signal) {
            SignalStateHandler.setState(new SignalStateInfo(info.world, info.pos, (Signal) block),
                    Signal.CUSTOMNAME, "TRUE");
        }
    }

    public static void setNameForNonSignal(final NameStateInfo info, final String name) {
        if (info.world.isClientSide || name == null)
            return;
        new Thread(() -> {
            if (info.world.isClientSide)
                return;
            sendNameToClient(info, name);
            synchronized (ALL_NAMES) {
                ALL_NAMES.put(info, name);
            }
            createToFile(info, name);
        }, "OSNameHandler:setName").start();
    }

    public static String getName(final NameStateInfo info) {
        if (info.world.isClientSide)
            return "";
        synchronized (ALL_NAMES) {
            final String name = ALL_NAMES.get(info);
            if (name == null)
                return "";
            return name;
        }
    }

    private static void sendNameToClient(final NameStateInfo info, final String name) {
        final ByteBuffer buffer = packToBuffer(info.pos, name);
        info.world.players().forEach(player -> sendTo(player, buffer));
    }

    private static ByteBuffer packToBuffer(final BlockPos pos, final String name) {
        final byte[] bytes = name.getBytes();
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(pos);
        buffer.putByte((byte) name.length());
        for (final byte b : bytes) {
            buffer.putByte(b);
        }
        return buffer.build();
    }

    public static void setRemoved(final NameStateInfo info) {
        synchronized (ALL_NAMES) {
            ALL_NAMES.remove(info);
        }
        NameHandlerFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(info.world);
        }
        file.deleteIndex(info.pos);
        sendRemoved(info);
        final IChunk chunk = info.world.getChunk(info.pos);
        if (chunk == null)
            return;
        synchronized (CURRENTLY_LOADED_CHUNKS) {
            CURRENTLY_LOADED_CHUNKS.get(chunk).remove(info);
        }
    }

    private static void sendRemoved(final NameStateInfo info) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(info.pos);
        buffer.putByte((byte) 255);
        info.world.players().forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save event) {
        final World world = (World) event.getWorld();
        if (world.isClientSide)
            return;
        Map<NameStateInfo, String> map;
        synchronized (ALL_NAMES) {
            map = ImmutableMap.copyOf(ALL_NAMES);
        }
        new Thread(() -> {
            synchronized (ALL_LEVEL_FILES) {
                map.entrySet().stream().filter(entry -> entry.getKey().world.equals(world))
                        .forEach(entry -> createToFile(entry.getKey(), entry.getValue()));
            }
        }, "OSNameHandler:Save").start();
    }

    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload unload) {
        if (unload.getWorld().isClientSide())
            return;
        synchronized (ALL_LEVEL_FILES) {
            ALL_LEVEL_FILES.remove(unload.getWorld());
        }
    }

    private static void createToFile(final NameStateInfo info, final String name) {
        NameHandlerFile file;
        synchronized (ALL_LEVEL_FILES) {
            file = ALL_LEVEL_FILES.get(info.world);
        }
        if (file == null)
            return;
        SignalStatePos posInFile = file.find(info.pos);
        if (posInFile == null) {
            posInFile = file.createState(info.pos, name);
            return;
        }
        synchronized (posInFile) {
            file.writeString(posInFile, name);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        final IChunk chunk = event.getChunk();
        final World world = (World) chunk.getWorldForge();
        if (world.isClientSide())
            return;
        synchronized (ALL_LEVEL_FILES) {
            if (!ALL_LEVEL_FILES.containsKey(world)) {
                ALL_LEVEL_FILES.put(world,
                        new NameHandlerFile(Paths.get("osfiles/namefiles/"
                                + ((ServerWorld) world).getServer().getWorldData().getLevelName()
                                        .replace(":", "").replace("/", "").replace("\\", "")
                                + "/" + world.dimension().location().toString().replace(":", ""))));
            }
        }
        final List<NameStateInfo> states = new ArrayList<>();
        chunk.getBlockEntitiesPos().forEach(pos -> {
            final Block block = chunk.getBlockState(pos).getBlock();
            if (block instanceof Signal || block instanceof RedstoneIO) {
                final NameStateInfo info = new NameStateInfo(world, pos);
                final String name = ALL_LEVEL_FILES.get(world).getString(pos);
                if (name.isEmpty())
                    return;
                synchronized (ALL_NAMES) {
                    ALL_NAMES.put(info, name);
                }
                states.add(info);
                sendNameToClient(info, name);
            }
        });
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
        List<NameStateInfo> states;
        synchronized (CURRENTLY_LOADED_CHUNKS) {
            states = CURRENTLY_LOADED_CHUNKS.remove(chunk);
        }
        states.forEach(stateInfo -> {
            String name;
            synchronized (ALL_NAMES) {
                name = ALL_NAMES.remove(stateInfo);
            }
            if (name == null)
                return;
            createToFile(stateInfo, name);
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        final PlayerEntity player = event.getPlayer();
        final Map<NameStateInfo, String> names;
        synchronized (ALL_NAMES) {
            names = ImmutableMap.copyOf(ALL_NAMES);
        }
        names.forEach((info, name) -> sendTo(player, packToBuffer(info.pos, name)));
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