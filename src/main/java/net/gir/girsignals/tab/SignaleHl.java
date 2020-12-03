package net.gir.girsignals.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class SignaleHl extends CreativeTabs {

	public SignaleHl() {
		super("tabsignalehl");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Blocks.BEDROCK);
	}

}
