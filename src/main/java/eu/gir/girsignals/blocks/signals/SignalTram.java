package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.Tram;
import eu.gir.girsignals.EnumSignals.TramAdd;
import eu.gir.girsignals.EnumSignals.TramSwitch;
import eu.gir.girsignals.EnumSignals.TramType;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.ChangeableStage;
import eu.gir.girsignals.init.SignalItems;

public class SignalTram extends Signal {

    public SignalTram() {
        super(builder(SignalItems.PLACEMENT_TOOL, "TramSignal").height(0).build());
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
