package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.vecmath.AxisAngle4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.IForgeModelState;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements IModel {

    private final HashMap<Predicate<IModelData>, Pair<IModelData, Vector3f>> modelCache = new HashMap<>();
    private List<ResourceLocation> textures = new ArrayList<>();
    private MultiPartBakedModel cachedModel = null;
    private SignalAngel angel = SignalAngel.ANGEL0;
    private final Matrix4f rotation;

    public SignalCustomModel(final Consumer<SignalCustomModel> init, final SignalAngel facing) {
        init.accept(this);
        this.textures = ImmutableList.copyOf(textures);
        this.angel = facing;
        final Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setRotation(new AxisAngle4f(0, 1, 0, (float) angel.getRadians()));
        rotation = mat;
    }

    private Vector3f multiply(final Vector3f vec, final Matrix4f mat) {
        return new Vector3f(
                vec.x() * mat.getM00() + vec.y() * mat.getM01() + vec.z() * mat.getM02()
                        + mat.getM03(), //
                vec.x() * mat.getM10() + vec.y() * mat.getM11() + vec.z() * mat.getM12()
                        + mat.getM13(), //
                vec.x() * mat.getM20() + vec.y() * mat.getM21() + vec.z() * mat.getM22()
                        + mat.getM23());
    }

    private BakedQuad transform(final BakedQuad quad) {
        final int[] data = quad.getVertices();
        final VertexFormat format = quad.getFormat();
        for (int i = 0; i < data.length - 3; i += format.getIntegerSize()) {
            final Vector3f vector = new Vector3f(Float.intBitsToFloat(data[i]) - 0.5f,
                    Float.intBitsToFloat(data[i + 1]), Float.intBitsToFloat(data[i + 2]) - 0.5f);
            final Vector3f out = multiply(vector, rotation);
            data[i] = Float.floatToRawIntBits(out.x + 0.5f);
            data[i + 1] = Float.floatToRawIntBits(out.y);
            data[i + 2] = Float.floatToRawIntBits(out.z + 0.5f);
        }
        return quad;
    }

    @SuppressWarnings("deprecation")
    private MultiPartBakedModel transform(final MultiPartBakedModel model) {
        final com.google.common.collect.ImmutableList.Builder<BakedQuad> outgoing = ImmutableList
                .builder();
        for (final BakedQuad quad : model.getQuads(null, null, 0)) {
            outgoing.add(transform(quad));
        }
        final com.google.common.collect.ImmutableMap.Builder<Direction, List<BakedQuad>> faceOutgoing = ImmutableMap
                .builder();
        for (final Direction face : Direction.values()) {
            final com.google.common.collect.ImmutableList.Builder<BakedQuad> current = ImmutableList
                    .builder();
            for (final BakedQuad quad : model.getQuads(null, face, 0)) {
                current.add(transform(quad));
            }
            faceOutgoing.put(face, current.build());
        }
        return new MultiPartBakedModel(outgoing.build(), faceOutgoing.build(),
                model.useAmbientOcclusion(), model.isGui3d(), model.getParticleIcon(),
                model.getTransforms(), model.getOverrides());
    }

    @Override
    public MultiPartBakedModel bake(final IForgeModelState state, final VertexFormat format,
            final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (cachedModel == null) {
            final MultiPartBakedModel.Builder build = new MultiPartBakedModel.Builder();
            modelCache.forEach((pr, m) -> {
                final IModelData model = m.getFirst();
                final Vector3f f = m.getSecond();
                final TRSRTransformation baseState = new TRSRTransformation(f, null, null, null);
                build.putModel(blockstate -> pr.test((IModelData) blockstate),
                        transform(model.bake(baseState, format, bakedTextureGetter)));
            });
            return cachedModel = build.makeMultipartModel();
        }
        return cachedModel;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return textures;
    }

    protected void register(final String name, final Predicate<IModelData> state, final float x,
            final float y, final float z, final Map<String, String> map) {

        IModel m = ModelLoaderRegistry.getModelOrLogError(
                new ResourceLocation(OpenSignalsMain.MODID, "block/" + name),
                "Couldn't find " + name);
        m = m.smoothLighting(false);

        if (map != null && !map.isEmpty()) {
            final Builder<String, String> build = ImmutableMap.builder();
            for (final Map.Entry<String, String> entry : map.entrySet())
                build.put(entry.getKey(), entry.getValue());

            m = m.retexture(build.build());
        }

        m.getTextures().stream().filter(rs -> !textures.contains(rs)).forEach(textures::add);
        modelCache.put(state, Pair.of(m, new Vector3f(x, y, z)));
    }

    @SuppressWarnings("unchecked")
    protected void loadExtention(final TextureStats texturestate,
            final Map<String, ModelExtention> extention, final String modelname,
            final ModelStats states, final Models models, final FunctionParsingInfo info) {

        final String blockstate = texturestate.getBlockstate();

        final Map<String, String> retexture = texturestate.getRetextures();

        for (final Map.Entry<String, Map<String, String>> extentions : texturestate.getExtentions()
                .entrySet()) {

            final String extentionName = extentions.getKey();
            final Map<String, String> extentionProperties = extentions.getValue();
            final ModelExtention extentionValues = extention.get(extentionName);
            if (extentionValues == null)
                throw new ContentPackException(String
                        .format("There doesn't exists an extention named [%s]!", extentionName));

            for (final Map.Entry<String, String> entry : extentionValues.getExtention()
                    .entrySet()) {

                final String enumValue = entry.getKey();
                final String retextureValue = entry.getValue();

                for (final Map.Entry<String, String> extProperties : extentionProperties
                        .entrySet()) {
                    final String seProperty = extProperties.getKey();
                    final String retextureKey = extProperties.getValue();

                    final boolean load = texturestate.appendExtention(seProperty, enumValue,
                            retextureKey, retextureValue);

                    if (load) {

                        try {
                            this.register(modelname,
                                    LogicParser.predicate(texturestate.getBlockstate(), info),
                                    models.getX(texturestate.getOffsetX()),
                                    models.getY(texturestate.getOffsetY()),
                                    models.getZ(texturestate.getOffsetZ()),
                                    states.createRetexture(texturestate.getRetextures()));
                        } catch (final LogicalParserException e) {
                            OpenSignalsMain.log
                                    .error("There was an problem during loading an extention into "
                                            + modelname + " with the blockstate '"
                                            + texturestate.getBlockstate() + "'!");
                            e.printStackTrace();
                            return;
                        }
                        texturestate.resetStates(blockstate, retexture);
                    }
                }
            }

        }
    }
}