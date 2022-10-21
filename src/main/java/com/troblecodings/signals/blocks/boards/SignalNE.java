package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.ChangeableStage;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.Arrow;
import com.troblecodings.signals.EnumSignals.NE;
import com.troblecodings.signals.EnumSignals.NEAddition;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.init.GIRItems;

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
