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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public final class NameHandler implements INetworkSync {

    private static final Map<NameStateInfo, String> ALL_NAMES = new HashMap<>();
    private static final Map<Chunk, List<NameStateInfo>> CURRENTLY_LOADED_CHUNKS = new HashMap<>();
    private static final Map<World, NameHandlerFile> ALL_LEVEL_FILES = new HashMap<>();
    private static FMLEventChannel channel;
    private static String channelName;

    public static void init() {
        channelName = new ResourceLocation(OpenSignalsMain.MODID, "namehandler").toString();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName);
        channel.register(new NameHandler());
    }

    public static void add(final Object obj) {
        channel.register(obj);
    }

    public static void createName(final NameStateInfo info, final String name) {
        if (info.world.isRemote || name == null)
            return;
        new Thread(() -> {
            setNameForNonSignal(info, name);
            createToFile(info, name);
        }, "OSNameHandler:createName").start();
    }

    public static void setNameForSignal(final NameStateInfo info, final String name) {
        if (info.world.isRemote || name == null)
            return;
        setNameForNonSignal(info, name);
        final Block block = info.world.getBlockState(info.pos).getBlock();
        if (block instanceof Signal) {
            SignalStateHandler.setState(new SignalStateInfo(info.world, info.pos, (Signal) block),
                    Signal.CUSTOMNAME, "true");
        }
    }

    public static void setNameForNonSignal(final NameStateInfo info, final String name) {
        if (info.world.isRemote || name == null)
            return;
        new Thread(() -> {
            synchronized (ALL_NAMES) {
                ALL_NAMES.put(info, name);
            }
            sendNameToClient(info, name);
            createToFile(info, name);
        }, "OSNameHandler:setName").start();
    }

    public static String getName(final NameStateInfo info) {
        if (info.world.isRemote)
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
        info.world.playerEntities.forEach(player -> sendTo(player, buffer));
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
        final Chunk chunk = info.world.getChunkFromBlockCoords(info.pos);
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
        info.world.playerEntities.forEach(player -> sendTo(player, buffer.getBuildedBuffer()));
    }

    @SubscribeEvent
    public static void onWorldSave(final WorldEvent.Save event) {
        final World world = event.getWorld();
        if (world.isRemote)
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
        if (unload.getWorld().isRemote)
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
        final Chunk chunk = event.getChunk();
        final World world = chunk.getWorld();
        if (world == null || world.isRemote)
            return;
        synchronized (ALL_LEVEL_FILES) {
            if (!ALL_LEVEL_FILES.containsKey(world)) {
                ALL_LEVEL_FILES.put(world,
                        new NameHandlerFile(Paths.get("osfiles/namefiles/"
                                + ((WorldServer) world).getMinecraftServer().getName()
                                        .replace(":", "").replace("/", "").replace("\\", "")
                                + "/" + ((WorldServer) world).provider.getDimensionType().getName()
                                        .replace(":", ""))));
            }
        }
        final List<NameStateInfo> states = new ArrayList<>();
        chunk.getTileEntityMap().keySet().forEach(pos -> {
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
        final Chunk chunk = event.getChunk();
        final World level = chunk.getWorld();
        if (level.isRemote)
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
        final EntityPlayer player = event.player;
        final Map<NameStateInfo, String> names;
        synchronized (ALL_NAMES) {
            names = ImmutableMap.copyOf(ALL_NAMES);
        }
        names.forEach((info, name) -> sendTo(player, packToBuffer(info.pos, name)));
    }

    private static void sendTo(final EntityPlayer player, final ByteBuffer buf) {
        final PacketBuffer buffer = new PacketBuffer(
                Unpooled.copiedBuffer((ByteBuffer) buf.position(0)));
        if (player instanceof EntityPlayerMP) {
            final EntityPlayerMP server = (EntityPlayerMP) player;
            channel.sendTo(new FMLProxyPacket(buffer, channelName), server);
        } else {
            channel.sendToServer(new FMLProxyPacket(new CPacketCustomPayload(channelName, buffer)));
        }
    }

    @SubscribeEvent
    public void clientEvent(final ClientCustomPacketEvent event) {
        deserializeServer(event.getPacket().payload().nioBuffer());
    }
}