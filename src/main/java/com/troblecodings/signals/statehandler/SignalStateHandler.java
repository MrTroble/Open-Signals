package com.troblecodings.signals.statehandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.troblecodings.signals.SEProperty;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SignalStateHandler {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> currentlyLoadedStates = new HashMap<>();
    private static final Map<ChunkAccess, List<SignalStateInfo>> currentlyLoadedChunks = new HashMap<>();
    private static final Map<Level, SignalStateFile> allLevelFiles = new HashMap<>();

    public static void setStates(final SignalStateInfo info, final Map<SEProperty, String> states) {
        if (currentlyLoadedStates.containsKey(info)) {
            currentlyLoadedStates.put(info, states);

        } else {
            final SignalStateFile file = allLevelFiles.get(info.world);
            if (file == null) {
                return;
            }
            file.write(new SignalStatePos(0, 0), null);
            // TODO Update Client
        }
    }

    public static Map<SEProperty, String> getStates(final SignalStateInfo info) {
        if (currentlyLoadedStates.containsKey(info)) {
            return currentlyLoadedStates.get(info);
        } else {
            final SignalStateFile file = allLevelFiles.get(info.world);
            if (file == null) {
                return Map.of();
            }
            final ByteBuf buffer = file.read(new SignalStatePos(0, 0));
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

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        // TODO ad chunk to currentlyLoadedChunks and read out signals and sync client
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        // TODO remove from currentlyLoadedChunks all signals and sync client
    }

}
