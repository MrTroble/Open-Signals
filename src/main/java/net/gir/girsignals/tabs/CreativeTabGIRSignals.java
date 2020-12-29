package net.gir.girsignals.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CreativeTabGIRSignals extends CreativeTabs {

	public CreativeTabGIRSignals() {
		super("GIRSignals");
	}
 
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Blocks.BEDROCK);
	}

}
