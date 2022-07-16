package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class HVSignalConfig implements ISignalAutoconfig {

    public static final HVSignalConfig INSTANCE = new HVSignalConfig();

    private HVSignalConfig() {
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    @Override
    public void change(final ConfigInfo info) {

        final HashMap<SEProperty, Object> values = new HashMap<>();

        if (info.type.equals(PathType.SHUNTING)) {
            RSSignalConfig.RS_CONFIG.change(info);
            return;
        }

        if (info.next != null) {
            if (info.speed < 7 && info.speed > 0 && info.speed != 4) {
                info.current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
                    final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + info.speed];
                    info.current.setProperty(SignalHV.ZS3, zs32);
                });
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP2);
                values.put(SignalHV.STOPSIGNAL, HP.HP2);
            } else if (info.speed == 4) {
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP2);
                values.put(SignalHV.STOPSIGNAL, HP.HP2);
            } else if (info.speed >= 7 && info.speed <= 16) {
                info.current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
                    final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + info.speed];
                    info.current.setProperty(SignalHV.ZS3, zs32);
                });
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP1);
                values.put(SignalHV.STOPSIGNAL, HP.HP1);
            } else {
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP1);
                values.put(SignalHV.STOPSIGNAL, HP.HP1);
            }
            final Optional<ZS32> speedKS = (Optional<ZS32>) info.next.getProperty(SignalKS.ZS3);
            final Optional<ZS32> speedKSplate = (Optional<ZS32>) info.next
                    .getProperty(SignalKS.ZS3_PLATE);
            final Optional<HLLightbar> getlightbar = (Optional<HLLightbar>) info.next
                    .getProperty(SignalHL.LIGHTBAR);
            final Optional<VR> currentdistant = (Optional<VR>) info.current
                    .getProperty(SignalHV.DISTANTSIGNAL);
            final Optional<ZS32> speedHVZS3plate = (Optional<ZS32>) info.next
                    .getProperty(SignalHV.ZS3_PLATE);
            final Optional<ZS32> hvZS3 = (Optional<ZS32>) info.next.getProperty(SignalHV.ZS3);
            final Optional<HPHome> nextHPHOME = (Optional<HPHome>) info.next
                    .getProperty(SignalHV.HPHOME);
            final Optional<HP> nextHP = (Optional<HP>) info.next.getProperty(SignalHV.STOPSIGNAL);

            final boolean hlstop = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> Signallists.HL_STOP.contains(a)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL)
                            .filter(d -> Signallists.HLEXIT_STOP.contains(d)).isPresent();
            final boolean hlmain40 = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(c -> Signallists.HL_40_MAIN.contains(c)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL).filter(HLExit.HL2_3::equals)
                            .isPresent();
            final boolean ksstop = info.next.getProperty(SignalKS.STOPSIGNAL)
                    .filter(a -> Signallists.STOP_KS.contains(a)).isPresent();
            final boolean stop = info.next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP0::equals)
                    .isPresent() || nextHPHOME.filter(HPHome.HP0::equals).isPresent()
                    || nextHPHOME.filter(HPHome.HP0_ALTERNATE_RED::equals).isPresent();
            final boolean stop2 = nextHP.filter(HP.HP0::equals).isPresent()
                    || nextHP.filter(HP.SHUNTING::equals).isPresent();
            final boolean ksstopmain = info.next.getProperty(SignalKS.MAINSIGNAL)
                    .filter(b -> Signallists.STOP_KS_MAIN.contains(b)).isPresent();

            if (currentdistant.isPresent()) {
                if (stop || stop2) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                } else if (nextHP.filter(HP.HP2::equals).isPresent()
                        || nextHPHOME.filter(HPHome.HP2::equals).isPresent()) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                } else {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                }
            }

            info.next.getProperty(SignalHV.ZS3)
                    .ifPresent(prevzs3 -> values.put(SignalHV.ZS3V, prevzs3));

            if (info.next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL).isPresent()) {
                if (currentdistant.isPresent()) {
                    if (hlstop) {
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                    } else if (info.current.getProperty(SignalHV.ZS3V).isPresent()) {
                        if (info.next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                                || info.next.getProperty(SignalHL.EXITSIGNAL).isPresent()) {
                            if (hlmain40
                                    && getlightbar.filter(HLLightbar.OFF::equals).isPresent()) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                            } else if (hlmain40
                                    && getlightbar.filter(HLLightbar.YELLOW::equals).isPresent()) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                                values.put(SignalHV.ZS3V, ZS32.Z6);
                            } else if (hlmain40
                                    && getlightbar.filter(HLLightbar.GREEN::equals).isPresent()) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                                values.put(SignalHV.ZS3V, ZS32.Z10);
                            } else if (hlstop) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                            } else {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                            }
                        }
                    } else if (hlmain40) {
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                    } else {
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                    }
                }
            }
            if (currentdistant.isPresent()) {
                if ((!ksstop || !ksstopmain)
                        && (info.next.getProperty(SignalKS.STOPSIGNAL).isPresent()
                                || info.next.getProperty(SignalKS.MAINSIGNAL).isPresent())) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                    if (speedKS.isPresent() || speedKSplate.isPresent()) {
                        final ZS32 speednext = speedKS.isPresent() ? speedKS.get()
                                : speedKSplate.get();
                        final int zs32 = speednext.ordinal();
                        if (zs32 > 26 && zs32 <= 42) {
                            values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                            values.put(SignalHV.ZS3V, speednext);
                            if (zs32 == 30) {
                                values.put(SignalHV.ZS3V, ZS32.OFF);
                            }
                            if (zs32 > 32) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                            }
                        } else if (zs32 < 26) {
                            values.put(SignalKS.ZS2V, speednext);
                        }
                    }
                } else if (ksstop || ksstopmain) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                }
            }
            if (speedHVZS3plate.isPresent() && (!stop || !stop2)) {
                if (!hvZS3.isPresent()) {
                    final ZS32 speedcurrent = speedHVZS3plate.get();
                    final int zs32 = speedcurrent.ordinal();
                    if (zs32 > 26 && zs32 <= 42) {
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                        values.put(SignalHV.ZS3V, speedcurrent);
                        if (zs32 == 30) {
                            values.put(SignalHV.ZS3V, VR.VR2);
                            values.put(SignalHV.ZS3V, ZS32.OFF);
                        }
                        if (zs32 > 32) {
                            values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                        }
                    } else if (zs32 <= 26) {
                        values.put(SignalHV.ZS2V, speedcurrent);
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                    }
                }
            }
        } else {
            values.put(SignalHV.HPBLOCK, HPBlock.HP1);
            values.put(SignalHV.HPHOME, HPHome.HP2);
            values.put(SignalHV.STOPSIGNAL, HP.HP2);
            values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
            values.put(SignalHV.ZS3, ZS32.Z3);
            values.put(SignalHV.ZS3V, ZS32.OFF);
            values.put(SignalHV.ZS1, false);
            values.put(SignalHV.ZS7, false);
        }
        this.changeIfPresent(values, info.current);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void reset(final SignalTileEnity current) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        values.put(SignalHV.HPBLOCK, HPBlock.HP0);
        values.put(SignalHV.HPHOME, HPHome.HP0);
        values.put(SignalHV.STOPSIGNAL, HP.HP0);
        values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
        values.put(SignalHV.ZS3, ZS32.OFF);
        values.put(SignalHV.ZS3V, ZS32.OFF);
        values.put(SignalHV.ZS1, false);
        values.put(SignalHV.ZS7, false);
        this.changeIfPresent(values, current);
    }

}
