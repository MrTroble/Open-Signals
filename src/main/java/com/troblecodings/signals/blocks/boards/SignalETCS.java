package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.ETCS;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalETCS extends Signal {

    public SignalETCS() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "etcssignal").height(3).noLink().build());
    }

    public static final SEProperty<ETCS> ETCS_TYPE = SEProperty.of("etcs_type", ETCS.NE14_LEFT,
            ChangeableStage.GUISTAGE);

}
