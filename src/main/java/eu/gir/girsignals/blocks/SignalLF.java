package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.LF1;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalLF extends SignalBlock implements IBlockColor {

	public SignalLF() {
		super("lfsignal", 1);
	}

	public static final SEProperty<LF1> INDICATOR = SEProperty.of("indicator", LF1.Z2, ChangeableStage.GUISTAGE);

	@Override
	public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
		return 0;
	}
	
}
