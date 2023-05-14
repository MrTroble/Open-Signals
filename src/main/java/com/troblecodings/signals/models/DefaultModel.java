package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.client.renderer.model.BuiltInModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("deprecation")
public final class DefaultModel implements IUnbakedModel {

    public static final DefaultModel INSTANCE = new DefaultModel();

    private DefaultModel() {
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public IBakedModel bake(ModelBakery bakery,
            Function<ResourceLocation, TextureAtlasSprite> function, ISprite sprite,
            VertexFormat format) {
        return new BuiltInModel(ItemCameraTransforms.NO_TRANSFORMS, ItemOverrideList.EMPTY,
                function.apply(new ResourceLocation(OpenSignalsMain.MODID, "")));
    }

    @Override
    public Collection<ResourceLocation> getTextures(
            Function<ResourceLocation, IUnbakedModel> p_209559_1_, Set<String> p_209559_2_) {
        return new ArrayList<>();
    }
}