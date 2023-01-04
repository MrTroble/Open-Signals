package com.troblecodings.signals.statehandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
            final SignalStatePos pos = file.find(info.pos);
            final ByteBuf buf = file.read(pos);
            // TODO Sync Client and update properties in file
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
            final SignalStatePos pos = file.find(info.pos);
            final ByteBuf buffer = file.read(pos);
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
        final ChunkAccess chunk = event.getChunk();
        final List<SignalStateInfo> states = new ArrayList<>();
        chunk.getBlockEntitiesPos().forEach(pos -> {
            final Block block = chunk.getBlockState(pos).getBlock();
            if (!(block instanceof Signal)) {
                return;
            }
            // TODO read states out of files and add them to currentlyLoadedStates
            states.add(new SignalStateInfo(chunk, pos));
        });
        currentlyLoadedChunks.put(chunk, states);
        // TODO sync client
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        final ChunkAccess chunk = event.getChunk();
        currentlyLoadedChunks.get(chunk).forEach(signal -> currentlyLoadedStates.remove(signal));
        currentlyLoadedChunks.remove(chunk);
        // TODO sync client
    }
}