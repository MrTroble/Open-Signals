package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class RedstoneIO extends Block {
	
	public static final PropertyBool POWER = PropertyBool.create("power");

	public RedstoneIO() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
		this.setDefaultState(getDefaultState().withProperty(POWER, false));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(POWER) ? 0:1;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(POWER, meta == 1);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { POWER });
	}

}
