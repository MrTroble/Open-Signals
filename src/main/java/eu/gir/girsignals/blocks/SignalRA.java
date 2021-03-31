package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.EnumSignals.RA_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalRA extends SignalBlock {

	public SignalRA() {
		super("rasignal", 0);
	}

	public static final SEProperty<RA> RATYPE = SEProperty.of("ratype", RA.RA10, ChangeableStage.GUISTAGE);
	public static final SEProperty<RA_LIGHT> RALIGHT = SEProperty.of("ralight", RA_LIGHT.OFF);

}
