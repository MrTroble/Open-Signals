package net.gir.girsignals.models;

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

import net.gir.girsignals.GirsignalsMain;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
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

	private ArrayList<ResourceLocation> textures = new ArrayList<>();
	private HashMap<Predicate<IExtendedBlockState>, Pair<IModel, Pair<Vector3f, Vector3f>>> modelCache = new HashMap<>();
	private IBakedModel cachedModel = null;
	private EnumFacing facing = EnumFacing.NORTH;

	public SignalCustomModel(Consumer<SignalCustomModel> init, EnumFacing facing) {
		init.accept(this);
		modelCache.entrySet().forEach(set -> set.getValue().first().getTextures().forEach(System.out::println));
		this.facing = facing;
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		if (cachedModel == null) {
			MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
			modelCache
					.forEach((pr, m) -> build.putModel(br -> pr.test((IExtendedBlockState) br), m.first().bake(part -> {
						Optional<TRSRTransformation> trs = state.apply(part);
						if (trs.isPresent()) {
							TRSRTransformation trsr = trs.get();
							net.minecraft.client.renderer.block.model.ItemTransformVec3f itf = trsr.toItemTransform();
							Vector3f vec = m.second().first();
							itf.translation.x += (facing != EnumFacing.SOUTH && facing != EnumFacing.NORTH)
									? ((facing == EnumFacing.WEST ? 1 : -1) * vec.z)
									: ((facing == EnumFacing.NORTH ? 1 : -1) * vec.x);
							itf.translation.y += vec.y;
							itf.translation.z += (facing != EnumFacing.SOUTH && facing != EnumFacing.NORTH)
									? ((facing == EnumFacing.WEST ? -1 : 1) * vec.x)
									: ((facing == EnumFacing.NORTH ? 1 : -1) * vec.z);

							Vector3f scale = m.second().second();
							itf.scale.x += scale.x;
							itf.scale.y += scale.y;
							itf.scale.z += scale.z;
							if (facing != EnumFacing.NORTH)
								itf.rotation.y += facing.getHorizontalAngle();
							trsr = TRSRTransformation.from(itf);
							return Optional.of(trsr);
						}
						return Optional.empty();
					}, format, bakedTextureGetter)));
			modelCache.clear();
			return cachedModel = build.makeMultipartModel();
		}
		return cachedModel;
	}

	@Override
	public IModelState getDefaultState() {
		if (facing != EnumFacing.NORTH)
			return TRSRTransformation.from(facing);
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

	protected void register(String name, Predicate<IExtendedBlockState> state, float x, float y, float z, String... strings) {
		this.register(name, state, x, y, z, 0, 0, 0, strings);
	}
	
	protected void register(String name, Predicate<IExtendedBlockState> state, float x, float y, float z) {
		this.register(name, state, x, y, z, 0, 0, 0);
	}

	protected void register(String name, Predicate<IExtendedBlockState> state, float x, float y, float z, float xs,
			float ys, float zs) {
		this.register(name, state, x, y, z, xs, ys, zs, (String[])null);
	}
	
	protected void register(String name, Predicate<IExtendedBlockState> state, float x, float y, float z, float xs,
			float ys, float zs, @Nullable String... strings) {
		IModel m = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
				"Couldn't find " + name);
		if (strings != null && strings.length > 0) {
			Builder<String, String> build = ImmutableMap.builder();
			for (int i = 0; i < (int) Math.floor(strings.length / 2); i++)
				build.put(strings[i * 2], strings[i * 2 + 1]);
			m = m.retexture(build.build());
		}
		m.getTextures().stream().filter(rs -> !textures.contains(rs)).forEach(textures::add);
		modelCache.put(state, Pair.of(m, Pair.of(new Vector3f(x, y, z), new Vector3f(xs, ys, zs))));
	}
}
