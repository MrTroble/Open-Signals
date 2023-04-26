package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

@OnlyIn(Dist.CLIENT)
public class SignalBakedModel implements IDynamicBakedModel {

    private final BakedModel baseGetter;
    private final List<BakedModelPair> bakedCache;

    public SignalBakedModel(final List<BakedModelPair> bakedCache) {
        this.bakedCache = bakedCache;
        this.baseGetter = bakedCache.iterator().next().model;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseGetter.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseGetter.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseGetter.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseGetter.isCustomRenderer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseGetter.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return baseGetter.getOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final Random rand,
            final IModelData extraData) {
        final List<BakedQuad> quadBuilder = new ArrayList<>();
        final ModelInfoWrapper modelData = new ModelInfoWrapper(extraData);
        for (final BakedModelPair pair : bakedCache) {
            if (pair.predicate.test(modelData))
                quadBuilder.addAll(pair.model.getQuads(state, side, rand, modelData));
        }
        return quadBuilder;
    }
}