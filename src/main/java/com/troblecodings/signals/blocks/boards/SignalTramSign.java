package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.TramSigns;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;

public class SignalTramSign extends Signal {

    public SignalTramSign() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "tramsignsignal").height(0).noLink().build());
    }

    public static final SEProperty<TramSigns> TRAMSIGNS = SEProperty.of("tramsigns", TramSigns.SH1,
            ChangeableStage.GUISTAGE);

}
