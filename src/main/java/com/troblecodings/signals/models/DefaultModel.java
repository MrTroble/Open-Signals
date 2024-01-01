package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public final class DefaultModel implements UnbakedModel {

    public static final DefaultModel INSTANCE = new DefaultModel();

    private DefaultModel() {
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> p_119538_) {

    }

    @SuppressWarnings("deprecation")
    @Override
    public BakedModel bake(ModelBaker p_250133_, Function<Material, TextureAtlasSprite> function,
            ModelState p_119536_, ResourceLocation p_119537_) {
        return new BuiltInModel(ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY,
                function.apply(new Material(TextureAtlas.LOCATION_BLOCKS,
                        MissingTextureAtlasSprite.getLocation())),
                false);
    }
}