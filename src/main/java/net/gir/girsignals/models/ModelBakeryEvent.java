package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Supplier;

import net.gir.girsignals.GirsignalsMain;
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
	
	private static HashMap<String, Supplier<IModel>> registeredModels = new HashMap<>(); 
	
	@SubscribeEvent
	public void onModelBakeStart(ModelBakeEvent event) {
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		registeredModels.clear();
		registeredModels.put("hvsignal", SignalCustomModel::new);
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if(!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID)) return false;
		return registeredModels.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return registeredModels.get(modelLocation.getResourcePath()).get();
	}

}
