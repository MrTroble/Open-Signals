package eu.gir.girsignals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.GhostBlock;
import eu.gir.girsignals.blocks.SignalController;
import eu.gir.girsignals.blocks.SignalHL;
import eu.gir.girsignals.blocks.SignalHV;
import eu.gir.girsignals.blocks.SignalKS;
import eu.gir.girsignals.blocks.SignalLF;
import eu.gir.girsignals.blocks.SignalSHLight;
import eu.gir.girsignals.blocks.SignalTram;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class GIRBlocks {

	public static final SignalController HV_SIGNAL_CONTROLLER = new SignalController();
	public static final SignalHV HV_SIGNAL = new SignalHV();
	public static final GhostBlock GHOST_BLOCK = new GhostBlock();
	public static final SignalKS KS_SIGNAL = new SignalKS();
	public static final SignalHL HL_SIGNAL = new SignalHL();
	public static final SignalSHLight SH_LIGHT = new SignalSHLight();
	public static final SignalTram TRAM_SIGNAL = new SignalTram();
	public static final SignalLF LF_SIGNAL = new SignalLF();

	private static ArrayList<Block> blocksToRegister = new ArrayList<>();

	public static void init() {
		Field[] fields = GIRBlocks.class.getFields();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers)) {
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
	public static void addColor(ColorHandlerEvent.Block event) {
		BlockColors colors = event.getBlockColors();
		blocksToRegister.forEach(block -> {
			if(block instanceof IBlockColor)
				colors.registerBlockColorHandler((IBlockColor)block, block);
		});
	}
	
	@SubscribeEvent
	public static void registerBlock(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		blocksToRegister.forEach(registry::register);
	}

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		blocksToRegister
				.forEach(block -> registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName())));
	}
}
