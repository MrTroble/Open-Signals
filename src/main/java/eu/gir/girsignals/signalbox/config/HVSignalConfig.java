package eu.gir.girsignals.signalbox.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.HPType;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class HVSignalConfig implements ISignalAutoconfig {

    public static final HVSignalConfig INSTANCE = new HVSignalConfig();
    private static final ArrayList<KS> STOP_KS = Lists.newArrayList(KS.HP0, KS.KS_SHUNTING,
            KS.KS_ZS1, KS.KS_ZS7);
    private static final ArrayList<KSMain> STOP_KS_MAIN = Lists.newArrayList(KSMain.HP0,
            KSMain.KS_SHUNTING, KSMain.KS_ZS1, KSMain.KS_ZS7);
    private static final ArrayList<HL> STOP_HL_MAIN = Lists.newArrayList(HL.HP0,
            HL.HP0_ALTERNATE_RED, HL.HL_SHUNTING, HL.HL_ZS1);
    private static final ArrayList<HLExit> STOP_HL_EXIT = Lists.newArrayList(HLExit.HP0,
            HLExit.HP0_ALTERNATE_RED, HLExit.HL_SHUNTING, HLExit.HL_ZS1);
    private static final ArrayList<HL> HL_40_MAIN = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9,
            HL.HL11_12);

    private HVSignalConfig() {
    }

    private VR next(final HP hp) {
        switch (hp) {
            case HP0:
                return VR.VR0;
            case HP1:
                return VR.VR1;
            case HP2:
                return VR.VR2;
            case OFF:
            default:
                return VR.OFF;
        }
    }

    private VR next(final HPHome hp) {
        switch (hp) {
            case HP0:
            case HP0_ALTERNATE_RED:
                return VR.VR0;
            case HP1:
                return VR.VR1;
            case HP2:
                return VR.VR2;
            case OFF:
            default:
                return VR.OFF;
        }
    }

    private VR next(final HPBlock hp) {
        switch (hp) {
            case HP0:
                return VR.VR0;
            case HP1:
                return VR.VR1;
            case OFF:
            default:
                return VR.OFF;
        }
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    @Override
    public void change(final int speed, final SignalTileEnity current, final SignalTileEnity next) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        if (next != null) {
            if (speed < 7 && speed > 0 && speed != 4) {
                current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
                    final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
                    current.setProperty(SignalHV.ZS3, zs32);
                });

                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP2);
                values.put(SignalHV.STOPSIGNAL, HP.HP2);
            } else if (speed == 4) {
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP2);
                values.put(SignalHV.STOPSIGNAL, HP.HP2);
            } else if (speed >= 7 && speed <= 16) {
                current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
                    final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
                    current.setProperty(SignalHV.ZS3, zs32);
                });
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP1);
                values.put(SignalHV.STOPSIGNAL, HP.HP1);
            } else {
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP1);
                values.put(SignalHV.STOPSIGNAL, HP.HP1);
            }

            final boolean hlstop = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> STOP_HL_MAIN.contains(a)).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).filter(d -> STOP_HL_EXIT.contains(d))
                            .isPresent();
            final boolean hlmain40 = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(c -> HL_40_MAIN.contains(c)).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).filter(HLExit.HL2_3::equals)
                            .isPresent();

            final boolean ksstop = next.getProperty(SignalKS.STOPSIGNAL)
                    .filter(a -> STOP_KS.contains(a)).isPresent();

            final Optional<HPHome> nextHPHOME = (Optional<HPHome>) next
                    .getProperty(SignalHV.HPHOME);
            final Optional<HP> nextHP = (Optional<HP>) next.getProperty(SignalHV.STOPSIGNAL);
            final boolean stop = next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP0::equals)
                    .isPresent() || nextHPHOME.filter(HPHome.HP0::equals).isPresent()
                    || nextHPHOME.filter(HPHome.HP0_ALTERNATE_RED::equals).isPresent()
                    || nextHP.filter(HP.HP0::equals).isPresent()
                    || nextHP.filter(HP.SHUNTING::equals).isPresent();
            final boolean ksstopmain = next.getProperty(SignalKS.MAINSIGNAL)
                    .filter(b -> STOP_KS_MAIN.contains(b)).isPresent();
            final Optional<ZS32> speedKS = (Optional<ZS32>) next.getProperty(SignalKS.ZS3);
            final Optional<ZS32> speedKSplate = (Optional<ZS32>) next
                    .getProperty(SignalKS.ZS3_PLATE);
            final Optional<HLLightbar> getlightbar = (Optional<HLLightbar>) next
                    .getProperty(SignalHL.LIGHTBAR);
            final Optional<VR> distantpresent = (Optional<VR>) current
                    .getProperty(SignalHV.DISTANTSIGNAL);
            final Optional<HP> stoppresent = (Optional<HP>) next.getProperty(SignalHL.STOPSIGNAL);
            final Optional<ZS32> speedHVZS3plate = (Optional<ZS32>) next
                    .getProperty(SignalHV.ZS3_PLATE);
            final Optional<VR> currentVR = (Optional<VR>) current
                    .getProperty(SignalHV.DISTANTSIGNAL);

            current.getProperty(SignalHV.DISTANTSIGNAL)
                    .ifPresent(_u -> next.getProperty(SignalHV.HPTYPE).ifPresent(type -> {
                        VR vr = VR.VR0;
                        switch ((HPType) type) {
                            case HPBLOCK:
                                vr = next((HPBlock) next.getProperty(SignalHV.HPBLOCK).get());
                                break;
                            case HPHOME:
                                vr = next((HPHome) next.getProperty(SignalHV.HPHOME).get());
                                break;
                            case STOPSIGNAL:
                                vr = next((HP) next.getProperty(SignalHV.STOPSIGNAL).get());
                                break;
                            case OFF:
                            default:
                                break;
                        }
                        current.setProperty(SignalHV.DISTANTSIGNAL, vr);
                    }));
            current.getProperty(SignalHV.ZS3V).ifPresent(_u -> {
                current.setProperty(SignalHV.ZS3V, ZS32.OFF);
                next.getProperty(SignalHV.ZS3)
                        .ifPresent(prevzs3 -> current.setProperty(SignalHV.ZS3V, (ZS32) prevzs3));
            });

            if (stoppresent.isPresent() || next.getProperty(SignalHL.EXITSIGNAL).isPresent()) {
                if (distantpresent.isPresent()) {
                    if (hlstop) {
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                    } else if (current.getProperty(SignalHV.ZS3V).isPresent()) {
                        if (next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                                || next.getProperty(SignalHL.EXITSIGNAL).isPresent()) {
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
            if (current.getProperty(SignalHV.DISTANTSIGNAL).isPresent()) {
                if ((!ksstop || !ksstopmain) && (next.getProperty(SignalKS.STOPSIGNAL).isPresent()
                        || next.getProperty(SignalKS.MAINSIGNAL).isPresent())) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                    if (current.getProperty(SignalHV.ZS3V).isPresent()
                            && (speedKS.isPresent() || speedKSplate.isPresent())) {
                        final ZS32 speednext = speedKS.isPresent() ? speedKS.get()
                                : speedKSplate.get();
                        final int zs32 = speednext.ordinal();
                        if (zs32 > 26 && zs32 <= 42) {
                            values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                            values.put(SignalHV.ZS3V, speednext);
                            if (zs32 == 30) {
                                values.put(SignalHV.ZS3V, VR.OFF);
                            }
                            if (zs32 > 32) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                            }
                        }
                    }
                } else if (ksstop || ksstopmain) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                }
            }
            if (speedHVZS3plate.isPresent() && !stop) {
                final ZS32 speedcurrent = speedHVZS3plate.get();
                final int zs32 = speedcurrent.ordinal();
                values.put(SignalHV.ZS3V, speedcurrent);
                if (zs32 > 26 && zs32 <= 42) {
                    values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                    if (zs32 == 30) {
                        values.put(SignalHV.ZS3V, VR.VR2);
                        values.put(SignalHV.ZS3V, ZS32.OFF);
                    }
                    if (zs32 > 32) {
                        values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                    }
                }
            }
        } else {
            values.put(SignalHV.HPBLOCK, HPBlock.HP1);
            values.put(SignalHV.HPHOME, HPHome.HP2);
            values.put(SignalHV.STOPSIGNAL, HP.HP2);
            values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
            values.put(SignalHV.ZS3, ZS32.OFF);
            values.put(SignalHV.ZS3V, ZS32.OFF);
            values.put(SignalHV.ZS1, false);
            values.put(SignalHV.ZS7, false);
        }
        this.changeIfPresent(values, current);
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
