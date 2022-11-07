package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.WNCross;
import com.troblecodings.signals.EnumSignals.WNNormal;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.SignalItems;

public class SignalWN extends Signal {

    public SignalWN() {
        super(builder(SignalItems.SIGN_PLACEMENT_TOOL, "wnsignal").height(0).build());
    }

    public static final SEProperty<Boolean> WNTYPE = SEProperty.of("wntype", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<WNNormal> WNNORMAL = SEProperty.of("wnnormal", WNNormal.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(WNTYPE, false));
    public static final SEProperty<WNCross> WNCROSS = SEProperty.of("wncross", WNCross.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(WNTYPE, true));

}
