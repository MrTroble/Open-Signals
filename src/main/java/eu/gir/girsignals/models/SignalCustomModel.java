package eu.gir.girsignals.models;

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
import eu.gir.girsignals.blocks.SignalBlock.SignalAngel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SignalCustomModel implements IModel {

	private ArrayList<ResourceLocation> textures = new ArrayList<>();
	private HashMap<Predicate<IExtendedBlockState>, Pair<IModel, Vector3f>> modelCache = new HashMap<>();
	private IBakedModel cachedModel = null;
	private SignalAngel angel = SignalAngel.ANGEL0;
	FaceBakery faceBakery = new FaceBakery();

	public SignalCustomModel(Consumer<SignalCustomModel> init, SignalAngel facing) {
		init.accept(this);
		this.angel = facing;
	}

	protected BakedQuad makeBakedQuad(BlockPartRotation rot, BlockPart bp, BlockPartFace bpf, TextureAtlasSprite tas,
			EnumFacing face, ITransformation transform, boolean flag) {
		return this.faceBakery.makeBakedQuad(bp.positionFrom, bp.positionTo, bpf, tas, face, transform, rot, flag,
				bp.shade);
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		if (cachedModel == null) {
			final MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
			modelCache.forEach((pr, m) -> {
				final IModel model = m.first();
				final ModelBlock mdl = model.asVanillaModel().orElse(null);
				Vector3f f = m.second();
				f.x += angel.getX();
				f.z += angel.getY();
				final TRSRTransformation baseState = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(f, null, null, null));

				if (mdl != null) {
					if (mdl.getElements().isEmpty())
						return;
					final SimpleBakedModel.Builder buil = new SimpleBakedModel.Builder(mdl, mdl.createOverrides());
					final String part = mdl.getElements().get(0).mapFaces.values().iterator().next().texture;
					buil.setTexture(bakedTextureGetter.apply(new ResourceLocation(mdl.resolveTextureName(part))));

					mdl.getElements().forEach(bp -> {
						bp.mapFaces.forEach((face, bpf) -> {
							final TextureAtlasSprite tas = bakedTextureGetter
									.apply(new ResourceLocation(mdl.resolveTextureName(bpf.texture)));
							buil.setTexture(tas);
							BlockPartRotation prt = new BlockPartRotation(new org.lwjgl.util.vector.Vector3f(), Axis.Y,
									angel.getAngel(), false);
							if (bp.partRotation != null)
								prt = new BlockPartRotation(bp.partRotation.origin, Axis.Y, angel.getAngel(), false);
							buil.addGeneralQuad(makeBakedQuad(prt, bp, bpf, tas, face, baseState, false));
						});
					});
					build.putModel(blockstate -> pr.test((IExtendedBlockState) blockstate), buil.makeBakedModel());
					return;
				}

				build.putModel(blockstate -> pr.test((IExtendedBlockState) blockstate), model.bake(ms -> {
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

	protected void register(String name, Predicate<IExtendedBlockState> state, float yOffset) {
		this.register(name, state, 0, yOffset, 0);
	}

	protected void register(String name, Predicate<IExtendedBlockState> state, float yOffset, String... strings) {
		this.register(name, state, 0, yOffset, 0, strings);
	}

	protected void register(String name, Predicate<IExtendedBlockState> state, float x, float y, float z, @Nullable String... strings) {
		IModel m = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
				"Couldn't find " + name);
		m = m.smoothLighting(false);
		if (strings != null && strings.length > 0) {
			Builder<String, String> build = ImmutableMap.builder();
			for (int i = 0; i < (int) Math.floor(strings.length / 2); i++)
				build.put(strings[i * 2], strings[i * 2 + 1]);
			m = m.retexture(build.build());
		}
		m.getTextures().stream().filter(rs -> !textures.contains(rs)).forEach(textures::add);
		modelCache.put(state, Pair.of(m, new Vector3f(x, y, z)));
	}
}
