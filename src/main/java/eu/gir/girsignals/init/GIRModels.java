package eu.gir.girsignals.init;

import eu.gir.girsignals.blocks.Signal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GIRModels {
	
	@SubscribeEvent
	public static void register(ModelRegistryEvent event) {
		GIRItems.registeredItems.forEach(GIRModels::registerModel);
		registerModel(Item.getItemFromBlock(GIRBlocks.HV_SIGNAL_CONTROLLER));
		registerModel(Item.getItemFromBlock(GIRBlocks.POST));
		registerModel(Item.getItemFromBlock(GIRBlocks.SIGNAL_BOX));
		registerModel(Item.getItemFromBlock(GIRBlocks.REDSTONE_IN));
		registerModel(Item.getItemFromBlock(GIRBlocks.REDSTONE_OUT));
	}
	
	@SubscribeEvent
	public static void addColor(ColorHandlerEvent.Block event) {
		BlockColors colors = event.getBlockColors();
		GIRBlocks.blocksToRegister.forEach(block -> {
			if (block instanceof Signal) {
				Signal sb = (Signal) block;
				if (sb.hasCostumColor())
					colors.registerBlockColorHandler(sb::colorMultiplier, block);
			}
		});
	}
	
	private static void registerModel(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
