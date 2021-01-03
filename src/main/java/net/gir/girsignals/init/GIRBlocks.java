package net.gir.girsignals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.GhostBlock;
import net.gir.girsignals.blocks.SignalHV;
import net.gir.girsignals.controllers.SignalController;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class GIRBlocks {

	public static final SignalController HV_SIGNAL_CONTROLLER = new SignalController();
	public static final SignalHV HV_SIGNAL = new SignalHV();
	public static final GhostBlock GHOST_BLOCK = new GhostBlock();
	
	private static ArrayList<Block> blocksToRegister = new ArrayList<>();
	
	public static void init() {
		Field[] fields = GIRBlocks.class.getFields();
		for(Field field : fields) {
			int modifiers = field.getModifiers();
			if(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers)) {
				String name = field.getName().toLowerCase().replace("_", "");
				try {
					Block block = (Block) field.get(null);
					block.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
					block.setUnlocalizedName(name);
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
}
