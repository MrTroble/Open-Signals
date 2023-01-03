package com.troblecodings.signals.models;

import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("rawtypes")
public class SignalModelLoaderInfo {
    public String name;
    public Predicate state;
    public float x;
    public float y;
    public float z;
    public Map<String, String> retexture;
    public UnbakedModel model;

    public SignalModelLoaderInfo(String name, Predicate state, float x, float y, float z,
            Map<String, String> retexture) {
        super();
        this.name = name;
        this.state = state;
        this.x = x;
        this.y = y;
        this.z = z;
        this.retexture = retexture;
    }
    
    public SignalModelLoaderInfo with(UnbakedModel model) {
        this.model = model;
        return this;
    }

}