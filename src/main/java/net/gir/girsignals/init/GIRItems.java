package net.gir.girsignals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.SignalHV;
import net.gir.girsignals.blocks.SignalTileEnity;
import net.gir.girsignals.items.Linkingtool;
import net.gir.girsignals.items.Placementtool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class GIRItems {

	public static final Linkingtool LINKING_TOOL = new Linkingtool();
	public static final Placementtool PLACEMENT_TOOL = new Placementtool();
	// TODO Remove in production
	public static final Item DEBUG_TOOL = new Item() {
		public EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
			IBlockState state = worldIn.getBlockState(pos);
			if(state.getBlock() instanceof SignalHV) {
				IExtendedBlockState ebs = (IExtendedBlockState) state.getBlock().getExtendedState(state, worldIn, pos);
				System.out.println("======== HP " + ebs.getValue(SignalHV.STOPSIGNAL));
				((SignalTileEnity)worldIn.getTileEntity(pos)).setProperty(SignalHV.STOPSIGNAL, HPVR.HPVR0);
				((SignalTileEnity)worldIn.getTileEntity(pos)).setProperty(SignalHV.DISTANTSIGNAL, HPVR.HPVR0);
				((SignalTileEnity)worldIn.getTileEntity(pos)).setProperty(SignalHV.ZS32, ZS32.B);
				((SignalTileEnity)worldIn.getTileEntity(pos)).setProperty(SignalHV.ZS32V, ZS32.C);
				((SignalTileEnity)worldIn.getTileEntity(pos)).setProperty(SignalHV.ZS1, true);
				ebs = (IExtendedBlockState) state.getBlock().getExtendedState(state, worldIn, pos);
				System.out.println("======== HP " + ebs.getValue(SignalHV.STOPSIGNAL));
				worldIn.notifyBlockUpdate(pos, state, state, 3);
			}
			return EnumActionResult.PASS;
		};
	};

	private static ArrayList<Item> registeredItems = new ArrayList<>();
	
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
