package net.gir.girsignals.tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class SignaleKs extends CreativeTabs {

	public SignaleKs() {
		super("tabsignaleks");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Blocks.BRICK_BLOCK);
	}

}
