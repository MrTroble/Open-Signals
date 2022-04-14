package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRTabs;
import eu.gir.girsignals.tileentitys.RedstoneIOTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneIO extends Block implements ITileEntityProvider {
	
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

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, net.minecraft.util.math.BlockPos pos, EnumFacing side) {
		return getWeakPower(blockState, blockAccess, pos, side);
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, net.minecraft.util.math.BlockPos pos, EnumFacing side) {
		return blockState.getValue(POWER) ? 15:0;
	}
	
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new RedstoneIOTileEntity();
	}

}
