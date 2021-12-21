package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class Post extends Block {

	public Post() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
}
