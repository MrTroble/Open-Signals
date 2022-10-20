package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.ChangeableStage;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalBUELight extends Signal {

    public SignalBUELight() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "buelight").height(4).build());
    }

    public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> BUELIGHT = SEProperty.of("buelight", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
}
