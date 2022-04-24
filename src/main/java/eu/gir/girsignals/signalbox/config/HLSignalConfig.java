package eu.gir.girsignals.signalbox.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class HLSignalConfig implements ISignalAutoconfig {

    public static final HLSignalConfig INSTANCE = new HLSignalConfig();

    private HLSignalConfig() {
    }

    @SuppressWarnings("rawtypes")
    private void speedCheck(final int speed, final Map<SEProperty, Object> values, final HL normal,
            final HL restricted) {
        if (speed >= 1 && speed <= 10) {
            values.put(SignalHL.STOPSIGNAL, restricted);
            if (speed <= 5) {
                values.put(SignalHL.LIGHTBAR, HLLightbar.OFF);
            } else if (speed <= 9) {
                values.put(SignalHL.LIGHTBAR, HLLightbar.YELLOW);
            } else if (speed == 10) {
                values.put(SignalHL.LIGHTBAR, HLLightbar.GREEN);
            }
        } else {
            values.put(SignalHL.STOPSIGNAL, normal);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void change(final int speed, final SignalTileEnity current, final SignalTileEnity next) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        if (next != null) {
            final ArrayList<HL> stopCheck = Lists.newArrayList(HL.HP0, HL.HP0_ALTERNATE_RED,
                    HL.HL_ZS1, HL.HL_SHUNTING);
            final ArrayList<HL> unchanged = Lists.newArrayList(HL.HL1, HL.HL4, HL.HL7, HL.HL10);
            final Optional<HLExit> hlexit = (Optional<HLExit>) next.getProperty(SignalHL.EXITSIGNAL);
            final boolean stop = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(o -> stopCheck.contains(o)
                            || (unchanged.contains(o) 
                                    || hlexit.filter(HLExit.HP0::equals).isPresent())
                                    && next.getProperty(SignalHL.LIGHTBAR)
                                    .filter(lbar -> !lbar.equals(HLLightbar.OFF)).isPresent())
                    .isPresent();
            final ArrayList<HL> nextChangedSpeed = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9,
                    HL.HL11_12);
            final boolean changed100 = (next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(nextChangedSpeed::contains).isPresent()
                    || hlexit.filter(HLExit.HL2_3::equals).isPresent())
                    && next.getProperty(SignalHL.LIGHTBAR).filter(HLLightbar.GREEN::equals)
                            .isPresent();
            final boolean normalSpeed = (next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(unchanged::contains).isPresent()
                    || hlexit.filter(HLExit.HL1::equals).isPresent()) &&
                    next.getProperty(SignalHL.LIGHTBAR).filter(HLLightbar.OFF::equals)
                            .isPresent();

            if (stop) {
                speedCheck(speed, values, HL.HL10, HL.HL11_12);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
            } else if (changed100) {
                speedCheck(speed, values, HL.HL4, HL.HL5_6);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL4);
            } else if (normalSpeed) {
                speedCheck(speed, values, HL.HL1, HL.HL2_3);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
            } else {
                speedCheck(speed, values, HL.HL7, HL.HL8_9);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL7);
            }
            if (speed <= 10) {
                values.put(SignalHL.EXITSIGNAL, HLExit.HL2_3);
            } else {
                values.put(SignalHL.EXITSIGNAL, HLExit.HL1);
            }
        } else {
            values.put(SignalHL.LIGHTBAR, HLLightbar.OFF);
            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
            values.put(SignalHL.STOPSIGNAL, HL.HL10);
            values.put(SignalHL.ZS2, ZS32.ZS13);
            values.put(SignalHL.EXITSIGNAL, HLExit.HP0);
        }
        this.changeIfPresent(values, current);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void reset(final SignalTileEnity current) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        values.put(SignalHL.LIGHTBAR, HLLightbar.OFF);
        values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
        values.put(SignalHL.STOPSIGNAL, HL.HP0);
        values.put(SignalHL.EXITSIGNAL, HLExit.HP0);
        values.put(SignalHL.ZS2, ZS32.OFF);
        this.changeIfPresent(values, current);
    }

}
