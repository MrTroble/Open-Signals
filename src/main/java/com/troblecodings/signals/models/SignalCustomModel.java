package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SignalAngel;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.SimpleModelState;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements UnbakedModel {

    @Nonnull
    public static final RandomSource RANDOM = RandomSource.create();

    private final SignalAngel angel;
    private final List<SignalModelLoaderInfo> list;
    private final List<ResourceLocation> dependencies;
    private final Map<String, Either<Material, String>> materialsFromString = new HashMap<>();

    public SignalCustomModel(final SignalAngel angel, final List<SignalModelLoaderInfo> list) {
        super();
        this.angel = angel;
        this.list = list;
        this.dependencies = list.stream()
                .map(info -> new ResourceLocation(OpenSignalsMain.MODID, "block/" + info.name))
                .collect(Collectors.toUnmodifiableList());
        list.forEach(info -> info.retexture.forEach(
                (id, texture) -> materialsFromString.computeIfAbsent(texture, _u -> Either.left(
                        new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(texture))))));
    }

    private static void transform(final BakedQuad quad, final Matrix4f quaterion) {
        final int[] oldVertex = quad.getVertices();

        final int size = DefaultVertexFormat.BLOCK.getIntegerSize();
        for (int i = 0; i < oldVertex.length; i += size) {
            final float x = Float.intBitsToFloat(oldVertex[i]);
            final float y = Float.intBitsToFloat(oldVertex[i + 1]);
            final float z = Float.intBitsToFloat(oldVertex[i + 2]);
            final Vector4f vector = new Vector4f(x, y, z, 1);
            quaterion.transform(vector);
            oldVertex[i + 0] = Float.floatToIntBits(vector.x());
            oldVertex[i + 1] = Float.floatToIntBits(vector.y());
            oldVertex[i + 2] = Float.floatToIntBits(vector.z());
        }
    }

    @SuppressWarnings("deprecation")
    private static BakedModelPair transform(final SignalModelLoaderInfo info,
            final ModelBakery bakery, final ResourceLocation location,
            final Function<Material, TextureAtlasSprite> function,
            final Map<String, Either<Material, String>> material, final Quaternionf rotation) {
        final Transformation transformation = new Transformation(
                new Vector3f(info.x, info.y, info.z), null, null, null);
        final BlockModel blockModel = (BlockModel) info.model;
        final ImmutableMap<String, Either<Material, String>> defaultMap = ImmutableMap
                .copyOf(blockModel.textureMap);
        info.retexture.forEach((id, texture) -> blockModel.textureMap.computeIfPresent(id,
                (_u, old) -> material.get(texture)));
        final BakedModel model = info.model.bake((ModelBaker) bakery, function,
                new SimpleModelState(transformation), location);
        blockModel.textureMap.putAll(defaultMap);
        final Matrix4f reverse = new Matrix4f();
        reverse.identity();
        reverse.translate(-0.5f, 0, -0.5f);
        
        final Matrix4f matrix = new Matrix4f();
        matrix.scale(1, 1, 1);
        matrix.translate(0.5f, 0, 0.5f);
        matrix.rotate(rotation);
        matrix.mul(reverse);

        model.getQuads(null, null, RANDOM)
                .forEach(quad -> transform(quad, matrix));
        for (final Direction direction : Direction.values()) {
            model.getQuads(null, direction, RANDOM)
                    .forEach(quad -> transform(quad, matrix));
        }
        return new BakedModelPair(info.state, model);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Collection<Material> getMaterials(
            final Function<ResourceLocation, UnbakedModel> function,
            final Set<Pair<String, String>> modelState) {
        final List<Material> material = new ArrayList<>();
        this.dependencies.forEach(location -> material
                .addAll(function.apply(location).getMaterials(function, modelState)));
        materialsFromString.values().stream().map(either -> either.left())
                .filter(Optional::isPresent).forEach(opt -> material.add(opt.get()));
        return material;
    }

    @Override
    public BakedModel bake(final ModelBaker bakery, final Function<Material, TextureAtlasSprite> function,
            final ModelState p_119536_, final ResourceLocation p_119537_) {
        list.forEach(info -> {
            if (info.model == null) {
                final ResourceLocation location = new ResourceLocation(OpenSignalsMain.MODID,
                        "block/" + info.name);
                info.model = bakery.getModel(location);
            }
        });
        final Quaternionf quaternion = angel.getQuaternion();
        return new SignalBakedModel(
                list.stream()
                        .map(info -> transform(info, bakery, resource, function,
                                materialsFromString, quaternion))
                        .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> p_119538_) {
        // TODO Auto-generated method stub
        
    }
}