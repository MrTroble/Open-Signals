package net.gir.girsignals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.util.Pair;

import net.gir.girsignals.GirsignalsMain;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
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

public class SignalCustomModel implements IModel {

	private ArrayList<ResourceLocation> textures = new ArrayList<>();
	private HashMap<Predicate<IExtendedBlockState>, Pair<IModel, Float>> modelCache = new HashMap<>();
	private IBakedModel cachedModel = null;
	private EnumFacing facing = EnumFacing.NORTH;
	
	public SignalCustomModel(Consumer<SignalCustomModel> init, EnumFacing facing) {
		init.accept(this);
		this.facing = facing;
	}	
	
	@SuppressWarnings("deprecation")
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		if (cachedModel == null) {
			MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
			modelCache.forEach((pr, m) -> build.putModel(br -> pr.test((IExtendedBlockState) br),
					m.first().bake(part -> {
						Optional<TRSRTransformation> trs = state.apply(part);
						if(trs.isPresent()) {
							TRSRTransformation trsr = trs.get();
							ItemTransformVec3f itf = trsr.toItemTransform();
							itf.translation.y += m.second();
							if(facing != EnumFacing.NORTH)
								itf.rotation.y += facing.getHorizontalAngle();
							trsr = TRSRTransformation.from(itf);
							return Optional.of(trsr);
						}
						return Optional.empty();
					}, format, bakedTextureGetter)));
			return cachedModel = build.makeMultipartModel();
		}
		return cachedModel;
	}
	
	@Override
	public IModelState getDefaultState() {
		if(facing != EnumFacing.NORTH)
			return TRSRTransformation.from(facing);
		return TRSRTransformation.identity();
	}
	
	@Override
	public Collection<ResourceLocation> getTextures() {
		return ImmutableList.copyOf(textures);
	}

	protected void register(String name, Predicate<IExtendedBlockState> state, float yOffset) {
		IModel m = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
				"Couldn't find " + name);
		textures.addAll(m.getTextures());
		modelCache.put(state, Pair.of(m, yOffset));
	}
}
