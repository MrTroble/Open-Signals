package com.troblecodings.signals.models;

import java.util.Map;

import com.troblecodings.core.interfaces.BlockModelDataWrapper;
import com.troblecodings.signals.SEProperty;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelInfoWrapper implements BlockModelDataWrapper {

    private final ModelData data;

    public ModelInfoWrapper(final Map<SEProperty, String> states) {
        final ModelData.Builder builder = ModelData.builder();
        states.forEach((property, value) -> builder.with(property, value));
        this.data = builder.build();
    }

    public ModelInfoWrapper(final ModelData data) {
        this.data = data;
    }

    public boolean hasProperty(final ModelProperty<?> prop) {
        return data.has(prop);
    }

    public <T> T getData(final ModelProperty<T> prop) {
        return data.get(prop);
    }

    public boolean has(final SEProperty property) {
        return hasProperty(property);
    }

    public String get(final SEProperty property) {
        return getData(property);
    }

    public ModelData getModelData() {
        return data;
    }

}