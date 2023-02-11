package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class DefaultModel implements UnbakedModel {

    public static final DefaultModel INSTANCE = new DefaultModel();
    
    private DefaultModel() {}
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Material> getMaterials(
            final Function<ResourceLocation, UnbakedModel> function,
            final Set<Pair<String, String>> set) {
        return new ArrayList<>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public BakedModel bake(final ModelBakery bakery,
            final Function<Material, TextureAtlasSprite> function, final ModelState stat,
            final ResourceLocation location) {
        return new BuiltInModel(ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY,
                function.apply(new Material(TextureAtlas.LOCATION_BLOCKS,
                        MissingTextureAtlasSprite.getLocation())),
                false);
    }

}
