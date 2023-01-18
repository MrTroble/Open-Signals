package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class DefaultModel implements UnbakedModel {

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Material> getMaterials(
            final Function<ResourceLocation, UnbakedModel> p_119538_,
            final Set<Pair<String, String>> p_119539_) {
        return new ArrayList<>();
    }

    @Override
    public BakedModel bake(final ModelBakery p_119534_,
            final Function<Material, TextureAtlasSprite> p_119535_, final ModelState p_119536_,
            final ResourceLocation p_119537_) {
        return null;
    }

}
