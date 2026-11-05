package com.troblecodings.signals.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.mojang.math.Transformation;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SignalAngel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements UnbakedModel {

    private static final Map<ResourceLocation, BakedModel> LOCATION_TO_MODEL = new HashMap<>();

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
            // JOML: Vector4f.mul(Matrix4f) replaces Mojang's Vector4f.transform(Matrix4f).
            vector.mul(quaterion);
            oldVertex[i + 0] = Float.floatToIntBits(vector.x());
            oldVertex[i + 1] = Float.floatToIntBits(vector.y());
            oldVertex[i + 2] = Float.floatToIntBits(vector.z());

            String texName = quad.getSprite().contents().name().toString();
            if (texName.contains("lamp_")) {
                oldVertex[i + 6] = 15728880;
            } else if (texName.contains("reflection_")) {
                oldVertex[i + 6] = (int) (15728880 * 0.5f);
            }
        }
    }

    private BakedModelPair transform(final SignalModelLoaderInfo info, final ModelBaker baker,
            final ResourceLocation location, final Function<Material, TextureAtlasSprite> function,
            final Map<String, Either<Material, String>> material, final Quaternionf rotation) {
        final Transformation transformation =
                new Transformation(new Vector3f(info.x, info.y, info.z), null, null, null);
        final BlockModel blockModel = (BlockModel) info.model;
        final ImmutableMap<String, Either<Material, String>> defaultMap =
                ImmutableMap.copyOf(blockModel.textureMap);
        info.retexture.forEach((id, texture) -> blockModel.textureMap.computeIfPresent(id,
                (_u, old) -> material.get(texture)));
        final BakedModel model =
                info.model.bake(baker, function, new SimpleModelState(transformation), location);
        blockModel.textureMap.putAll(defaultMap);

        // Rotate about the centre of the block: T(+0.5) * R * T(-0.5). JOML's translate/rotate
        // post-multiply just like Mojang's multiplyWithTranslation/multiply did, so the order of
        // the calls carries over unchanged.
        final Matrix4f matrix = new Matrix4f();
        matrix.translate(0.5f, 0, 0.5f);
        matrix.rotate(rotation);
        matrix.translate(-0.5f, 0, -0.5f);

        model.getQuads(null, null, RANDOM, ModelData.EMPTY, null)
                .forEach(quad -> transform(quad, matrix));
        for (final Direction direction : Direction.values()) {
            model.getQuads(null, direction, RANDOM, ModelData.EMPTY, null)
                    .forEach(quad -> transform(quad, matrix));
        }

        if (angel.equals(SignalAngel.ANGEL0) && info.isAnimation) {
            LOCATION_TO_MODEL.put(new ResourceLocation(OpenSignalsMain.MODID, info.name), model);
        }
        return new BakedModelPair(info.state, model);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.dependencies;
    }

    /**
     * Replaces getMaterials, which 1.19.3 removed. Texture gathering now happens through the atlas
     * sprite sources (see assets/minecraft/atlases/blocks.json) rather than being reported by each
     * model, so this only has to resolve parents.
     */
    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> function) {
        this.dependencies.forEach(location -> {
            final UnbakedModel model = function.apply(location);
            if (model != null)
                model.resolveParents(function);
        });
    }

    /**
     * The returned model bakes its parts on first use rather than here: this runs during
     * ModelEvent.ModifyBakingResult, before the texture atlases have been uploaded, so no sprites
     * are available yet. See {@link SignalBakedModel}.
     */
    @Override
    public BakedModel bake(final ModelBaker baker,
            final Function<Material, TextureAtlasSprite> function, final ModelState state,
            final ResourceLocation resource) {
        return new SignalBakedModel(() -> {
            list.forEach(info -> {
                if (info.model == null) {
                    final ResourceLocation location =
                            new ResourceLocation(OpenSignalsMain.MODID, "block/" + info.name);
                    info.model = baker.getModel(location);
                }
            });
            final Quaternionf quaternion = angel.getQuaternion();
            return list.stream()
                    .map(info -> transform(info, baker, resource, function, materialsFromString,
                            quaternion))
                    .collect(Collectors.toUnmodifiableList());
        });
    }

    public static BakedModel getModelFromLocation(final ResourceLocation location) {
        return LOCATION_TO_MODEL.getOrDefault(location,
                Minecraft.getInstance().getModelManager().getMissingModel());
    }
}