package com.troblecodings.signals.blocks.boards;

import java.util.Map;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.RA;
import com.troblecodings.signals.EnumSignals.RALight;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.config.RSSignalConfig;

public class SignalRA extends Signal {

    public SignalRA() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "rasignal").height(0)
                .config(RSSignalConfig.RS_CONFIG).build());
    }

    public static final SEProperty<RA> RATYPE = SEProperty.of("ratype", RA.RA10,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> RALIGHT = SEProperty.of("ralight", false,
            ChangeableStage.APISTAGE, true,
            check(RATYPE, RA.RA11A).or(check(RATYPE, RA.RA11B)).or(check(RATYPE, RA.RA11_DWARF)));
    public static final SEProperty<RALight> RALIGHTSIGNAL = SEProperty.of("ralightsignal",
            RALight.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true, check(RATYPE, RA.RA6_9));

    @Override
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        final RA ra = (RA) map.get(RATYPE);
        if (ra == null)
            return super.getHeight(map);
        switch (ra) {
            case RA10:
                return 1;
            case RA11A:
            case RA11B:
                return 3;
            case RA6_9:
                return 4;
            default:
                return super.getHeight(map);
        }
    }

}
