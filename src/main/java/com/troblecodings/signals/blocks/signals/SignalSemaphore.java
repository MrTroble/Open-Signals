package com.troblecodings.signals.blocks.signals;

import java.util.Map;

import com.troblecodings.signals.EnumSignals.SemaDist;
import com.troblecodings.signals.EnumSignals.SemaType;
import com.troblecodings.signals.EnumSignals.ZS32;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.SignalItems;
import com.troblecodings.signals.signalbox.config.SemaphoreConfig;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.client.gui.FontRenderer;

public class SignalSemaphore extends Signal {

    public SignalSemaphore() {
        super(builder(SignalItems.PLACEMENT_TOOL, "semaphore_signal").height(3).offsetX(10f)
                .offsetY(-11f).signHeight(1.47f).config(SemaphoreConfig.SEMAPHORE_CONFIG).build());
    }

    public static final SEProperty<SemaType> SEMATYPE = SEProperty.of("sematype", SemaType.DIST,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> HP2 = SEProperty.of("hp2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> WING1 = SEProperty.of("wing1", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, false,
            check(SEMATYPE, SemaType.MAIN).or(check(SEMATYPE, SemaType.MAIN_SMALL)));
    public static final SEProperty<Boolean> WING2 = SEProperty.of("wing2", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, false,
            (check(SEMATYPE, SemaType.MAIN).or(check(SEMATYPE, SemaType.MAIN_SMALL)))
                    .and(check(HP2, Boolean.TRUE)));
    public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
    public static final SEProperty<ZS32> ZS3_PLATE = SEProperty.of("zs3plate", ZS32.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> ZS1 = SEProperty.of("zs1", false);
    public static final SEProperty<Boolean> ZS7 = SEProperty.of("zs7", false);
    public static final SEProperty<Boolean> RA12 = SEProperty.of("ra12", false);
    public static final SEProperty<SemaDist> SEMA_VR = SEProperty.of("semavr", SemaDist.VR0,
            ChangeableStage.APISTAGE_NONE_CONFIG, false, check(SEMATYPE, SemaType.DIST));
    public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2_3 = SEProperty.of("ne2_3", false,
            ChangeableStage.GUISTAGE);

    @Override
    public int getHeight(final Map<SEProperty<?>, Object> map) {
        final SemaType st = (SemaType) map.get(SEMATYPE);
        if (st == null)
            return super.getHeight(map);
        switch (st) {
            case MAIN:
                return 7;
            case MAIN_SMALL:
                return 4;
            default:
                return super.getHeight(map);
        }
    }

    @Override
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        super.renderOverlay(x, y, z, te, font,
                te.getProperty(SEMATYPE).filter(st -> st.equals(SemaType.MAIN_SMALL)).isPresent()
                        ? 1.04f
                        : this.prop.customNameRenderHeight);
    }
}
