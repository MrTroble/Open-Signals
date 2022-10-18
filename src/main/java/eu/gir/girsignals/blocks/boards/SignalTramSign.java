package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.TramSigns;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalTramSign extends Signal {

    public SignalTramSign() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "tramsignsignal").height(0).noLink().build());
    }

    public static final SEProperty<TramSigns> TRAMSIGNS = SEProperty.of("tramsigns", TramSigns.SH1,
            ChangeableStage.GUISTAGE);

}
