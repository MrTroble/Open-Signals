package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class SignalBakedModel implements IDynamicBakedModel {

    /**
     * Baking is deferred because this model is created during
     * {@code ModelEvent.ModifyBakingResult}, which is the only point at which an entry can still
     * reach the BlockState to BakedModel cache, but which runs before the texture atlases have been
     * uploaded. Resolving the quads needs sprites, so that work happens on first use (or when
     * {@link #resolve()} is called once the atlases are ready).
     */
    private final Supplier<List<BakedModelPair>> supplier;
    private List<BakedModelPair> bakedCache;

    public SignalBakedModel(final Supplier<List<BakedModelPair>> supplier) {
        this.supplier = supplier;
    }

    public void resolve() {
        pairs();
    }

    private List<BakedModelPair> pairs() {
        if (bakedCache == null)
            bakedCache = supplier.get();
        return bakedCache;
    }

    private BakedModel base() {
        return pairs().iterator().next().model;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base().useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base().isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return base().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return base().isCustomRenderer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base().getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return base().getOverrides();
    }

    @Override
    public @Nonnull List<BakedQuad> getQuads(final BlockState state, final Direction side,
            final @Nonnull RandomSource rand, final @Nonnull ModelData extraData,
            final @Nullable RenderType renderType) {
        final List<BakedQuad> quadBuilder = new ArrayList<>();
        final ModelInfoWrapper modelData = new ModelInfoWrapper(extraData);
        for (final BakedModelPair pair : pairs()) {
            if (pair.predicate.test(modelData))
                quadBuilder.addAll(
                        pair.model.getQuads(state, side, rand, extraData, renderType));
        }
        return quadBuilder;
    }
}
