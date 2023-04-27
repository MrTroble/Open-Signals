package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.model.BuiltInModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.SeparatePerspectiveModel.BakedModel;

public final class DefaultModel implements IUnbakedModel {

    public static final DefaultModel INSTANCE = new DefaultModel();

    private DefaultModel() {
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, IUnbakedModel> p_225614_1_,
            Set<Pair<String, String>> p_225614_2_) {
        return new ArrayList<>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public BakedModel bake(ModelBakery p_225613_1_,
            Function<RenderMaterial, TextureAtlasSprite> p_225613_2_, IModelTransform p_225613_3_,
            ResourceLocation p_225613_4_) {
        return new BuiltInModel(ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY,
                function.apply(new Material(TextureAtlas.LOCATION_BLOCKS,
                        MissingTextureAtlasSprite.getLocation())),
                false);
    }
}