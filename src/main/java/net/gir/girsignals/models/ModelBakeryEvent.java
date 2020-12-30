package net.gir.girsignals.models;

import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT)
public class ModelBakeryEvent implements ICustomModelLoader {
	
	@SubscribeEvent
	public void onModelBakeStart(ModelBakeEvent event) {
		ModelManager manager = event.getModelManager();
		manager.getModel(new ModelResourceLocation(""));
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return false;
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return null;
	}

}
