package com.troblecodings.signals.models;

import com.troblecodings.signals.SEProperty;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelInfoWrapper implements IModelData {

    private IModelData data;
        
    public ModelInfoWrapper(IModelData data) {
        this.data = data;
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return data.hasProperty(prop);
    }

    @Override
    public <T> T getData(ModelProperty<T> prop) {
        return data.getData(prop);
    }

    @Override
    public <T> T setData(ModelProperty<T> prop, T value) {
        return data.setData(prop, value);
    }

    public boolean has(SEProperty property) {
        return hasProperty(property);
    }
    
    public String get(SEProperty property) {
        return getData(property);
    }

    public void set(SEProperty property, String value) {
        setData(property, value);
    }
}
