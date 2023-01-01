package com.troblecodings.signals.statehandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.troblecodings.signals.SEProperty;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.world.ChunkEvent;

public final class SignalStateHandler {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> currentlyLoadedStates = new HashMap<>();
    private static final Map<ChunkAccess, List<SignalStateInfo>> currentlyLoadedChunks = new HashMap<>();
    private static final Map<Level, SignalStateFile> allLevelFiles = new HashMap<>();

    public static void setStates(final SignalStateInfo info, final Map<SEProperty, String> states) {
        if (currentlyLoadedStates.containsKey(info)) {
            currentlyLoadedStates.put(info, states);
            // TODO Update Client
        } else {
            // TODO Write into an file
        }
    }

    public static Optional<Map<SEProperty, String>> getStates(final SignalStateInfo info) {
        if (currentlyLoadedStates.containsKey(info)) {
            return Optional.of(currentlyLoadedStates.get(info));
        } else {
            // TODO Read out of files
        }
        return Optional.empty();
    }

    public static void setState(final SignalStateInfo info, final SEProperty property,
            final String value) {
        final Map<SEProperty, String> map = new HashMap<>();
        map.put(property, value);
        setStates(info, map);
    }

    public static Optional<String> getState(final SignalStateInfo info, final SEProperty property) {
        final Optional<Map<SEProperty, String>> properties = getStates(info);
        if (!properties.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(properties.get().get(property));
    }

    public static void onChunkLoad(final ChunkEvent.Load event) {
        // TODO ad chunk to currentlyLoadedChunks and read out signals and sync client
    }

    public static void onChunkUnload(final ChunkEvent.Unload event) {
        // TODO remove from currentlyLoadedChunks all signals and sync client
    }

}
