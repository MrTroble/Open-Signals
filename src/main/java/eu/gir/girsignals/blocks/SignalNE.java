package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.EnumSignals.NE_ADDITION;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;

public class SignalNE extends Signal {

	public SignalNE() {
		super(GIRItems.SIGN_PLACEMENT_TOOL, "ne", 1);
	}

	public static final SEProperty<NE> NETYPE = SEProperty.of("netype", NE.NE1, ChangeableStage.GUISTAGE);
	public static final SEProperty<NE_ADDITION> NEADDITION = SEProperty.of("neaddition", NE_ADDITION.OFF, ChangeableStage.GUISTAGE);

	@Override
	public boolean canBeLinked() {
		return false;
	}

}
