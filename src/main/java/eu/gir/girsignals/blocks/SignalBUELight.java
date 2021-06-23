package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.BUE_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;

public class SignalBUELight extends Signal {

	public SignalBUELight() {
		super(GIRItems.SIGN_PLACEMENT_TOOL, "buelight", 4);
	}

	public static final SEProperty<BUE_LIGHT> BUELIGHT = SEProperty.of("buelight", BUE_LIGHT.OFF);
	public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false, ChangeableStage.GUISTAGE);
}
