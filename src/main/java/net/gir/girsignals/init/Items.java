package net.gir.girsignals.init;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.items.hvsignalcontrolleritem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class Items {
	
	public static final hvsignalcontrolleritem hv_signal_controller_item = new hvsignalcontrolleritem();
	
	public static void ItemInit() {
		setItemName(hv_signal_controller_item, "hvsignalcontrolleritem");
	}
	
	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registry.register(hv_signal_controller_item);
	}
	
	public static void setItemName(Item item, String name) {
		item.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
		item.setUnlocalizedName(name);
	}

}
