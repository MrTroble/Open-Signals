package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HL_DISTANT;
import eu.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import eu.gir.girsignals.EnumSignals.HL_TYPE;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalHL extends Signal {

	public SignalHL() {
		super(builder(GIRItems.PLACEMENT_TOOL, "HL").height(6).signHeight(1.15f).build());
	}

	public static final SEProperty<HL_TYPE> HLTYPE = SEProperty.of("hltype", HL_TYPE.MAIN, ChangeableStage.GUISTAGE);
	public static final SEProperty<HL> STOPSIGNAL = SEProperty.of("mainlightsignal", HL.OFF, ChangeableStage.APISTAGE_NONE_CONFIG);
	public static final SEProperty<HL_DISTANT> DISTANTSIGNAL = SEProperty.of("mainlightsignal_distant", HL_DISTANT.OFF, ChangeableStage.APISTAGE_NONE_CONFIG);
	public static final SEProperty<HL_LIGHTBAR> LIGHTBAR = SEProperty.of("mainlightsignallightbar", HL_LIGHTBAR.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs2", ZS32.OFF);
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
