package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class KSSignalConfig implements ISignalAutoconfig {

    public static final KSSignalConfig INSTANCE = new KSSignalConfig();

    private KSSignalConfig() {
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public void change(final ConfigInfo info) {

        if (info.type.equals(PathType.SHUNTING)) {
            RSSignalConfig.RS_CONFIG.change(info);
            return;
        }

        final HashMap<SEProperty, Object> values = new HashMap<>();

        if (info.next != null) {
            if (info.speed <= 16 && info.speed > 0) {
                final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + info.speed];
                values.put(SignalKS.ZS3, zs32);
            }
            final Optional<ZS32> speedHV = (Optional<ZS32>) info.next.getProperty(SignalHV.ZS3);
            final Optional<ZS32> speedHVplate = (Optional<ZS32>) info.next
                    .getProperty(SignalHV.ZS3_PLATE);
            final boolean nexthl = info.next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL).isPresent();
            final Optional<HLLightbar> nextlighbar = (Optional<HLLightbar>) info.next
                    .getProperty(SignalHL.LIGHTBAR);
            final Optional<ZS32> currentzs3v = (Optional<ZS32>) info.current
                    .getProperty(SignalKS.ZS3V);
            final Optional<ZS32> speedKSZS3plate = (Optional<ZS32>) info.next
                    .getProperty(SignalKS.ZS3_PLATE);
            final Optional<KS> currentks = (Optional<KS>) info.current
                    .getProperty(SignalKS.STOPSIGNAL);
            final Optional<ZS32> nextZS3 = (Optional<ZS32>) info.next.getProperty(SignalKS.ZS3);
            final Optional opt = info.next.getProperty(SignalKS.STOPSIGNAL);

            final boolean stop = info.next.getProperty(SignalKS.MAINSIGNAL)
                    .filter(KSMain.HP0::equals).isPresent()
                    || opt.filter(KS.HP0::equals).isPresent();
            final boolean hlstop = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> Signallists.HL_STOP.contains(a)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL)
                            .filter(d -> Signallists.HLEXIT_STOP.contains(d)).isPresent();
            final boolean hlmain40 = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(c -> Signallists.HL_40_MAIN.contains(c)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL).filter(HLExit.HL2_3::equals)
                            .isPresent();
            final boolean hvstopgo = info.next.getProperty(SignalHV.STOPSIGNAL)
                    .filter(HP.HP1::equals).isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals)
                            .isPresent();
            final boolean hvhomego = info.next.getProperty(SignalHV.HPHOME)
                    .filter(HPHome.HP1::equals).isPresent()
                    || info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals)
                            .isPresent();
            final boolean hvblockgo = info.next.getProperty(SignalHV.HPBLOCK)
                    .filter(HPBlock.HP1::equals).isPresent();
            final boolean hv40 = info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals)
                            .isPresent();
            final boolean hvstop = info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP0::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.SHUNTING::equals)
                            .isPresent()
                    || info.next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP0::equals)
                            .isPresent();
            final boolean hvstop2 = info.next.getProperty(SignalHV.HPHOME)
                    .filter(HPHome.HP0::equals).isPresent()
                    || info.next.getProperty(SignalHV.HPHOME)
                            .filter(HPHome.HP0_ALTERNATE_RED::equals).isPresent();

            info.current.getProperty(SignalKS.ZS3V).ifPresent(_u -> nextZS3
                    .ifPresent(value -> info.current.setProperty(SignalKS.ZS3V, (ZS32) value)));

            final boolean changes = nextZS3.filter(e -> ((ZS32) e).ordinal() > ZS32.Z.ordinal()
                    && (((ZS32) e).ordinal() - ZS32.Z.ordinal()) != info.speed).isPresent();

            values.put(SignalKS.MAINSIGNAL, KSMain.KS1);

            if (stop || hlstop) {
                values.put(SignalKS.STOPSIGNAL, KS.KS2);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
                values.put(SignalKS.ZS3V, ZS32.OFF);
            } else if (changes) {
                values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
            } else {
                values.put(SignalKS.STOPSIGNAL, KS.KS1);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1);
                values.put(SignalKS.ZS3V, ZS32.OFF);
            }
            if (speedKSZS3plate.isPresent() && currentks.isPresent()
                    && !currentks.filter(KS.HP0::equals).isPresent()) {
                if (!nextZS3.isPresent() && currentzs3v.isPresent() && !stop) {
                    final ZS32 speednext = speedKSZS3plate.get();
                    final int speed = speednext.ordinal();
                    if (speed > 26 && speed < 42) {
                        values.put(SignalKS.ZS3V, speednext);
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                    } else if (speednext.ordinal() <= 26) {
                        values.put(SignalKS.ZS2V, speednext);
                        values.put(SignalKS.STOPSIGNAL, KS.KS1);
                    }
                }
            }
            if ((!hlmain40 && nexthl) || hvstop || hvstop2) {
                values.put(SignalKS.ZS3V, ZS32.OFF);
            }
            if (currentzs3v.isPresent()) {
                if (nexthl) {
                    if (hlmain40 && nextlighbar.filter(HLLightbar.OFF::equals).isPresent()) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                        values.put(SignalKS.ZS3V, ZS32.Z4);
                    } else if (hlmain40
                            && nextlighbar.filter(HLLightbar.YELLOW::equals).isPresent()) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                        values.put(SignalKS.ZS3V, ZS32.Z6);
                    } else if (hlmain40
                            && nextlighbar.filter(HLLightbar.GREEN::equals).isPresent()) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                        values.put(SignalKS.ZS3V, ZS32.Z10);
                    } else if (hlstop) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS2);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
                    } else {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1);
                    }
                }
            }
            if (hvblockgo || hvhomego || hvstopgo) {
                values.put(SignalKS.STOPSIGNAL, KS.KS1);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1);
                if (hv40 && currentzs3v.isPresent()) {
                    values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                    values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                    values.put(SignalKS.ZS3V, ZS32.Z4);
                }
                if ((speedHV.isPresent() || speedHVplate.isPresent()) && currentzs3v.isPresent()) {
                    final ZS32 speednext = speedHV.isPresent() ? speedHV.get() : speedHVplate.get();
                    final int zs32 = speednext.ordinal();
                    if (zs32 > 26 && zs32 <= 42 && zs32 != 30) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                        values.put(SignalKS.ZS3V, speednext);
                    } else if (zs32 == 30) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                        values.put(SignalKS.ZS3V, ZS32.Z4);
                    } else if (zs32 < 26) {
                        values.put(SignalKS.ZS2V, speednext);
                    }
                }
            } else if (hvstop || hvstop2) {
                values.put(SignalKS.STOPSIGNAL, KS.KS2);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
            }
        } else {
            values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
            values.put(SignalKS.STOPSIGNAL, KS.KS2);
            values.put(SignalKS.MAINSIGNAL, KSMain.KS1);
            values.put(SignalKS.ZS2, ZS32.OFF);
            values.put(SignalKS.ZS2V, ZS32.OFF);
            values.put(SignalKS.ZS3V, ZS32.OFF);
            values.put(SignalKS.ZS3, ZS32.Z3);
        }

        this.changeIfPresent(values, info.current);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void reset(final SignalTileEnity current) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
        values.put(SignalKS.STOPSIGNAL, KS.HP0);
        values.put(SignalKS.MAINSIGNAL, KSMain.HP0);
        values.put(SignalKS.ZS2, ZS32.OFF);
        values.put(SignalKS.ZS3, ZS32.OFF);
        values.put(SignalKS.ZS2V, ZS32.OFF);
        values.put(SignalKS.ZS3V, ZS32.OFF);
        this.changeIfPresent(values, current);
    }

}
