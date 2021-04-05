package eu.gir.girsignals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.items.Linkingtool;
import eu.gir.girsignals.items.Placementtool;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class GIRItems {

	public static final Linkingtool LINKING_TOOL = new Linkingtool();
	public static final Placementtool PLACEMENT_TOOL = new Placementtool();
	public static final Placementtool SIGN_PLACEMENT_TOOL = new Placementtool();
	
	public static ArrayList<Item> registeredItems = new ArrayList<>();
	
	public static void init() {
		Field[] fields = GIRItems.class.getFields();
		for(Field field : fields) {
			int modifiers = field.getModifiers();
			if(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers)) {
				String name = field.getName().toLowerCase().replace("_", "");
				try {
					Item item = (Item) field.get(null);
					item.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
					item.setUnlocalizedName(name);
					registeredItems.add(item);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registeredItems.forEach(registry::register);
	}

}
