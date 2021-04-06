package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.LF;
import eu.gir.girsignals.EnumSignals.LFBACKGROUND;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalLF extends SignalBlock {

	public SignalLF() {
		super("lfsignal", 1);
	}

	public static final SEProperty<LF> INDICATOR = SEProperty.of("indicator", LF.Z2, ChangeableStage.GUISTAGE);
	public static final SEProperty<LFBACKGROUND> LFTYPE = SEProperty.of("lftype", LFBACKGROUND.LF1, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> LAMPS = SEProperty.of("lamps", true, ChangeableStage.GUISTAGE);

	@Override
	public boolean hasCostumColor() {
		return true;
	}
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
