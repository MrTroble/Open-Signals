package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.BUE;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalBUE extends Signal {

	public SignalBUE() {
		super(GIRItems.SIGN_PLACEMENT_TOOL,"buesignal", 2);
	}

	public static final SEProperty<BUE> BUETYPE = SEProperty.of("buetype", BUE.BUE4, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> BUEADD = SEProperty.of("bueadd", false, ChangeableStage.GUISTAGE);

	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
