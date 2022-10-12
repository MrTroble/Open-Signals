package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalKS.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.MAINSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3V;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.signalbox.config.KSSignalConfig;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public class GIRConfigtestKS {

    private final KSSignalConfig config = KSSignalConfig.INSTANCE;

    private void assertChange(final SignalTileEnity current, final SignalTileEnity next,
            final DummySignal expected) {
        this.assertChange(current, next, expected, 0);
    }

    private void assertChange(final SignalTileEnity current, final SignalTileEnity next,
            final DummySignal expected, final int speed) {
        assertNotEquals(current, expected);
        final ConfigInfo info = new ConfigInfo(current, next, speed);
        config.change(info);
        assertEquals(expected, current);
    }

    @Test
    public void testKSConfig() {
        final ConfigInfo info = new ConfigInfo(new DummySignal(), null, 0);
        config.change(info);
        assertEquals(new DummySignal(), info.current);

        assertChange(DummyBuilder.start(SignalKS.DISTANTSIGNAL, KSDistant.OFF).build(),
                DummyBuilder.start(SignalKS.STOPSIGNAL, KS.KS1_BLINK).build(),
                DummyBuilder.start(SignalKS.DISTANTSIGNAL, KSDistant.KS1).build());

        assertChange(DummyBuilder.start(SignalKS.DISTANTSIGNAL, KSDistant.OFF).build(),
                DummyBuilder.start(SignalKS.MAINSIGNAL, KSMain.KS1).build(),
                DummyBuilder.start(SignalKS.DISTANTSIGNAL, KSDistant.KS1).build());

        assertChange(DummyBuilder.start(SignalKS.STOPSIGNAL, KS.KS1).build(),
                DummyBuilder.start(SignalKS.STOPSIGNAL, KS.HP0).build(),
                DummyBuilder.start(SignalKS.STOPSIGNAL, KS.KS2).build());

        assertChange(DummyBuilder.start(SignalKS.ZS3V, ZS32.Z5).build(),
                DummyBuilder.start(SignalKS.ZS3, ZS32.Z2).build(),
                DummyBuilder.start(SignalKS.ZS3V, ZS32.Z2).build());

        assertChange(
                DummyBuilder.start(SignalKS.ZS3V, ZS32.Z5).of(SignalKS.STOPSIGNAL, KS.KS1).build(),
                DummyBuilder.start(SignalKS.ZS3, ZS32.Z2).of(SignalKS.STOPSIGNAL, KS.KS1).build(),
                DummyBuilder.start(SignalKS.ZS3V, ZS32.Z2).of(SignalKS.STOPSIGNAL, KS.KS1_BLINK)
                        .build());

        // KS -> KS
        for (final ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 43) {
                configtestKS(KS.KS2, KSMain.KS1, KSDistant.KS2, zs32, ZS32.OFF, KS.HP0, KSMain.HP0,
                        KSDistant.KS2, ZS32.OFF, ZS32.OFF, zs32.ordinal() - 26);
                configtestKS(KS.KS1, KSMain.KS1, KSDistant.KS1, zs32, ZS32.OFF, KS.KS2, KSMain.KS1,
                        KSDistant.KS1, zs32, ZS32.OFF, zs32.ordinal() - 26);
            }

        }
        configtestKS(KS.KS2, KSMain.KS1, KSDistant.KS2, ZS32.OFF, ZS32.OFF, KS.HP0, KSMain.HP0,
                KSDistant.KS2, ZS32.OFF, ZS32.OFF, 0);
        configtestKS(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.OFF, ZS32.OFF, 0);

        // KS -> HV
        configtestKSHV(KS.KS2, KSMain.KS1, KSDistant.KS2, ZS32.OFF, ZS32.OFF, HP.HP0, HPHome.HP0,
                HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);

        for (final ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 43) {
                configtestKSHV(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, zs32,
                        HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, ZS32.OFF, 0);
                configtestKSHV(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, zs32, HP.HP1,
                        HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, ZS32.OFF, zs32.ordinal() - 26);
                configtestKSHV(KS.KS2, KSMain.KS1, KSDistant.KS2, zs32, ZS32.OFF, HP.HP0,
                        HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, zs32.ordinal() - 26);
            }
        }
        configtestKSHV(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, 0);

        // KS -> HL
        configtestKSHL(KS.KS2, KSMain.KS1, KSDistant.KS2, ZS32.OFF, ZS32.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        for (final ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 43) {
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);

                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL5_6, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL5_6, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL5_6, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);

                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL8_9, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL8_9, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL8_9, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);

                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF,
                        zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);
            }
        }
        configtestKSHL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL1, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestKSHL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL4, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestKSHL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL7, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKSHL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10,
                HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestKSHL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL10, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
    }

    private void configtestKS(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final KS ksnext, final KSMain ksmainnext, final KSDistant distantnext,
            final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, ksnext)
                .of(MAINSIGNAL, ksmainnext).of(DISTANTSIGNAL, distantnext).of(ZS3, zs3next)
                .of(ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }

    private void configtestKSHV(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final HP hpnext, final HPHome hphomenext, final HPBlock hpblocknext, final VR vrnext,
            final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHV.STOPSIGNAL, hpnext)
                .of(SignalHV.HPHOME, hphomenext).of(SignalHV.HPBLOCK, hpblocknext)
                .of(SignalHV.DISTANTSIGNAL, vrnext).of(SignalHV.ZS3, zs3next)
                .of(SignalHV.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }

    private void configtestKSHL(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final HL hlnext, final HLExit exitnext, final HLDistant distantnext,
            final HLLightbar lightnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHL.STOPSIGNAL, hlnext)
                .of(SignalHL.EXITSIGNAL, exitnext).of(SignalHL.DISTANTSIGNAL, distantnext)
                .of(SignalHL.LIGHTBAR, lightnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }
}
