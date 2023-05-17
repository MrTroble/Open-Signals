package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SignalAngel;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements IUnbakedModel {

    @Nonnull
    public static final Random RANDOM = new Random();

    private final SignalAngel angel;
    private final List<SignalModelLoaderInfo> list;
    private final List<ResourceLocation> dependencies;
    private final Map<String, Either<RenderMaterial, String>> materialsFromString = new HashMap<>();

    public SignalCustomModel(final SignalAngel angel, final List<SignalModelLoaderInfo> list) {
        super();
        this.angel = angel;
        this.list = list;
        this.dependencies = list.stream()
                .map(info -> new ResourceLocation(OpenSignalsMain.MODID, "block/" + info.name))
                .collect(Collectors.toList());
        list.forEach(info -> info.retexture
                .forEach((id, texture) -> materialsFromString.computeIfAbsent(texture,
                        _u -> Either.left(new RenderMaterial(PlayerContainer.BLOCK_ATLAS,
                                new ResourceLocation(texture))))));
    }

    private static void transform(final BakedQuad quad, final Matrix4f quaterion) {
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
            final ModelBakery bakery, final ResourceLocation location,
            final Function<RenderMaterial, TextureAtlasSprite> function,
            final Map<String, Either<RenderMaterial, String>> material, final Quaternion rotation) {
        final TransformationMatrix transformation = new TransformationMatrix(
                new Vector3f(info.x, info.y, info.z), null, null, null);
        final BlockModel blockModel = (BlockModel) info.model;
        final ImmutableMap<String, Either<RenderMaterial, String>> defaultMap = ImmutableMap
                .copyOf(blockModel.textureMap);
        info.retexture.forEach((id, texture) -> blockModel.textureMap.computeIfPresent(id,
                (_u, old) -> material.get(texture)));
        final IBakedModel model = info.model.bake(bakery, function,
                new SimpleModelTransform(transformation), location);
        blockModel.textureMap.putAll(defaultMap);
        final Matrix4f reverse = new Matrix4f();
        reverse.setIdentity();
        reverse.setTranslation(-0.5f, 0, -0.5f);

        final Matrix4f matrix = Matrix4f.createScaleMatrix(1, 1, 1);
        matrix.setTranslation(0.5f, 0, 0.5f);
        matrix.multiply(rotation);
        matrix.multiply(reverse);

        model.getQuads(null, null, RANDOM, EmptyModelData.INSTANCE)
                .forEach(quad -> transform(quad, matrix));
        for (final Direction direction : Direction.values()) {
            model.getQuads(null, direction, RANDOM, EmptyModelData.INSTANCE)
                    .forEach(quad -> transform(quad, matrix));
        }
        return new BakedModelPair(info.state, model);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Collection<RenderMaterial> getMaterials(
            final Function<ResourceLocation, IUnbakedModel> function,
            final Set<Pair<String, String>> modelState) {
        final Collection<RenderMaterial> material = new ArrayList<>();
        this.dependencies.forEach(location -> material
                .addAll(function.apply(location).getMaterials(function, modelState)));
        materialsFromString.values().stream().map(either -> either.left())
                .filter(Optional::isPresent).forEach(opt -> material.add(opt.get()));
        return material;
    }

    @Override
    public IBakedModel bake(final ModelBakery bakery,
            final Function<RenderMaterial, TextureAtlasSprite> function, final IModelTransform modelTransform,
            final ResourceLocation resource) {
        list.forEach(info -> {
            if (info.model == null) {
                final ResourceLocation location = new ResourceLocation(OpenSignalsMain.MODID,
                        "block/" + info.name);
                if (bakery instanceof ModelLoader) {
                    info.model = ((ModelLoader) bakery).getModelOrLogError(location,
                            String.format("Could not find %s!", location));
                } else {
                    info.model = bakery.getModel(location);
                }
            }
        });
        final Quaternion quaternion = angel.getQuaternion();
        return new SignalBakedModel(list.stream().map(info -> transform(info, bakery, resource,
                function, materialsFromString, quaternion)).collect(Collectors.toList()));
    }
}