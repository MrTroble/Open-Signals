package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.WN_CROSS;
import eu.gir.girsignals.EnumSignals.WN_NORMAL;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalWN extends Signal {
	
	public SignalWN() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "wnsignal").height(0).build());
	}
	
	public static final SEProperty<Boolean> WNTYPE = SEProperty.of("wntype", false, ChangeableStage.GUISTAGE);
	public static final SEProperty<WN_NORMAL> WNNORMAL = SEProperty.of("wnnormal", WN_NORMAL.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true, check(WNTYPE, false));
	public static final SEProperty<WN_CROSS> WNCROSS = SEProperty.of("wncross", WN_CROSS.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true, check(WNTYPE, true));
	
}
