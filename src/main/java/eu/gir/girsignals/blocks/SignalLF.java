package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.LF1;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalLF extends SignalBlock {

	public SignalLF() {
		super("lfsignal", 2);
	}

	public static final SEProperty<LF1> INDICATOR = SEProperty.of("indicator", LF1.Z2, ChangeableStage.GUISTAGE);
	
}
