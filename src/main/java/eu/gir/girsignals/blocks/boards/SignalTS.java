package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.TS;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalTS extends Signal {
    public SignalTS() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "tssignal").noLink().build());
    }

    public static final SEProperty<TS> TSTYPE = SEProperty.of("tstype", TS.TS1,
            ChangeableStage.GUISTAGE);

    @Override
    public boolean hasCostumColor() {
        return true;
    }
}
