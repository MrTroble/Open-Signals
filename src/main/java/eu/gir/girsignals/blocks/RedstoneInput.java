package eu.gir.girsignals.blocks;

import eu.gir.girsignals.tileentitys.RedstoneIOTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneInput extends RedstoneIO {
	
	public RedstoneInput() {
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (worldIn.isBlockPowered(pos)) {
			if(state.getValue(RedstoneIO.POWER) != true) {
				worldIn.setBlockState(pos, state.withProperty(RedstoneIO.POWER, true));
				final RedstoneIOTileEntity entity = (RedstoneIOTileEntity) worldIn.getTileEntity(pos);
				entity.sendToAll();
			}
		} else {
			worldIn.setBlockState(pos, state.withProperty(RedstoneIO.POWER, false));
			System.out.println(worldIn.isBlockPowered(pos));
		}
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return 0;
	}
	
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
}
