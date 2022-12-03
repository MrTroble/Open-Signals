package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalBUELight extends Signal {

    public SignalBUELight() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "buelight").height(4).build());
    }

    public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> BUELIGHT = SEProperty.of("buelight", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<Boolean> BUETYPE = SEProperty.of("buetype", false,
            ChangeableStage.GUISTAGE);
}
