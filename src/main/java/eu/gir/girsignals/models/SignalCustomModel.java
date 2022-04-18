package eu.gir.girsignals.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.realmsclient.util.Pair;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal.SignalAngel;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing.Axis;
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

    private final ArrayList<ResourceLocation> textures = new ArrayList<>();
    private final HashMap<Predicate<IExtendedBlockState>, Pair<IModel, Vector3f>> modelCache = new HashMap<>();
    private IBakedModel cachedModel = null;
    private SignalAngel angel = SignalAngel.ANGEL0;

    private static Field rotationField;
    static {
        for (final Field fdl : BlockPart.class.getFields()) {
            if (fdl.getType().equals(BlockPartRotation.class)) {
                fdl.setAccessible(true);
                rotationField = fdl;
            }
        }
    }

    public SignalCustomModel(final Consumer<SignalCustomModel> init, final SignalAngel facing) {
        init.accept(this);
        this.angel = facing;
    }

    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format,
            final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (cachedModel == null) {
            final MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
            modelCache.forEach((pr, m) -> {
                final IModel model = m.first();
                final ModelBlock mdl = model.asVanillaModel().orElse(null);
                final Vector3f f = m.second();
                final TRSRTransformation baseState = TRSRTransformation
                        .blockCenterToCorner(new TRSRTransformation(f, null, null, null));

                if (mdl != null) {
                    mdl.getElements().forEach(bp -> {
                        final BlockPartRotation prt = bp.partRotation == null
                                ? new BlockPartRotation(
                                        new org.lwjgl.util.vector.Vector3f(0.5f, 0.5f, 0.5f),
                                        Axis.Y, angel.getAngel(), false)
                                : new BlockPartRotation(bp.partRotation.origin, Axis.Y,
                                        angel.getAngel(), false);
                        try {
                            rotationField.set(bp, prt);
                        } catch (final IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                }

                build.putModel(blockstate -> pr.test((IExtendedBlockState) blockstate),
                        model.bake(ms -> {
                            if (ms.isPresent())
                                return Optional.empty();
                            return Optional.of(baseState);
                        }, format, bakedTextureGetter));
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
        return ImmutableList.copyOf(textures);
    }

    protected void register(final String name, final Predicate<IExtendedBlockState> state,
            final float yOffset) {
        this.register(name, state, 0, yOffset, 0);
    }

    protected void register(final String name, final Predicate<IExtendedBlockState> state,
            final float yOffset, final String... strings) {
        this.register(name, state, 0, yOffset, 0, strings);
    }

    protected void register(final String name, final Predicate<IExtendedBlockState> state,
            final float x, final float y, final float z, @Nullable final String... strings) {
        IModel m = ModelLoaderRegistry.getModelOrLogError(
                new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
                "Couldn't find " + name);
        m = m.smoothLighting(false);
        if (strings != null && strings.length > 0) {
            final Builder<String, String> build = ImmutableMap.builder();
            for (int i = 0; i < (int) Math.floor(strings.length / 2); i++)
                build.put(strings[i * 2], strings[i * 2 + 1]);
            m = m.retexture(build.build());
        }
        m.getTextures().stream().filter(rs -> !textures.contains(rs)).forEach(textures::add);
        modelCache.put(state, Pair.of(m, new Vector3f(x, y, z)));
    }
}
