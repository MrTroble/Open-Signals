package net.gir.girsignals.blocks;

import net.gir.girsignals.SEProperty;
import net.gir.girsignals.EnumSignals.HL;
import net.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.SEProperty.ChangeableStage;
import net.gir.girsignals.EnumSignals.MAST_SIGN;

public class SignalHL extends SignalBlock {

	public SignalHL() {
		super("HL");
	}

	public static final SEProperty<HL> STOPSIGNAL = SEProperty.of("mainlightsignal", HL.OFF);
	public static final SEProperty<HL_LIGHTBAR> LIGHTBAR = SEProperty.of("mainlightsignallightbar", HL_LIGHTBAR.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false);
	public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs3", ZS32.OFF);
}
