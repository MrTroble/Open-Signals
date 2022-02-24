package eu.gir.girsignals.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneInput extends RedstoneIO {
	
	
	public RedstoneInput() {}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.withProperty(RedstoneIO.POWER, true));
		}
	}
	
}
