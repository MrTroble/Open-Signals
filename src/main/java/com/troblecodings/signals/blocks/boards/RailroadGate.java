package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.RailroadGateLength;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class RailroadGate extends Signal {

    public RailroadGate() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "railroadgate").height(1).build());
    }

    public static final SEProperty<RailroadGateLength> BARRIER_LENGTH = SEProperty
            .of("barrier_length", RailroadGateLength.L1, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> BARRIER_OPEN = SEProperty.of("barrier_open", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
}
