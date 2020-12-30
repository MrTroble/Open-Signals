package net.gir.girsignals.init;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.items.Linkingtool;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class Items {

	public static final Linkingtool linking_tool = new Linkingtool();

	public static void ItemInit() {
		setItemName(linking_tool, "linkingtool");
	}

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registry.register(linking_tool);
	}

	public static void setItemName(Item item, String name) {
		item.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
		item.setUnlocalizedName(name);
	}

}
