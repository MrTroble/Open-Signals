package com.troblecodings.signals.models;

import java.util.function.Predicate;

import net.minecraft.client.renderer.model.IBakedModel;

public class BakedModelPair {

    public final Predicate<ModelInfoWrapper> predicate;
    public final IBakedModel model;

    public BakedModelPair(final Predicate<ModelInfoWrapper> predicate, final IBakedModel model) {
        this.predicate = predicate;
        this.model = model;
    }
}