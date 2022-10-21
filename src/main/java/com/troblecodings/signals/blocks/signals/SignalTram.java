package com.troblecodings.signals.blocks.signals;

import com.troblecodings.signals.ChangeableStage;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.CAR;
import com.troblecodings.signals.EnumSignals.PED;
import com.troblecodings.signals.EnumSignals.Tram;
import com.troblecodings.signals.EnumSignals.TramAdd;
import com.troblecodings.signals.EnumSignals.TramSwitch;
import com.troblecodings.signals.EnumSignals.TramType;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.init.SignaIItems;

public class SignalTram extends Signal {

    public SignalTram() {
        super(builder(SignaIItems.PLACEMENT_TOOL, "TramSignal").height(0).build());
    }

    public static final SEProperty<TramType> TRAMSIGNAL_TYPE = SEProperty.of("signaltramtype",
            TramType.TRAM, ChangeableStage.GUISTAGE, true);
    public static final SEProperty<Tram> TRAMSIGNAL = SEProperty.of("signaltram", Tram.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(TRAMSIGNAL_TYPE, TramType.TRAM));
    public static final SEProperty<TramAdd> TRAMSIGNAL_ADD = SEProperty.of("signaltramadd",
            TramAdd.OFF, ChangeableStage.GUISTAGE, true, check(TRAMSIGNAL_TYPE, TramType.TRAM));
    public static final SEProperty<Boolean> TRAMSIGNAL_A = SEProperty.of("signaltrama", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(TRAMSIGNAL_TYPE, TramType.TRAM)
                    .and(check(TRAMSIGNAL_ADD, TramAdd.A).or(check(TRAMSIGNAL_ADD, TramAdd.AT))));
    public static final SEProperty<Boolean> TRAMSIGNAL_T = SEProperty.of("signaltramt", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(TRAMSIGNAL_TYPE, TramType.TRAM)
                    .and(check(TRAMSIGNAL_ADD, TramAdd.T).or(check(TRAMSIGNAL_ADD, TramAdd.AT))));
    public static final SEProperty<TramSwitch> TRAM_SWITCH = SEProperty.of("signaltramswitch",
            TramSwitch.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(TRAMSIGNAL_TYPE, TramType.SWITCH));
    public static final SEProperty<CAR> CARSIGNAL = SEProperty.of("signalcar", CAR.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, false, check(TRAMSIGNAL_TYPE, TramType.CAR));
    public static final SEProperty<PED> PEDSIGNAL = SEProperty.of("signalped", PED.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, false,
            check(TRAMSIGNAL_TYPE, TramType.PEDESTRIAN));
}
