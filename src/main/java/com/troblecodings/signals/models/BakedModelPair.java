package com.troblecodings.signals.models;

import java.util.function.Predicate;

import net.minecraft.client.resources.model.BakedModel;

@SuppressWarnings("rawtypes")
public class BakedModelPair {
    public Predicate predicate;
    public BakedModel model;

    public BakedModelPair(Predicate predicate, BakedModel model) {
        this.predicate = predicate;
        this.model = model;
    }

}