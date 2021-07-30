package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.LF;
import eu.gir.girsignals.EnumSignals.LFBACKGROUND;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalLF extends Signal {

	public SignalLF() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "lfsignal").noLink().build());
	}

	public static final SEProperty<LF> INDICATOR = SEProperty.of("indicator", LF.Z1, ChangeableStage.GUISTAGE);
	public static final SEProperty<LFBACKGROUND> LFTYPE = SEProperty.of("lftype", LFBACKGROUND.LF1, ChangeableStage.GUISTAGE);

	@Override
	public boolean hasCostumColor() {
		return true;
	}
		
}
