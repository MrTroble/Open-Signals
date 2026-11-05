package com.troblecodings.signals.models;

import java.util.Map;

import com.troblecodings.core.interfaces.BlockModelDataWrapper;
import com.troblecodings.signals.SEProperty;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * Wraps a {@link ModelData} so signal state can be queried by {@link SEProperty}.
 *
 * Up to 1.18 this implemented Forge's {@code IModelData} interface directly. Since 1.19
 * {@code ModelData} is a final, immutable class built through a builder, so the wrapper holds one
 * instead of being one, and mutation produces a new instance.
 */
public class ModelInfoWrapper implements BlockModelDataWrapper {

    private ModelData data;

    public ModelInfoWrapper(final Map<SEProperty, String> states) {
        final ModelData.Builder builder = ModelData.builder();
        states.forEach((property, value) -> builder.with(property, value));
        this.data = builder.build();
    }

    public ModelInfoWrapper(final ModelData data) {
        this.data = data;
    }

    @Override
    public ModelData getModelData() {
        return data;
    }

    public boolean hasProperty(final ModelProperty<?> prop) {
        return data.has(prop);
    }

    public <T> T getData(final ModelProperty<T> prop) {
        return data.get(prop);
    }

    public <T> void setData(final ModelProperty<T> prop, final T value) {
        this.data = data.derive().with(prop, value).build();
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
