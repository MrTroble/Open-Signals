package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.ACAddition;
import eu.gir.girsignals.EnumSignals.ACCar;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalAndreasCross extends Signal {

    public SignalAndreasCross() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "andreas_cross").height(3).build());
    }

    public static final SEProperty<Boolean> ELECTRICITY = SEProperty.of("ac_electricity", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ACAddition> AC_ADDITION = SEProperty.of("ac_addition",
            ACAddition.OFF, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> AC_BLINK_LIGHT = SEProperty.of("ac_blink_light", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(AC_ADDITION, ACAddition.BLINK1).or(check(AC_ADDITION, ACAddition.BLINK2)));
    public static final SEProperty<ACCar> AC_CAR = SEProperty.of("ac_car", ACCar.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(AC_ADDITION, ACAddition.TRAFFIC_LIGHT));
}
