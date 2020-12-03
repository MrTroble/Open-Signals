package net.gir.girsignals.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class SignaleHV extends CreativeTabs {

	public SignaleHV() {
		super("tabsignalehv");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Blocks.BED);
	}

}
