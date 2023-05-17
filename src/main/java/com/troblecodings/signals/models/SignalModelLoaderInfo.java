package com.troblecodings.signals.models;

import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignalModelLoaderInfo {

    public final String name;
    public final Predicate<ModelInfoWrapper> state;
    public final float x;
    public final float y;
    public final float z;
    public final Map<String, String> retexture;
    public IUnbakedModel model;

    public SignalModelLoaderInfo(final String name, final Predicate<ModelInfoWrapper> state,
            final float x, final float y, final float z, final Map<String, String> retexture) {
        this.name = name;
        this.state = state;
        this.x = x;
        this.y = y;
        this.z = z;
        this.retexture = retexture;
    }

    public SignalModelLoaderInfo with(final IUnbakedModel model) {
        this.model = model;
        return this;
    }
}