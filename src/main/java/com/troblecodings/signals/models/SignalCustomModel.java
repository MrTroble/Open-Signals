package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SignalAngel;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.model.TRSRTransformation;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements IUnbakedModel {

    @Nonnull
    public static final Random RANDOM = new Random();

    private final SignalAngel angel;
    private final List<SignalModelLoaderInfo> list;
    private final List<ResourceLocation> dependencies;
    private final Map<String, ResourceLocation> materialsFromString = new HashMap<>();

    public SignalCustomModel(final SignalAngel angel, final List<SignalModelLoaderInfo> list) {
        super();
        this.angel = angel;
        this.list = list;
        this.dependencies = list.stream()
                .map(info -> new ResourceLocation(OpenSignalsMain.MODID, "block/" + info.name))
                .collect(Collectors.toList());
        list.forEach(info -> info.retexture
                .forEach((id, texture) -> materialsFromString.computeIfAbsent(texture,
                        _u -> new ResourceLocation(OpenSignalsMain.MODID, texture))));
    }

    private static void transform(final BakedQuad quad, final Quaternion quaterion) {
        final int[] oldVertex = quad.getVertices();
        final int size = DefaultVertexFormats.BLOCK.getIntegerSize();
        for (int i = 0; i < oldVertex.length; i += size) {
            final float x = Float.intBitsToFloat(oldVertex[i]);
            final float y = Float.intBitsToFloat(oldVertex[i + 1]);
            final float z = Float.intBitsToFloat(oldVertex[i + 2]);
            final Vector4f vector = new Vector4f(x, y, z, 1);
            vector.transform(quaterion);
            oldVertex[i + 0] = Float.floatToIntBits(vector.x());
            oldVertex[i + 1] = Float.floatToIntBits(vector.y());
            oldVertex[i + 2] = Float.floatToIntBits(vector.z());
        }
    }

    private static BakedModelPair transform(final SignalModelLoaderInfo info,
            final ModelBakery bakery, final Function<ResourceLocation, TextureAtlasSprite> function,
            final Map<String, ResourceLocation> material, final Quaternion rotation,
            final ISprite sprite) {
        final TRSRTransformation transformation = new TRSRTransformation(null);
        final BlockModel blockModel = (BlockModel) info.model;
        final Map<String, String> defaultMap = ImmutableMap.copyOf(blockModel.textureMap);
        info.retexture.forEach((id, texture) -> blockModel.textureMap.computeIfPresent(id,
                (_u, old) -> material.get(texture).getPath()));
        @SuppressWarnings("deprecation")
        final IBakedModel model = info.model.bake(bakery, function, sprite);
        blockModel.textureMap.putAll(defaultMap);
        final Matrix4f reverse = new Matrix4f();
        /*
         * reverse.setIdentity(); reverse.setTranslation(-0.5f, 0, -0.5f);
         * 
         * final Matrix4f matrix = Matrix4f.createScaleMatrix(1, 1, 1);
         * matrix.setTranslation(0.5f, 0, 0.5f); matrix.multiply(rotation);
         * matrix.multiply(reverse);
         */

        model.getQuads(null, null, RANDOM, EmptyModelData.INSTANCE)
                .forEach(quad -> transform(quad, rotation));
        for (final Direction direction : Direction.values()) {
            model.getQuads(null, direction, RANDOM, EmptyModelData.INSTANCE)
                    .forEach(quad -> transform(quad, rotation));
        }
        return new BakedModelPair(info.state, model);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.dependencies;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBakedModel bake(final ModelBakery bakery,
            final Function<ResourceLocation, TextureAtlasSprite> function, final ISprite sprite,
            final VertexFormat format) {
        list.forEach(info -> {
            if (info.model == null) {
                final ResourceLocation location = new ResourceLocation(OpenSignalsMain.MODID,
                        "block/" + info.name);
                if (bakery instanceof ModelLoader) {
                    info.model = ModelLoaderRegistry.getModelOrLogError(location,
                            String.format("Could not find %s!", location));
                } else {
                    info.model = bakery.getModel(location);
                }
            }
        });
        final Quaternion quaternion = angel.getQuaternion();
        return new SignalBakedModel(list.stream().map(
                info -> transform(info, bakery, function, materialsFromString, quaternion, sprite))
                .collect(Collectors.toList()));
    }

    @Override
    public Collection<ResourceLocation> getTextures(
            final Function<ResourceLocation, IUnbakedModel> function,
            final Set<String> modelState) {
        final Collection<ResourceLocation> material = new ArrayList<>();
        this.dependencies.forEach(location -> material
                .addAll(function.apply(location).getTextures(function, modelState)));
        materialsFromString.values().forEach(opt -> material.add(opt));
        return material;
    }
}