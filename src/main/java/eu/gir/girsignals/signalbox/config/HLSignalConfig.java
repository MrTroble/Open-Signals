package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.enums.PathType;
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
    private void speedCheckExit(final int speed, final Map<SEProperty, Object> values,
            final HLExit normal, final HLExit restricted) {
        if (speed >= 1 && speed <= 10) {
            values.put(SignalHL.EXITSIGNAL, restricted);
        } else {
            values.put(SignalHL.EXITSIGNAL, normal);
        }
    }

    @SuppressWarnings("rawtypes")
    private void speedChecknext(final int speed, final int zs32,
            final Map<SEProperty, Object> values) {
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

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    @Override
    public void change(final ConfigInfo info) {
        if (info.type.equals(PathType.SHUNTING)) {
            RSSignalConfig.RS_CONFIG.change(info);
            return;
        }
        final HashMap<SEProperty, Object> values = new HashMap<>();
        if (info.next != null) {
            info.next.getProperty(SignalHL.STOPSIGNAL).ifPresent(hl -> {
                final boolean stop = hl.equals(HL.HP0) || hl.equals(HL.HP0_ALTERNATE_RED);
                if (stop) {
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
                } else if (hl.equals(HL.HL4)) {
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL4);
                } else if (Signallists.HL_40_MAIN.contains(hl) || hl.equals(HL.HL7)) {
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL7);
                } else {
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
                }
            });
            final Optional<HLLightbar> optionalLightBar = (Optional<HLLightbar>) info.next
                    .getProperty(SignalHL.LIGHTBAR);
            final Optional<HL> hlStop = (Optional<HL>) info.next.getProperty(SignalHL.STOPSIGNAL);

            final Optional<HLExit> hlexit = (Optional<HLExit>) info.next
                    .getProperty(SignalHL.EXITSIGNAL);
            final Optional<ZS32> speedKS = (Optional<ZS32>) info.next.getProperty(SignalKS.ZS3);
            final Optional<ZS32> speedKSplate = (Optional<ZS32>) info.next
                    .getProperty(SignalKS.ZS3_PLATE);
            final Optional<ZS32> speedHV = (Optional<ZS32>) info.next.getProperty(SignalHV.ZS3);
            final Optional<ZS32> speedHVplate = (Optional<ZS32>) info.next
                    .getProperty(SignalHV.ZS3_PLATE);

            final boolean ksgo = info.next.getProperty(SignalKS.STOPSIGNAL)
                    .filter(a -> Signallists.KS_GO.contains(a)).isPresent()
                    || info.next.getProperty(SignalKS.MAINSIGNAL).filter(KSMain.KS1::equals)
                            .isPresent();
            final boolean hvgo = info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP1::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals).isPresent()
                    || info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP1::equals)
                            .isPresent();
            final boolean hvgo2 = info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP1::equals)
                            .isPresent();
            final boolean hv40 = info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals)
                            .isPresent();
            final boolean stop = hlStop
                    .filter(o -> Signallists.HL_STOP.contains(o)
                            || (Signallists.HL_UNCHANGED.contains(o) && optionalLightBar
                                    .filter(lbar -> !lbar.equals(HLLightbar.OFF)).isPresent()))
                    .isPresent()
                    || hlexit
                            .filter(a -> Signallists.HLEXIT_STOP.contains(a) && optionalLightBar
                                    .filter(lbar -> !lbar.equals(HLLightbar.OFF)).isPresent())
                            .isPresent();

            final boolean changed100 = (hlStop.filter(Signallists.HL_40_MAIN::contains).isPresent()
                    || hlexit.filter(HLExit.HL2_3::equals).isPresent())
                    && optionalLightBar.filter(HLLightbar.GREEN::equals).isPresent();

            final boolean normalSpeed = (hlStop.filter(Signallists.HL_UNCHANGED::contains)
                    .isPresent() || hlexit.filter(HLExit.HL1::equals).isPresent())
                    && (!optionalLightBar.isPresent()
                            || optionalLightBar.filter(HLLightbar.OFF::equals).isPresent());

            final boolean nexthv = info.next.getProperty(SignalHV.HPHOME).isPresent()
                    || info.next.getProperty(SignalHV.HPBLOCK).isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).isPresent();

            final boolean nextks = info.next.getProperty(SignalKS.STOPSIGNAL).isPresent()
                    || info.next.getProperty(SignalKS.MAINSIGNAL).isPresent();

            if (stop) {
                speedCheck(info.speed, values, HL.HL10, HL.HL11_12);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
            } else if (changed100) {
                speedCheck(info.speed, values, HL.HL4, HL.HL5_6);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL4);
            } else if (normalSpeed) {
                speedCheck(info.speed, values, HL.HL1, HL.HL2_3);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
            } else {
                speedCheck(info.speed, values, HL.HL7, HL.HL8_9);
                values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL7);
            }
            speedCheckExit(info.speed, values, HLExit.HL1, HLExit.HL2_3);
            if (nextks || nexthv) {
                if (ksgo || hvgo || hvgo2) {
                    if (hv40) {
                        speedCheck(info.speed, values, HL.HL7, HL.HL8_9);
                        values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL7);
                    } else if (!hv40) {
                        speedCheck(info.speed, values, HL.HL1, HL.HL2_3);
                        values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL1);
                    }
                    if (speedKS.isPresent() || speedKSplate.isPresent()) {
                        final ZS32 speednext = speedKS.isPresent() ? speedKS.get()
                                : speedKSplate.get();
                        final int zs32 = speednext.ordinal();
                        speedChecknext(info.speed, zs32, values);
                    } else if (speedHV.isPresent() || speedHVplate.isPresent()) {
                        final ZS32 speednext = speedHV.isPresent() ? speedHV.get()
                                : speedHVplate.get();
                        final int zs32 = speednext.ordinal();
                        speedChecknext(info.speed, zs32, values);
                    }
                } else {
                    speedCheck(info.speed, values, HL.HL10, HL.HL11_12);
                    values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
                }
            }
        } else {
            speedCheckExit(info.speed, values, HLExit.HL1, HLExit.HL2_3);
            speedCheck(info.speed, values, HL.HL10, HL.HL11_12);
            values.put(SignalHL.DISTANTSIGNAL, HLDistant.HL10);
            values.put(SignalHL.ZS2, ZS32.OFF);
        }
        this.changeIfPresent(values, info.current);
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
