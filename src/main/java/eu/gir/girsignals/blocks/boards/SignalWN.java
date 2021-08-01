package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.WN;
import eu.gir.girsignals.EnumSignals.WN_CROSS;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalWN extends Signal {

	public SignalWN() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "wnsignal").height(0).build());
	}

	public static final SEProperty<WN> WNTYPE = SEProperty.of("wntype", WN.OFF);
	public static final SEProperty<WN_CROSS> WNCROSS = SEProperty.of("wncross", WN_CROSS.OFF);

}
