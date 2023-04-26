package com.troblecodings.signals.models;

import com.troblecodings.signals.SEProperty;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelInfoWrapper implements IModelData {

    private final IModelData data;

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

    public String get(final SEProperty property) {
        return getData(property);
    }

    public void set(final SEProperty property, final String value) {
        setData(property, value);
    }
}