package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.BUE;
import com.troblecodings.signals.EnumSignals.BUEAdd;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalBUE extends Signal {

    public SignalBUE() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "buesignal").height(2).noLink().build());
    }

    public static final SEProperty<BUE> BUETYPE = SEProperty.of("buetype", BUE.BUE4,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<BUEAdd> BUEADD = SEProperty.of("bueadd", BUEAdd.ADD,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> BUE_SHORT = SEProperty.of("bueshort", false,
            ChangeableStage.GUISTAGE);

}
