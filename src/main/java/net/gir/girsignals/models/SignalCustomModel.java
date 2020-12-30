package net.gir.girsignals.models;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

public class SignalCustomModel implements IModel {

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		MultipartBakedModel.Builder build = new MultipartBakedModel.Builder();
		build.putModel(null, Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(null));
		return build.makeMultipartModel();
	}
	
}
