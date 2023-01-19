package com.troblecodings.signals.statehandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SignalStateHandler {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> currentlyLoadedStates = new HashMap<>();
    private static final Map<ChunkAccess, List<SignalStateInfo>> currentlyLoadedChunks = new HashMap<>();
    private static final Map<Level, SignalStateFile> allLevelFiles = new HashMap<>();

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(2);

    public static void setStates(final SignalStateInfo info, final Map<SEProperty, String> states) {
        synchronized (currentlyLoadedStates) {
            if (currentlyLoadedStates.containsKey(info)) {
                currentlyLoadedStates.put(info, states);
                return;
            }
        }
        final SignalStateFile file = allLevelFiles.get(info.world);
        if (file == null) {
            return;
        }
        SERVICE.submit(() -> {
            final SignalStatePos pos = file.find(info.pos);
            final ByteBuffer buffer = file.read(pos);
            final byte[] readData = buffer.array();
            states.forEach((property, string) -> {
                readData[info.signal.getIDFromProperty(property)] = (byte) property.getParent()
                        .getIDFromValue(string);
            });
            file.write(pos, buffer);
        });
    }

    public static Map<SEProperty, String> getStates(final SignalStateInfo info) {
        final Map<SEProperty, String> states = currentlyLoadedStates.get(info);
        if (states != null) {
            return states;
        } else {
            final SignalStateFile file = allLevelFiles.get(info.world);
            if (file == null) {
                return Map.of();
            }
            final SignalStatePos pos = file.find(info.pos);
            final ByteBuffer buffer = file.read(pos);
        }
        return Map.of();
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

    private static Map<SEProperty, String> readAndSerialize(SignalStateInfo stateInfo) {
        Map<SEProperty, String> map = new HashMap<>();
        SignalStateFile file;
        synchronized (allLevelFiles) {
            file = allLevelFiles.get(stateInfo.world);
        }
        final SignalStatePos pos = file.find(stateInfo.pos);
        if(pos == null)
            return map;
        return map;
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        final ChunkAccess chunk = event.getChunk();
        if (chunk.getWorldForge().isClientSide())
            return;
        SERVICE.submit(() -> {
            final List<SignalStateInfo> states = new ArrayList<>();
            chunk.getBlockEntitiesPos().forEach(pos -> {
                final Block block = chunk.getBlockState(pos).getBlock();
                if (!(block instanceof Signal)) {
                    return;
                }
                final SignalStateInfo stateinfo = new SignalStateInfo(chunk, pos);
                final Map<SEProperty, String> map = readAndSerialize(stateinfo);
                synchronized (currentlyLoadedStates) {
                    currentlyLoadedStates.put(stateinfo, map);
                }
                states.add(stateinfo);
            });
            synchronized (currentlyLoadedChunks) {
                currentlyLoadedChunks.put(chunk, states);
            }
            // TODO sync client
        });
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        final ChunkAccess chunk = event.getChunk();
        if (chunk.getWorldForge().isClientSide())
            return;
        currentlyLoadedChunks.get(chunk).forEach(signal -> {
            // TODO Write Properties in Files
            currentlyLoadedStates.remove(signal);
        });
        currentlyLoadedChunks.remove(chunk);
        // TODO sync client
    }
}