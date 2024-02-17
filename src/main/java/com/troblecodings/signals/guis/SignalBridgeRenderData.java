package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

import net.minecraft.util.math.vector.Vector3i;

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

    public void updateRenderProperties(final String name, final Map<SEProperty, Integer> properties,
            final Signal signal) {
        final Map<SEProperty, String> allProperties = nameForRenderProperties.computeIfAbsent(name,
                _u -> new HashMap<>());
        properties.forEach(
                (property, valueID) -> addToRenderNormal(allProperties, property, valueID));
        signalHeights.put(name, signal.getHeight(allProperties));
    }

    private static void addToRenderNormal(final Map<SEProperty, String> properties,
            final SEProperty property, final int valueID) {
        if (valueID < 0) {
            properties.remove(property);
            return;
        }
        if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
            properties.put(property, property.getObjFromID(valueID));
        } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
            if (valueID > 0) {
                properties.put(property, property.getDefault());
            } else {
                properties.remove(property);
            }
        } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
            properties.put(property, property.getDefault());
        }
    }

    public boolean checkCollision(final String name, final Vector3i signalVec,
            final Signal signal) {
        final Entry<String, Signal> entry = Maps.immutableEntry(name, signal);
        final int height = signalHeights.getOrDefault(name, 0);
        for (int i = 0; i <= height; i++) {
            final Vector3i vec = new Vector3i(signalVec.getX(), signalVec.getY() - i,
                    signalVec.getZ());
            if (builder.hasBlockOn(vec, entry)) {
                return true;
            }
        }
        return false;
    }

}