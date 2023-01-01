package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.core.SignalAngel;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.IForgeModelState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements IModelGeometry<SignalCustomModel> {

	public static final Random RANDOM = new Random();
	
    private final HashMap<Predicate<IModelData>, Pair<IModelData, Vector3f>> modelCache = new HashMap<>();
    private List<Material> textures = new ArrayList<>();
    private MultiPartBakedModel cachedModel = null;
    private SignalAngel angel = SignalAngel.ANGEL0;
    private final Matrix4f rotation;

    public SignalCustomModel(final Consumer<SignalCustomModel> init, final SignalAngel facing) {
        init.accept(this);
        this.textures = ImmutableList.copyOf(textures);
        this.angel = facing;
        rotation = new Matrix4f(Quaternion.fromXYZ(0, (float) angel.getRadians(), 0));
    }

    private Vector3f multiply(final Vector3f vec, final Matrix4f mat) {
    	Vector4f vec4 = new Vector4f(vec);
    	vec4.transform(mat);
    	return new Vector3f(vec4);
    }

    private BakedQuad transform(final BakedQuad quad) {
        final int[] data = quad.getVertices();
        final VertexFormat format = DefaultVertexFormat.BLOCK;
        for (int i = 0; i < data.length - 3; i += format.getIntegerSize()) {
            final Vector3f vector = new Vector3f(Float.intBitsToFloat(data[i]) - 0.5f,
                    Float.intBitsToFloat(data[i + 1]), Float.intBitsToFloat(data[i + 2]) - 0.5f);
            final Vector3f out = multiply(vector, rotation);
            data[i] = Float.floatToRawIntBits(out.x() + 0.5f);
            data[i + 1] = Float.floatToRawIntBits(out.y());
            data[i + 2] = Float.floatToRawIntBits(out.z() + 0.5f);
        }
        return quad;
    }

    @SuppressWarnings("deprecation")
    private MultiPartBakedModel transform(final MultiPartBakedModel model) {
        final com.google.common.collect.ImmutableList.Builder<BakedQuad> outgoing = ImmutableList
                .builder();
        for (final BakedQuad quad : model.getQuads(null, null, RANDOM)) {
            outgoing.add(transform(quad));
        }
        final com.google.common.collect.ImmutableMap.Builder<Direction, List<BakedQuad>> faceOutgoing = ImmutableMap
                .builder();
        for (final Direction face : Direction.values()) {
            final com.google.common.collect.ImmutableList.Builder<BakedQuad> current = ImmutableList
                    .builder();
            for (final BakedQuad quad : model.getQuads(null, face, RANDOM)) {
                current.add(transform(quad));
            }
            faceOutgoing.put(face, current.build());
        }
        // TODO
        return null;
    }

    protected void register(final String name, final Predicate<IModelData> state, final float x,
            final float y, final float z, final Map<String, String> map) {

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

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, UnbakedModel> modelGetter,
            Set<Pair<String, String>> missingTextureErrors) {
        return null;
    }
}