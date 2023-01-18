package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.world.entity.player.Player;

public interface PropertyPacket {

    default void sendProperty(final Player player, final int idProperty, final int idvalue) {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) idProperty);
        buffer.put((byte) idvalue);
        OpenSignalsMain.network.sendTo(player, buffer);
    }
}