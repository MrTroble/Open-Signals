package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.EL;
import eu.gir.girsignals.EnumSignals.ELArrow;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;

public class SignalEL extends Signal {

    public SignalEL() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "elsignal").height(2).noLink().build());
    }

    public static final SEProperty<EL> ELTYPE = SEProperty.of("eltype", EL.EL1V,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ELArrow> ELARROW = SEProperty.of("elarrow", ELArrow.OFF,
            ChangeableStage.GUISTAGE, false);

}
