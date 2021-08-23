package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalBridge extends Block {

	public static final PropertyBool COLUMN = PropertyBool.create("column");

	public SignalBridge() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
		setDefaultState(this.blockState.getBaseState().withProperty(COLUMN, false));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(COLUMN, this.canConnectTo(worldIn, pos.up()));
	}

	private boolean canConnectTo(IBlockAccess worldIn, BlockPos pos) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();
		return block == GIRBlocks.SIGNAL_BRIDGE;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, COLUMN);

	}

}
