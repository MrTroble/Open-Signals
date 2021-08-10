package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.EL;
import eu.gir.girsignals.EnumSignals.EL_ARROW;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalEL extends Signal {

	public SignalEL() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "elsignal").height(2).noLink().build());
	}

	public static final SEProperty<EL> ELTYPE = SEProperty.of("eltype", EL.EL1V, ChangeableStage.GUISTAGE);
	public static final SEProperty<EL_ARROW> ELARROW = SEProperty.of("elarrow", EL_ARROW.OFF, ChangeableStage.GUISTAGE, false);

}
