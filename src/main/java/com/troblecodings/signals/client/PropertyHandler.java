package com.troblecodings.signals.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PropertyHandler implements INetworkSync {

    private final Level world;

    public PropertyHandler(final Level world) {
        if (!world.isClientSide)
            throw new IllegalStateException("World must be client Side!");
        this.world = world;
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BlockPos pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        final Signal signal = (Signal) world.getBlockState(pos).getBlock();
        final int propSize = Byte.toUnsignedInt(buf.get());
        final Map<SEProperty, String> properties = new HashMap<>();
        final List<SEProperty> signalProperties = signal.getProperties();
        for (int i = 0; i < propSize; i++) {
            final SEProperty property = signalProperties.get(Byte.toUnsignedInt(buf.get()));
            properties.put(property, property.getObjFromID(Byte.toUnsignedInt(buf.get())));
        }
        // TODO inform client about render update
    }
}