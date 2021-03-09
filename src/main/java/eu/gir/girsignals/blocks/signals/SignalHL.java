package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;

public class SignalHL extends Signal {

	public SignalHL() {
		super("HL", 6, 1.15f);
	}

	public static final SEProperty<HL> STOPSIGNAL = SEProperty.of("mainlightsignal", HL.OFF);
	public static final SEProperty<HL> DISTANTSIGNAL = SEProperty.of("mainlightsignal_distant", HL.OFF);
	public static final SEProperty<HL_LIGHTBAR> LIGHTBAR = SEProperty.of("mainlightsignallightbar", HL_LIGHTBAR.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false);
	public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false);
	public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs3", ZS32.OFF);
}
