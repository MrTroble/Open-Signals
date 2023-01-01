package com.troblecodings.signals.test;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.signals.SEProperty;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class DummyBlockState implements IModelData {

    private final Map<SEProperty, Object> map = new HashMap<>();

    public DummyBlockState(final SEProperty property, final Object value) {
        super();
        map.put(property, value);
    }

    public DummyBlockState(final Map<SEProperty, Object> in) {
        this.map.putAll(in);
    }

    public DummyBlockState put(final SEProperty prop, final Object obj) {
        map.put(prop, obj);
        return this;
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return false;
    }

    @Override
    public <T> T getData(ModelProperty<T> prop) {
        return null;
    }

    @Override
    public <T> T setData(ModelProperty<T> prop, T data) {
        return null;
    }

}
