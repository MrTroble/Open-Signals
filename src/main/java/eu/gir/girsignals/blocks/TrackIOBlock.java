package eu.gir.girsignals.blocks;

import java.util.Arrays;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TrackIOBlock extends Signal {

	public TrackIOBlock() {
		super("trackio", 0);
	}

	public static final SEProperty<Boolean> INPUTMODE = SEProperty.of("inputmode", false,
			ChangeableStage.APISTAGE_NONE_CONFIG);
	public static final SEProperty<Boolean> VALUE = SEProperty.of("value", false, ChangeableStage.APISTAGE_NONE_CONFIG);

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		IExtendedBlockState state = (IExtendedBlockState) getExtendedState(blockState, blockAccess, pos);
		return !state.getValue(INPUTMODE) && state.getValue(VALUE) ? 15 : 0;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		IExtendedBlockState state = (IExtendedBlockState) getExtendedState(world.getBlockState(pos), world, pos);
		if (state.getValue(INPUTMODE)) {
			SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
			entity.setProperty(VALUE, Arrays.stream(EnumFacing.VALUES).anyMatch(e -> world.getStrongPower(pos, e) > 0));
		}
	}
	
}
