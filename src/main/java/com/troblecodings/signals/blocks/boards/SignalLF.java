package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.LF;
import com.troblecodings.signals.EnumSignals.LFBachground;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalLF extends Signal {

    public SignalLF() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "lfsignal").noLink().build());
    }

    public static final SEProperty<LF> INDICATOR = SEProperty.of("indicator", LF.Z1,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<LFBachground> LFTYPE = SEProperty.of("lftype", LFBachground.LF1,
            ChangeableStage.GUISTAGE);

    @Override
    public boolean hasCostumColor() {
        return true;
    }

}