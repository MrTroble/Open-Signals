package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public final class NameHandler implements INetworkSync {

    private static final Map<NameStateInfo, String> ALL_NAMES = new HashMap<>();
    private static final Map<World, NameHandlerFile> ALL_LEVEL_FILES = new HashMap<>();
    private static final Map<NameStateInfo, Integer> LOAD_COUNTER = new HashMap<>();
    private static final String CHANNELNAME = "namehandlernet";
    private static FMLEventChannel channel;

    public static void init() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELNAME);
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
            sendToAll(info, name);
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

    private static void sendToAll(final NameStateInfo info, final String name) {
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
        final Chunk chunk = event.getChunkInstance();
        final World world = chunk.getWorld();
        if (world == null || world.isRemote)
            return;
        final EntityPlayer player = event.getPlayer();
        final List<NameStateInfo> states = new ArrayList<>();
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
        chunk.getTileEntityMap().forEach((pos, tile) -> {
            if (tile instanceof SignalTileEntity || tile instanceof RedstoneIOTileEntity) {
                states.add(new NameStateInfo(world, pos));
            }
        });
        loadNames(states, player);
    }

    @SubscribeEvent
    public static void onChunkUnWatch(final ChunkWatchEvent.UnWatch event) {
        final Chunk chunk = event.getChunkInstance();
        final World world = chunk.getWorld();
        final List<NameStateInfo> states = new ArrayList<>();
        chunk.getTileEntityMap().forEach((pos, tile) -> {
            if (tile instanceof SignalTileEntity || tile instanceof RedstoneIOTileEntity) {
                states.add(new NameStateInfo(world, pos));
            }
        });
        unloadNames(states);
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

    private static void loadNames(final List<NameStateInfo> infos,
            final @Nullable EntityPlayer player) {
        if (infos == null || infos.isEmpty())
            return;
        new Thread(() -> {
            infos.forEach(info -> {
                synchronized (LOAD_COUNTER) {
                    Integer count = LOAD_COUNTER.get(info);
                    if (count != null && count > 0) {
                        LOAD_COUNTER.put(info, ++count);
                        return;
                    }
                    LOAD_COUNTER.put(info, 1);
                    String name;
                    synchronized (ALL_LEVEL_FILES) {
                        name = ALL_LEVEL_FILES.get(info.world).getString(info.pos);
                    }
                    synchronized (ALL_NAMES) {
                        ALL_NAMES.put(info, name);
                    }
                    if (player == null) {
                        sendToAll(info, name);
                    } else {
                        sendTo(player, packToBuffer(info.pos, name));
                    }
                }
            });
        }, "OSNameHandler:loadNames").start();
    }

    private static void unloadNames(final List<NameStateInfo> infos) {
        if (infos == null || infos.isEmpty())
            return;
        new Thread(() -> {
            infos.forEach(info -> {
                synchronized (LOAD_COUNTER) {
                    Integer count = LOAD_COUNTER.get(info);
                    if (count != null && count > 1) {
                        LOAD_COUNTER.put(info, --count);
                        return;
                    }
                    LOAD_COUNTER.remove(info);
                }
                String name;
                synchronized (ALL_NAMES) {
                    name = ALL_NAMES.remove(info);
                }
                if (name == null)
                    return;
                createToFile(info, name);

            });
        }, "OSNameHandler:unloadNames").start();
    }

    private static void sendTo(final EntityPlayer player, final ByteBuffer buf) {
        final PacketBuffer buffer = new PacketBuffer(
                Unpooled.copiedBuffer((ByteBuffer) buf.position(0)));
        if (player instanceof EntityPlayerMP) {
            final EntityPlayerMP server = (EntityPlayerMP) player;
            channel.sendTo(new FMLProxyPacket(buffer, CHANNELNAME), server);
        } else {
            channel.sendToServer(new FMLProxyPacket(new CPacketCustomPayload(CHANNELNAME, buffer)));
        }
    }

    @SubscribeEvent
    public void clientEvent(final ClientCustomPacketEvent event) {
        deserializeServer(event.getPacket().payload().nioBuffer());
    }
}