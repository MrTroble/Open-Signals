package net.gir.girsignals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.util.Pair;

import net.gir.girsignals.GirsignalsMain;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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

	public SignalCustomModel() {
		reg("hv_base", bs -> true, 0);
		reg("hv_hp", bs -> true, 2);
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
	public Collection<ResourceLocation> getTextures() {
		return ImmutableList.copyOf(textures);
	}

	private void reg(String name, Predicate<IExtendedBlockState> state, float yOffset) {
		IModel m = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
				"Couldn't find " + name);
		System.out.println(m.getClass().toGenericString());
		textures.addAll(m.getTextures());
		modelCache.put(state, Pair.of(m, yOffset));
	}
}
