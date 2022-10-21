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
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.realmsclient.util.Pair;
import com.troblecodings.signals.GirsignalsMain;
import com.troblecodings.signals.blocks.Signal.SignalAngel;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.models.parser.LogicalParserException;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SignalCustomModel implements IModel {

    private final HashMap<Predicate<IExtendedBlockState>, Pair<IModel, Vector3f>> modelCache = new HashMap<>();
    private List<ResourceLocation> textures = new ArrayList<>();
    private IBakedModel cachedModel = null;
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
                vec.getX() * mat.getM00() + vec.getY() * mat.getM01() + vec.getZ() * mat.getM02()
                        + mat.getM03(), //
                vec.getX() * mat.getM10() + vec.getY() * mat.getM11() + vec.getZ() * mat.getM12()
                        + mat.getM13(), //
                vec.getX() * mat.getM20() + vec.getY() * mat.getM21() + vec.getZ() * mat.getM22()
                        + mat.getM23());
    }

    private BakedQuad transform(final BakedQuad quad) {
        final int[] data = quad.getVertexData();
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
    private IBakedModel transform(final IBakedModel model) {
        final com.google.common.collect.ImmutableList.Builder<BakedQuad> outgoing = ImmutableList
                .builder();
        for (final BakedQuad quad : model.getQuads(null, null, 0)) {
            outgoing.add(transform(quad));
        }
        final com.google.common.collect.ImmutableMap.Builder<EnumFacing, List<BakedQuad>> faceOutgoing = ImmutableMap
                .builder();
        for (final EnumFacing face : EnumFacing.VALUES) {
            final com.google.common.collect.ImmutableList.Builder<BakedQuad> current = ImmutableList
                    .builder();
            for (final BakedQuad quad : model.getQuads(null, face, 0)) {
                current.add(transform(quad));
            }
            faceOutgoing.put(face, current.build());
        }
        return new SimpleBakedModel(outgoing.build(), faceOutgoing.build(),
                model.isAmbientOcclusion(), model.isGui3d(), model.getParticleTexture(),
                model.getItemCameraTransforms(), model.getOverrides());
    }

    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format,
            final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (cachedModel == null) {
            final MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
            modelCache.forEach((pr, m) -> {
                final IModel model = m.first();
                final Vector3f f = m.second();
                final TRSRTransformation baseState = new TRSRTransformation(f, null, null, null);
                build.putModel(blockstate -> pr.test((IExtendedBlockState) blockstate),
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

    protected void register(final String name, final Predicate<IExtendedBlockState> state,
            final float x, final float y, final float z, final Map<String, String> map) {

        IModel m = ModelLoaderRegistry.getModelOrLogError(
                new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
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
            final ModelStats states, final Models models, final FunctionParsingInfo signaltype) {

        final String blockstate = texturestate.getBlockstate();

        final Map<String, String> retexture = texturestate.getRetextures();

        for (final Map.Entry<String, ModelExtention> extentionvalues : extention.entrySet()) {

            for (final Map.Entry<String, Map<String, String>> modelExtentions : texturestate
                    .getExtentions().entrySet()) {

                final String extentionInFilename = modelExtentions.getKey();
                final Map<String, String> extentionprops = modelExtentions.getValue();

                if (extentionInFilename.equalsIgnoreCase(extentionvalues.getKey())) {

                    for (final Map.Entry<String, String> entry : extentionvalues.getValue()
                            .getExtention().entrySet()) {

                        final String enumValue = entry.getKey();
                        final String retextureValue = entry.getValue();

                        for (final Map.Entry<String, String> extentionProperties : extentionprops
                                .entrySet()) {
                            final String seProperty = extentionProperties.getKey();
                            final String retexturekey = extentionProperties.getValue();

                            final boolean load = texturestate.appendExtention(seProperty, enumValue,
                                    retexturekey, retextureValue);

                            if (load) {

                                try {

                                    this.register(modelname,
                                            LogicParser.predicate(texturestate.getBlockstate(),
                                                    signaltype),
                                            models.getX(texturestate.getOffsetX()),
                                            models.getY(texturestate.getOffsetY()),
                                            models.getZ(texturestate.getOffsetZ()),
                                            states.createRetexture(texturestate.getRetextures()));

                                } catch (final LogicalParserException e) {
                                    GirsignalsMain.log.error(
                                            "There was an problem during loading an extention into "
                                                    + modelname + " with the blockstate '"
                                                    + texturestate.getBlockstate() + "'!");
                                    e.printStackTrace();
                                }

                                texturestate.resetStates(blockstate, retexture);
                            }
                        }
                    }
                }
            }
        }
    }
}