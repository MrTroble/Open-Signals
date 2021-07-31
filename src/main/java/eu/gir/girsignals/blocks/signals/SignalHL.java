package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HL_DISTANT;
import eu.gir.girsignals.EnumSignals.HL_LIGHTBAR;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalHL extends Signal {

	public SignalHL() {
		super(builder(GIRItems.PLACEMENT_TOOL, "HL").height(6).signHeight(1.15f).build());
	}

	public static final SEProperty<HL> STOPSIGNAL = SEProperty.of("mainlightsignal", HL.OFF);
	public static final SEProperty<HL_DISTANT> DISTANTSIGNAL = SEProperty.of("mainlightsignal_distant", HL_DISTANT.OFF);
	public static final SEProperty<HL_LIGHTBAR> LIGHTBAR = SEProperty.of("mainlightsignallightbar", HL_LIGHTBAR.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs2", ZS32.OFF);
	
}
