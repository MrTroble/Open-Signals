package com.troblecodings.signals.models;

import java.util.Map;

import com.google.common.collect.ImmutableMap.Builder;
import com.troblecodings.signals.SEProperty;

public class ModelInfoWrapper {

    private final IModelData data;

    public ModelInfoWrapper(final Map<SEProperty, String> states) {
        final Builder builder = new ModelDataMap.Builder();
        states.forEach((property, value) -> builder.withInitial(property, value));
        this.data = builder.build();
    }

    public ModelInfoWrapper(final IModelData data) {
        this.data = data;
    }

    @Override
    public boolean hasProperty(final ModelProperty<?> prop) {
        return data.hasProperty(prop);
    }

    @Override
    public <T> T getData(final ModelProperty<T> prop) {
        return data.getData(prop);
    }

    @Override
    public <T> T setData(final ModelProperty<T> prop, final T value) {
        return data.setData(prop, value);
    }

    public boolean has(final SEProperty property) {
        return hasProperty(property);
    }

}
