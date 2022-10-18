package eu.gir.girsignals.blocks.boards;

import java.util.Map;

import eu.gir.girsignals.EnumSignals.Arrow;
import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.EnumSignals.NE5Addition;
import eu.gir.girsignals.EnumSignals.NEAddition;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalNE extends Signal {

    public SignalNE() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "ne").height(2).build());
    }

    public static final SEProperty<NE> NETYPE = SEProperty.of("netype", NE.NE1,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<NEAddition> NEADDITION = SEProperty.of("neaddition",
            NEAddition.OFF, ChangeableStage.GUISTAGE);
    public static final SEProperty<Arrow> ARROWPROP = SEProperty.of("arrow", Arrow.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<NE5Addition> NE5_ADDITION = SEProperty.of("ne5_addition",
            NE5Addition.OFF, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE13 = SEProperty.of("ne13", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(NETYPE, NE.NE13));

    @Override
    public boolean hasCostumColor() {
        return true;
    }
    
    @Override
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        final NE other = (NE) map.get(NETYPE);
        if (other == null)
            return super.getHeight(map);
        switch (other) {
            case NE2:
            case NE2_1:
            case NE4_SMALL:
                return 0;
            case NE1:
            case NE5:
            case NE6:
            case SO1:
            case SO106:
                return 1;
            default:
                return super.getHeight(map);
        }
    }

}
