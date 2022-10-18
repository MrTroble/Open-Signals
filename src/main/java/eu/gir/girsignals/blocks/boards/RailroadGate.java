package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.RailroadGateLength;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class RailroadGate extends Signal {

    public RailroadGate() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "railroadgate").height(1).build());
    }

    public static final SEProperty<RailroadGateLength> BARRIER_LENGTH = SEProperty
            .of("barrier_length", RailroadGateLength.L1, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> BARRIER_OPEN = SEProperty.of("barrier_open", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
}
