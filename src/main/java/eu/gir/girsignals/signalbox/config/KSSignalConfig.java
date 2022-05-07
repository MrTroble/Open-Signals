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
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class KSSignalConfig implements ISignalAutoconfig {

    public static final KSSignalConfig INSTANCE = new KSSignalConfig();
    private static final ArrayList<HL> STOP_HL = Lists.newArrayList(HL.HP0, HL.HP0_ALTERNATE_RED,
            HL.HL_SHUNTING, HL.HL_ZS1);
    private static final ArrayList<HLExit> STOP_HL_EXIT = Lists.newArrayList(HLExit.HP0,
            HLExit.HP0_ALTERNATE_RED, HLExit.HL_SHUNTING, HLExit.HL_ZS1);
    private static final ArrayList<HL> HL_40_MAIN = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9,
            HL.HL11_12);

    private KSSignalConfig() {
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public void change(final int speed, final SignalTileEnity current, final SignalTileEnity next) {
        final HashMap<SEProperty, Object> values = new HashMap<>();

        if (next != null) {
            current.getProperty(SignalKS.ZS3V).ifPresent(_u -> next.getProperty(SignalKS.ZS3)
                    .ifPresent(value -> current.setProperty(SignalKS.ZS3V, (ZS32) value)));
            if (speed <= 16 && speed > 0) {
                final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
                current.getProperty(SignalKS.ZS3)
                        .ifPresent(_u -> current.setProperty(SignalKS.ZS3, zs32));
            }
            final boolean changes = next.getProperty(SignalKS.ZS3)
                    .filter(e -> ((ZS32) e).ordinal() > ZS32.Z.ordinal()
                            && (((ZS32) e).ordinal() - ZS32.Z.ordinal()) < speed)
                    .isPresent();
            values.put(SignalKS.MAINSIGNAL, KSMain.KS1);
            final Optional opt = next.getProperty(SignalKS.STOPSIGNAL);
            final boolean stop = next.getProperty(SignalKS.MAINSIGNAL).filter(KSMain.HP0::equals)
                    .isPresent() || opt.filter(KS.HP0::equals).isPresent();

            final boolean hlstop = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> STOP_HL.contains(a)).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).filter(d -> STOP_HL_EXIT.contains(d))
                            .isPresent();
            final boolean hlmain40 = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(c -> HL_40_MAIN.contains(c)).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).filter(HLExit.HL2_3::equals)
                            .isPresent();

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
            final boolean hvstop = next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP0::equals)
                    .isPresent()
                    || next.getProperty(SignalHV.STOPSIGNAL).filter(HP.SHUNTING::equals).isPresent()
                    || next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP0::equals).isPresent();
            final boolean hvstop2 = next.getProperty(SignalHV.HPHOME).filter(HPHome.HP0::equals)
                    .isPresent()
                    || next.getProperty(SignalHV.HPHOME).filter(HPHome.HP0_ALTERNATE_RED::equals)
                            .isPresent();
            final Optional<ZS32> speedHV = (Optional<ZS32>) next.getProperty(SignalHV.ZS3);
            final boolean nexthl = next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).isPresent();
            final Optional<HLLightbar> nextlighbar = (Optional<HLLightbar>) next
                    .getProperty(SignalHL.LIGHTBAR);
            final Optional<ZS32> currentzs3v = (Optional<ZS32>) current.getProperty(SignalKS.ZS3V);

            if (stop || hlstop) {
                values.put(SignalKS.STOPSIGNAL, KS.KS2);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
            } else if (changes) {
                values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
            } else {
                values.put(SignalKS.STOPSIGNAL, KS.KS1);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1);
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
                    values.put(SignalKS.ZS3V, ZS32.Z4);
                }
                if (speedHV.isPresent() && currentzs3v.isPresent()) {
                    final ZS32 speednext = speedHV.get();
                    final int zs32 = speednext.ordinal();
                    if (zs32 > 26 && zs32 <= 42 && zs32 != 30) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.ZS3V, speednext);
                    }
                    if (zs32 == 30) {
                        values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                        values.put(SignalKS.ZS3V, ZS32.Z4);
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
            values.put(SignalKS.ZS3, ZS32.OFF);
            values.put(SignalKS.ZS2V, ZS32.OFF);
            values.put(SignalKS.ZS3V, ZS32.OFF);
        }
        this.changeIfPresent(values, current);
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
