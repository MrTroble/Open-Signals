package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.TS;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalTS extends Signal {
    public SignalTS() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "tssignal").noLink().build());
    }

    public static final SEProperty<TS> TSTYPE = SEProperty.of("tstype", TS.TS1,
            ChangeableStage.GUISTAGE);

    @Override
    public boolean hasCostumColor() {
        return true;
    }
}
