package net.gir.girsignals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import net.gir.girsignals.GirsignalsMain;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class SignalCustomModel implements IModel {

	private ArrayList<ResourceLocation> textures = new ArrayList<>();
	private HashMap<Predicate<IExtendedBlockState>, IModel> modelCache = new HashMap<>();
	private IBakedModel cachedModel = null;

	public SignalCustomModel() {
		reg("hv_base", bs -> true);
		reg("hv_hp", bs -> true);
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		if (cachedModel == null) {
			MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
			modelCache.forEach((pr, m) -> build.putModel(br -> pr.test((IExtendedBlockState) br),
					m.bake(state, format, bakedTextureGetter)));
			return cachedModel = build.makeMultipartModel();
		}
		return cachedModel;
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		System.out.println(textures);
		return ImmutableList.copyOf(textures);
	}

	private void reg(String name, Predicate<IExtendedBlockState> state) {
		IModel m = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(GirsignalsMain.MODID, "block/" + name),
				"Couldn't find " + name);
		textures.addAll(m.getTextures());
		modelCache.put(state, m);
	}
}
