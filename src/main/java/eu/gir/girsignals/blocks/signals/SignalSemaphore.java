package eu.gir.girsignals.blocks.signals;

import java.util.Map;

import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalSemaphore extends Signal {

    public SignalSemaphore() {
        super(builder(GIRItems.PLACEMENT_TOOL, "semaphore_signal").height(3).offsetX(10f).offsetY(-11f).signHeight(1.5f).build());
    }
    
    public static final SEProperty<Boolean> SEMATYPE = SEProperty.of("sematype", true,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> WING1 = SEProperty.of("wing1", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<Boolean> HP2 = SEProperty.of("hp2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> WING2 = SEProperty.of("wing2", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
    public static final SEProperty<ZS32> ZS3_PLATE = SEProperty.of("zs3plate", ZS32.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> ZS1 = SEProperty.of("zs1", false);
    public static final SEProperty<Boolean> ZS7 = SEProperty.of("zs7", false);
    public static final SEProperty<Boolean> RA12 = SEProperty.of("ra12", false);
    public static final SEProperty<Boolean> VR2 = SEProperty.of("vr2", false);
    public static final SEProperty<Boolean> VR_PLATE = SEProperty.of("vr_plate", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<Boolean> VR_WING = SEProperty.of("vr_wing", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2_3 = SEProperty.of("ne2_3", false,
            ChangeableStage.GUISTAGE);
    
    @Override
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        if ((Boolean) map.getOrDefault(SEMATYPE, false)) {
            return 7;
        }
        return super.getHeight(map);
    }
}
