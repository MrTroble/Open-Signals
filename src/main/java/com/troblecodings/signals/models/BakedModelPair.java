package com.troblecodings.signals.models;

import java.util.function.Predicate;

import net.minecraft.client.resources.model.BakedModel;

public class BakedModelPair {

    public final Predicate<ModelInfoWrapper> predicate;
    public final BakedModel model;

    public BakedModelPair(final Predicate<ModelInfoWrapper> predicate, final BakedModel model) {
        this.predicate = predicate;
        this.model = model;
    }
}