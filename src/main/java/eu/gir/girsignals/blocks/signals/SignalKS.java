package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KS_DISTANT;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalKS extends Signal {

	public SignalKS() {
		super(builder(GIRItems.PLACEMENT_TOOL, "KS").height(6).signHeight(4.95f).build());
	}

	public static final SEProperty<KS> STOPSIGNAL = SEProperty.of("kombisignal", KS.OFF);
	public static final SEProperty<KS_DISTANT> DISTANTSIGNAL = SEProperty.of("kombisignal_distant", KS_DISTANT.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs2", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3_PLATE = SEProperty.of("zs3plate", ZS32.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<ZS32> ZS3V_PLATE = SEProperty.of("zs3vplate", ZS32.OFF, ChangeableStage.GUISTAGE);
	
	@Override
	public boolean hasCostumColor() {
		return true;
	}
	
	@Override
	public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
		return tintIndex == 1 ? 0xFFC200:0xFFFFFF;
	}
	
}
