package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.Arrow;
import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.EnumSignals.NEAddition;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;

public class SignalNE extends Signal {

    public SignalNE() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "ne").noLink().build());
    }

    public static final SEProperty<NE> NETYPE = SEProperty.of("netype", NE.NE1,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<NEAddition> NEADDITION = SEProperty.of("neaddition",
            NEAddition.OFF, ChangeableStage.GUISTAGE);
    public static final SEProperty<Arrow> ARROWPROP = SEProperty.of("arrow", Arrow.OFF,
            ChangeableStage.GUISTAGE);

    @Override
    public boolean hasCostumColor() {
        return true;
    }

}
