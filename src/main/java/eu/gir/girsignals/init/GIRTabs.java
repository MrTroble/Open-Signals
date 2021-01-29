package eu.gir.girsignals.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class GIRTabs {

	public static final CreativeTabs tab = new CreativeTabs("GIRSignals") {
		
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(GIRBlocks.HV_SIGNAL_CONTROLLER);
		}
	};

}
