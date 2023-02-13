package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import net.minecraft.world.entity.player.Player;

public interface PropertyPacket {

    default void sendPropertyToServer(final Player player, final Signal signal,
            final SEProperty property, final String value) {
        sendPropertyToServer(player, signal, property, property.getParent().getIDFromValue(value));
    }

    default void sendPropertyToServer(final Player player, final Signal signal,
            final SEProperty property, final int value) {
        sendPropertyToServer(player, signal.getIDFromProperty(property), value);
    }

    default void sendPropertyToServer(final Player player, final int idProperty,
            final int idvalue) {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) idProperty);
        buffer.put((byte) idvalue);
        OpenSignalsMain.network.sendTo(player, buffer);
    }
}