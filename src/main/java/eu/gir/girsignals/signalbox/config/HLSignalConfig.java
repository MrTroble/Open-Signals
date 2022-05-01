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
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
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
            final Optional<HLExit> hlexit = (Optional<HLExit>) next
                    .getProperty(SignalHL.EXITSIGNAL);

            final ArrayList<KS> goks = Lists.newArrayList(KS.KS1, KS.KS1_BLINK, KS.KS1_BLINK_LIGHT,
                    KS.KS2, KS.KS2_LIGHT);

            final boolean stop = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(o -> stopCheck.contains(o) || (unchanged.contains(o)
                            || hlexit.filter(HLExit.HP0::equals).isPresent())
                            && next.getProperty(SignalHL.LIGHTBAR)
                                    .filter(lbar -> !lbar.equals(HLLightbar.OFF)).isPresent())
                    .isPresent();
            final ArrayList<HL> nextChangedSpeed = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9,
                    HL.HL11_12);

            final boolean ksgo = next.getProperty(SignalKS.STOPSIGNAL).filter(a -> goks.contains(a))
                    .isPresent()
                    || next.getProperty(SignalKS.MAINSIGNAL).filter(KSMain.KS1::equals).isPresent();
            final boolean hvstopgo = next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP1::equals)
                    .isPresent()
                    || next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals).isPresent();
            final boolean hvhomego = next.getProperty(SignalHV.HPHOME).filter(HPHome.HP1::equals)
                    .isPresent()
                    || next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals).isPresent();
            final boolean hvblockgo = next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP1::equals)
                    .isPresent();
            final boolean hv40 = next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals)
                    .isPresent()
                    || next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals).isPresent();

            final boolean changed100 = (next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(nextChangedSpeed::contains).isPresent()
                    || hlexit.filter(HLExit.HL2_3::equals).isPresent())
                    && next.getProperty(SignalHL.LIGHTBAR).filter(HLLightbar.GREEN::equals)
                            .isPresent();
            final boolean normalSpeed = (next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(unchanged::contains).isPresent()
                    || hlexit.filter(HLExit.HL1::equals).isPresent())
                    && next.getProperty(SignalHL.LIGHTBAR).filter(HLLightbar.OFF::equals)
                            .isPresent();

            final Optional<ZS32> speedKS = (Optional<ZS32>) next.getProperty(SignalKS.ZS3);
            final Optional<ZS32> speedHV = (Optional<ZS32>) next.getProperty(SignalHV.ZS3);

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
            if (next.getProperty(SignalKS.STOPSIGNAL).isPresent()
                    || next.getProperty(SignalKS.MAINSIGNAL).isPresent()) {
                if (ksgo) {
                    speedCheck(speed, values, HL.HL1, HL.HL2_3);
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
                    if (speedKS.isPresent()) {
                        final ZS32 speednext = speedKS.get();
                        final int zs32 = speednext.ordinal();
                        if (zs32 > 26 && zs32 <= 35) {
                            speedCheck(speed, values, HL.HL7, HL.HL8_9);
                            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL7);
                        } else if (zs32 >= 36 && zs32 < 42) {
                            speedCheck(speed, values, HL.HL4, HL.HL5_6);
                            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL4);
                        } else {
                            speedCheck(speed, values, HL.HL1, HL.HL2_3);
                            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
                        }
                    }
                } else {
                    speedCheck(speed, values, HL.HL10, HL.HL11_12);
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
                }
            }
            if (next.getProperty(SignalHV.HPHOME).isPresent()
                    || next.getProperty(SignalHV.HPBLOCK).isPresent()
                    || next.getProperty(SignalHV.STOPSIGNAL).isPresent()) {
                if (hvblockgo || hvhomego || hvstopgo) {
                    if (hv40) {
                        speedCheck(speed, values, HL.HL7, HL.HL8_9);
                    } else {
                        speedCheck(speed, values, HL.HL1, HL.HL2_3);
                        values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
                    }
                    if (speedHV.isPresent()) {
                        final ZS32 speednext = speedHV.get();
                        final int zs32 = speednext.ordinal();
                        if (zs32 > 26 && zs32 <= 35) {
                            speedCheck(speed, values, HL.HL7, HL.HL8_9);
                            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL7);
                        } else if (zs32 >= 36 && zs32 < 42) {
                            speedCheck(speed, values, HL.HL4, HL.HL5_6);
                            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL4);
                        } else {
                            speedCheck(speed, values, HL.HL1, HL.HL2_3);
                            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
                        }
                    }
                } else {
                    speedCheck(speed, values, HL.HL10, HL.HL11_12);
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
                }
            }
        } else {
            if (speed <= 10) {
                values.put(SignalHL.EXITSIGNAL, HL.HL2_3);
            } else {
                values.put(SignalHL.EXITSIGNAL, HLExit.HL1);
            }
            speedCheck(speed, values, HL.HL10, HL.HL11_12);
            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
            values.put(SignalHL.ZS2, ZS32.OFF);

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
