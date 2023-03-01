package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import com.troblecodings.signals.core.RedstonePacket;

import net.minecraft.core.BlockPos;

public final class SignalBoxHandler {

    private static final Map<BlockPos, LinkedList<RedstonePacket>> UPDATES = new HashMap<>();

    public static void addToQueue(final BlockPos pos, final RedstonePacket packet) {
        final LinkedList<RedstonePacket> packets = UPDATES.computeIfAbsent(pos,
                _u -> new LinkedList<>());
        packets.add(packet);
        UPDATES.put(pos, packets);
    }

    public static Optional<LinkedList<RedstonePacket>> getPacket(final BlockPos pos) {
        return Optional.ofNullable(UPDATES.get(pos));
    }
}