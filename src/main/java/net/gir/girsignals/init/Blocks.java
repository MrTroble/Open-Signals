package net.gir.girsignals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.HVSignal;
import net.gir.girsignals.controllers.SignalController;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class Blocks {

	public static SignalController HV_SIGNAL_CONTROLLER = new SignalController();
	public static HVSignal HV_SIGNAL = new HVSignal();
	
	private static ArrayList<Block> blocksToRegister = new ArrayList<>();
	
	public static void init() {
		Field[] fields = Blocks.class.getFields();
		for(Field field : fields) {
			int modifiers = field.getModifiers();
			if(Modifier.isStatic(modifiers) && Modifier.isStatic(modifiers)) {
				String name = field.getName().toLowerCase().replace("_", "");
				try {
					Block block = (Block) field.get(null);
					setBlockName(block, name);
					blocksToRegister.add(block);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerBlock(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		blocksToRegister.forEach(registry::register);
	}

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		blocksToRegister.forEach(block -> registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName())));
	}

	private static void setBlockName(Block block, String name) {
		block.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
		block.setUnlocalizedName(name);
	}
}
