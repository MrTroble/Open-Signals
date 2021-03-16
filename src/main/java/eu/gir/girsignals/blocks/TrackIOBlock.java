package eu.gir.girsignals.blocks;

import java.util.Arrays;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TrackIOBlock extends Signal {

	public TrackIOBlock() {
		super("trackio", 0);
	}

	private static final class SENoneConfigBool extends SEProperty<Boolean> {

		public SENoneConfigBool(String name) {
			super(PropertyBool.create(name), false, ChangeableStage.APISTAGE_NONE_CONFIG);
		}
		
		@Override
		public boolean isChangabelAtStage(ChangeableStage stage) {
			return super.isChangabelAtStage(stage) || stage == ChangeableStage.APISTAGE;
		}
	}
	
	public static final SEProperty<Boolean> INPUTMODE = new SENoneConfigBool("inputmode");
	public static final SEProperty<Boolean> VALUE = new SENoneConfigBool("value");

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		IExtendedBlockState state = (IExtendedBlockState) getExtendedState(blockState, blockAccess, pos);
		return state != null && !state.getValue(INPUTMODE) && state.getValue(VALUE) ? 15 : 0;
	}
	
	@Override
	public void neighborChanged(IBlockState osstate, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		IExtendedBlockState state = (IExtendedBlockState) getExtendedState(osstate, world, pos);
		if (state != null && state.getValue(INPUTMODE) && !world.isRemote) {
			boolean b = Arrays.stream(EnumFacing.VALUES).anyMatch(e -> world.isSidePowered(pos.offset(e), e.getOpposite()));
			SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
			entity.setProperty(VALUE, b);
		}
		onNeighborChange(world, pos, fromPos);
	}
	
	@Override
	public boolean hasAngel() {
		return false;
	}
}
