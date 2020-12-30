package net.gir.girsignals.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class Tabs {

	public static final CreativeTabs tab = new CreativeTabs("GIRSignals") {
		
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Blocks.HV_SIGNAL_CONTROLLER);
		}
	};

}
