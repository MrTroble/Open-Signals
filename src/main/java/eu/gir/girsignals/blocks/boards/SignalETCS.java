package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.ETCS;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalETCS extends Signal {

    public SignalETCS() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "etcssignal").height(3).noLink().build());
    }

    public static final SEProperty<ETCS> ETCS_TYPE = SEProperty.of("etcs_type", ETCS.NE14_LEFT,
            ChangeableStage.GUISTAGE);

}
