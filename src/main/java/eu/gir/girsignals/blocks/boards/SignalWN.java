package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.ChangeableStage;
import eu.gir.girsignals.EnumSignals.WNCross;
import eu.gir.girsignals.EnumSignals.WNNormal;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalWN extends Signal {

    public SignalWN() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "wnsignal").height(0).build());
    }

    public static final SEProperty<Boolean> WNTYPE = SEProperty.of("wntype", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<WNNormal> WNNORMAL = SEProperty.of("wnnormal", WNNormal.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(WNTYPE, false));
    public static final SEProperty<WNCross> WNCROSS = SEProperty.of("wncross", WNCross.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(WNTYPE, true));

}
