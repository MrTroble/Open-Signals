package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;

public class SignalNE extends Signal {

	public SignalNE() {
		super(GIRItems.SIGN_PLACEMENT_TOOL, "ne", 1);
	}

	public static final SEProperty<NE> NETYPE = SEProperty.of("netype", NE.NE1, ChangeableStage.GUISTAGE);

	@Override
	public boolean canBeLinked() {
		return false;
	}

}
