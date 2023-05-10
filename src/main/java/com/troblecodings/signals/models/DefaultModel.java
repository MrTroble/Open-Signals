package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.BuiltInModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public final class DefaultModel implements IUnbakedModel {

    public static final DefaultModel INSTANCE = new DefaultModel();

    private DefaultModel() {
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public Collection<RenderMaterial> getMaterials(
            Function<ResourceLocation, IUnbakedModel> p_225614_1_,
            Set<Pair<String, String>> p_225614_2_) {
        return new ArrayList<>();
    }

    @Override
    public IBakedModel bake(final ModelBakery bakery,
            final Function<RenderMaterial, TextureAtlasSprite> function,
            final IModelTransform transform, final ResourceLocation location) {
        return new BuiltInModel(ItemCameraTransforms.NO_TRANSFORMS, ItemOverrideList.EMPTY,
                function.apply(new RenderMaterial(location, location)), false);
    }

}