package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.EL;
import com.troblecodings.signals.EnumSignals.ELArrow;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalEL extends Signal {

    public SignalEL() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "elsignal").height(2).noLink().build());
    }

    public static final SEProperty<EL> ELTYPE = SEProperty.of("eltype", EL.EL1V,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ELArrow> ELARROW = SEProperty.of("elarrow", ELArrow.OFF,
            ChangeableStage.GUISTAGE, false);

}
