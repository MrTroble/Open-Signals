package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

import net.minecraft.core.Vec3i;

public class SignalBridgeRenderData {

    private final Map<String, Map<SEProperty, String>> nameForRenderProperties = new HashMap<>();
    private final Map<String, Integer> signalHeights = new HashMap<>();
    private final SignalBridgeBuilder builder;

    public SignalBridgeRenderData(final SignalBridgeBuilder builder) {
        this.builder = builder;
    }

    public Map<SEProperty, String> getDataForName(final String name) {
        return nameForRenderProperties.getOrDefault(name, new HashMap<>());
    }

    public void removeSignal(final String signalName) {
        nameForRenderProperties.remove(signalName);
        signalHeights.remove(signalName);
    }

    public void updateName(final String oldName, final String newName) {
        nameForRenderProperties.put(newName, nameForRenderProperties.remove(oldName));
        signalHeights.put(newName, signalHeights.remove(oldName));
    }

    public void putSignal(final String name, final Map<SEProperty, String> properties,
            final Signal signal) {
        final Map<SEProperty, String> allProperties = nameForRenderProperties.computeIfAbsent(name,
                _u -> new HashMap<>());
        allProperties.putAll(properties);
        signalHeights.put(name, signal.getHeight(allProperties));
    }

    public boolean checkCollision(final String name, final Vec3i signalVec, final Signal signal) {
        final Entry<String, Signal> entry = Maps.immutableEntry(name, signal);
        final int height = signalHeights.getOrDefault(name, 0);
        for (int i = 0; i <= height; i++) {
            final Vec3i vec = new Vec3i(signalVec.getX(), signalVec.getY() - i, signalVec.getZ());
            if (builder.hasBlockOn(vec, entry)) {
                return true;
            }
        }
        return false;
    }

}
