package com.troblecodings.signals.blocks.boards;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.STNumber;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.SignaIItems;

public class StationNumberPlate extends Signal {

    public StationNumberPlate() {
        super(builder(SignaIItems.SIGN_PLACEMENT_TOOL, "stationnumberplate").noLink().height(0)
                .build());

    }

    public static final SEProperty<STNumber> STATIONNUMBER = SEProperty.of("stationnumber",
            STNumber.Z1, ChangeableStage.GUISTAGE);

}
