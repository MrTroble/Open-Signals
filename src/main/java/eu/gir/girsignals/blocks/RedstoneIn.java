package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class RedstoneIn extends Block {

	public RedstoneIn() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
	}
}
