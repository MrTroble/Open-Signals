package net.gir.girsignals.models;

import java.util.function.Function;

import net.gir.girsignals.GirsignalsMain;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

public class SignalCustomModel implements IModel {

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		System.out.println("==============Bake==============");
		MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
		IModel m = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(GirsignalsMain.MODID, "block/hv_base"), "Couldn't find hv base");
		build.putModel(bs -> {
			System.out.println(bs.getClass().toGenericString());
			return true;
		}, m.bake(state, format, bakedTextureGetter));
		return build.makeMultipartModel();
	}
	
}
