package com.troblecodings.signals.signalbridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.guilib.ecs.entitys.UIBlockRenderInfo;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.models.ModelInfoWrapper;

public class SignalBridgeRenderData {

    public static final ModelInfoWrapper EMPTY_WRAPPER = new ModelInfoWrapper(
            new SignalBridgeBasicBlock(null));
    private static final VectorWrapper RENDER_START = new VectorWrapper(15, 15, 0);

    private final Map<String, Map<SEProperty, String>> nameForRenderProperties = new HashMap<>();
    private final Map<String, Integer> signalHeights = new HashMap<>();
    private final SignalBridgeBuilder builder;
    private BiFunction<String, Signal, ModelInfoWrapper> function = (_u, _u1) -> EMPTY_WRAPPER;

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

    public boolean checkCollision(final String name, final VectorWrapper signalVec,
            final Signal signal) {
        final Entry<String, Signal> entry = Maps.immutableEntry(name, signal);
        final int height = signalHeights.getOrDefault(name, 0);
        for (int i = 0; i <= height; i++) {
            final VectorWrapper vec = new VectorWrapper(signalVec.getX(), signalVec.getY() - i,
                    signalVec.getZ());
            if (builder.hasBlockOn(vec, entry)) {
                return true;
            }
        }
        return false;
    }

    public List<UIBlockRenderInfo> getRenderPosAndBlocks() {
        final Builder<UIBlockRenderInfo> builder = ImmutableList.builder();
        this.builder.getPointsForBlocks().forEach((point, block) -> {
            final VectorWrapper vector = new VectorWrapper(point.getX(), point.getY(), 0);
            builder.add(new UIBlockRenderInfo(block.getDefaultState(), EMPTY_WRAPPER,
                    RENDER_START.subtract(vector)));
        });
        this.builder.getVecsForSignals().forEach((entry,
                vec) -> builder.add(new UIBlockRenderInfo(entry.getValue().getDefaultState(),
                        function.apply(entry.getKey(), entry.getValue()),
                        RENDER_START.subtract(vec))));
        return builder.build();
    }

    public void setFunctionForModelData(
            final BiFunction<String, Signal, ModelInfoWrapper> function) {
        this.function = function;
    }

}