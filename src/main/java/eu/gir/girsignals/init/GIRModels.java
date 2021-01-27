package eu.gir.girsignals.init;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GIRModels {

	@SubscribeEvent
	public static void register(ModelRegistryEvent event) {
		GIRItems.registeredItems.forEach(GIRModels::registerModel);
		registerModel(Item.getItemFromBlock(GIRBlocks.HV_SIGNAL_CONTROLLER));
	}

	private static void registerModel(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0,
				new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
