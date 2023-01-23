package com.troblecodings.signals.statehandler;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SignalStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> currentlyLoadedStates = new HashMap<>();
    private static final Map<ChunkAccess, List<SignalStateInfo>> currentlyLoadedChunks = new HashMap<>();
    private static final Map<Level, SignalStateFile> allLevelFiles = new HashMap<>();

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(2);

    public static void setStates(final SignalStateInfo info, final Map<SEProperty, String> states) {
        if (info.world.isClientSide || states == null || states.isEmpty()) {
            return;
        }
        synchronized (currentlyLoadedStates) {
            if (currentlyLoadedStates.containsKey(info)) {
                currentlyLoadedStates.put(info, states);
                sendPropertiesToClient(info, states);
                return;
            }
        }
        final SignalStateFile file = allLevelFiles.get(info.world);
        if (file == null) {
            return;
        }
        SERVICE.submit(() -> {
            SignalStatePos pos = file.find(info.pos);
            if (pos == null) {
                pos = file.create(info.pos);
            }
            final ByteBuffer buffer = file.read(pos);
            final byte[] readData = buffer.array();
            states.forEach((property, string) -> {
                readData[info.signal.getIDFromProperty(property)] = (byte) property.getParent()
                        .getIDFromValue(string);
            });
            file.write(pos, buffer);
        });
        sendPropertiesToClient(info, states);
    }

    public static Map<SEProperty, String> getStates(final SignalStateInfo info) {
        final Map<SEProperty, String> states = currentlyLoadedStates.get(info);
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
        final SignalStatePos pos = file.find(stateInfo.pos);
        final ByteBuffer buffer = file.read(pos);
        final List<SEProperty> properties = stateInfo.signal.getProperties();
        for (int i = 0; i < properties.size(); i++) {
            final SEProperty property = properties.get(i);
            final String value = property.getObjFromID(Byte.toUnsignedInt(buffer.get()));
            map.put(property, value);
        }
        return map;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        final ChunkAccess chunk = event.getChunk();
        if (chunk.getWorldForge().isClientSide())
            return;
        final Level world = (Level) chunk.getWorldForge();
        if (!allLevelFiles.containsKey(world)) {
            allLevelFiles.put(world,
                    new SignalStateFile(Paths
                            .get("ossignalfiles/" + world.dimension().getRegistryName().toString()
                                    .replace(":", "").replace("/", "").replace("\\", ""))));
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
                final ByteBuffer buffer = ByteBuffer.allocate(properties.size());
                properties.forEach((property, value) -> {
                    buffer.put((byte) property.getParent().getIDFromValue(value));
                });
                file.write(pos, buffer);
                unRenderClients(stateInfo);
            });
        });
    }

    private static void unRenderClients(final SignalStateInfo stateInfo) {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 255);
        stateInfo.world.players().forEach(player -> {
            if (checkInRange(player.blockPosition(), stateInfo.pos)) {
                // TODO add network handler for unrender
            }
        });
    }

    private static void sendPropertiesToClient(final SignalStateInfo stateInfo,
            final Map<SEProperty, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        final ByteBuffer buffer = ByteBuffer.allocate(13 + properties.size() * 2);
        buffer.putInt(stateInfo.pos.getX());
        buffer.putInt(stateInfo.pos.getY());
        buffer.putInt(stateInfo.pos.getZ());
        buffer.put((byte) properties.size());
        properties.forEach((property, value) -> {
            buffer.put((byte) stateInfo.signal.getIDFromProperty(property));
            buffer.put((byte) property.getParent().getIDFromValue(value));
        });
        stateInfo.world.players().forEach(player -> {
            if (checkInRange(player.blockPosition(), stateInfo.pos)) {
                // TODO add network handler
            }
        });
    }

    /**
     * This is the distance in blocks in which signals get rendered
     */
    private static final int RENDER_DISTANCE = 512;

    private static boolean checkInRange(final BlockPos playerPos, final BlockPos signalPos) {
        return Math.abs(playerPos.getX() - signalPos.getX()) <= RENDER_DISTANCE
                || Math.abs(playerPos.getY() - signalPos.getY()) <= RENDER_DISTANCE
                || Math.abs(playerPos.getZ() - signalPos.getZ()) <= RENDER_DISTANCE;
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BlockPos signalPos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        final Minecraft mc = Minecraft.getInstance();
        final SignalStateInfo stateInfo = new SignalStateInfo(mc.player.level, signalPos);
        final int propertiesSize = Byte.toUnsignedInt(buf.get());
        final List<SEProperty> signalProperties = stateInfo.signal.getProperties();
        Map<SEProperty, String> properties;
        if (currentlyLoadedStates.containsKey(stateInfo)) {
            properties = currentlyLoadedStates.get(stateInfo);
        } else {
            properties = new HashMap<>();
        }
        for (int i = 0; i < propertiesSize; i++) {
            final SEProperty property = signalProperties.get(Byte.toUnsignedInt(buf.get()));
            final List<String> values = (List<String>) property.getParent().getAllowedValues();
            final String value = values.get(Byte.toUnsignedInt(buf.get()));
            properties.put(property, value);
        }
        currentlyLoadedStates.put(stateInfo, properties);
    }
}