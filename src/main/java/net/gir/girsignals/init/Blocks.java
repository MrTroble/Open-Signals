package net.gir.girsignals.init;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.controllers.HVSignalController;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class Blocks {
	
	public static final HVSignalController HV_SIGNAL_CONTROLLER = new HVSignalController();
	
	public static void init() {
		setBlockName(HV_SIGNAL_CONTROLLER, "HV Signal-Controller");
	}
	
	@SubscribeEvent
	public static void registerBlock(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		registry.register(HV_SIGNAL_CONTROLLER);
	}
	
	public static void setBlockName(Block block, String name) {
		block.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
		block.setUnlocalizedName(name);
	}
}
