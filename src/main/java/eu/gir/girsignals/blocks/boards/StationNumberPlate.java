package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.STNumber;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;

public class StationNumberPlate extends Signal {

    public StationNumberPlate() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "stationnumberplate").noLink().height(0)
                .build());

    }

    public static final SEProperty<STNumber> STATIONNUMBER = SEProperty.of("stationnumber",
            STNumber.Z1, ChangeableStage.GUISTAGE);

}
