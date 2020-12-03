package net.gir.girsignals.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class SignaleSh extends CreativeTabs {

	public SignaleSh() {
		super("tabsignalesh");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Blocks.BOOKSHELF);
	}

}
