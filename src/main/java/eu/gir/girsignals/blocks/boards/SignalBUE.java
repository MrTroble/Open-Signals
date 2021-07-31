package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.BUE;
import eu.gir.girsignals.EnumSignals.BUE_ADD;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalBUE extends Signal {

	public SignalBUE() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "buesignal").height(2).noLink().build());
	}

	public static final SEProperty<BUE> BUETYPE = SEProperty.of("buetype", BUE.BUE4, ChangeableStage.GUISTAGE);
	public static final SEProperty<BUE_ADD> BUEADD = SEProperty.of("bueadd", BUE_ADD.ADD, ChangeableStage.GUISTAGE);

}
