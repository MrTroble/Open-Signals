package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalKS.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.MAINSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3V;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.signalbox.config.KSSignalConfig;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigtestKS {

    final KSSignalConfig config = KSSignalConfig.INSTANCE;

    @Test
    public void testKSConfig() {

        // KS -> KS
        for (ZS32 zs32 : ZS32.values()) {
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
        configtestKS_HV(KS.KS2, KSMain.KS1, KSDistant.KS2, ZS32.OFF, ZS32.OFF, HP.HP0, HPHome.HP0,
                HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
        
        for (ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 43) {
                configtestKS_HV(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, zs32,
                        HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, ZS32.OFF, 0);
                configtestKS_HV(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, zs32, HP.HP1,
                        HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, ZS32.OFF, zs32.ordinal() - 26);
                configtestKS_HV(KS.KS2, KSMain.KS1, KSDistant.KS2, zs32, ZS32.OFF,
                        HP.HP0, HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF,
                        zs32.ordinal() - 26);
            }
        }
        configtestKS_HV(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, 0);

        // KS -> HL
        configtestKS_HL(KS.KS2, KSMain.KS1, KSDistant.KS2, ZS32.OFF, ZS32.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        for (ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 43) {
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);

                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL5_6, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL5_6, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL5_6, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);

                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL8_9, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL8_9, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL8_9, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);

                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z4,
                        HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF,
                        zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z6,
                        HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW,
                        zs32.ordinal() - 26);
                configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, zs32, ZS32.Z10,
                        HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN,
                        zs32.ordinal() - 26);
            }
        }
        configtestKS_HL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL1, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestKS_HL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL4, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestKS_HL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL7, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z4,
                HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z6,
                HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, 0);
        configtestKS_HL(KS.KS1_BLINK, KSMain.KS1, KSDistant.KS1_BLINK, ZS32.OFF, ZS32.Z10,
                HL.HL11_12, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestKS_HL(KS.KS1, KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, HL.HL10, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
    }

    private void configtestKS(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final KS ksnext, final KSMain ksmainnext, final KSDistant distantnext,
            final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, ksnext)
                .of(MAINSIGNAL, ksmainnext).of(DISTANTSIGNAL, distantnext).of(ZS3, zs3next)
                .of(ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

    private void configtestKS_HV(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final HP hpnext, final HPHome hphomenext, final HPBlock hpblocknext, final VR vrnext,
            final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHV.STOPSIGNAL, hpnext)
                .of(SignalHV.HPHOME, hphomenext).of(SignalHV.HPBLOCK, hpblocknext)
                .of(SignalHV.DISTANTSIGNAL, vrnext).of(SignalHV.ZS3, zs3next)
                .of(SignalHV.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

    private void configtestKS_HL(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final HL hlnext, final HLExit exitnext, final HLDistant distantnext,
            final HLLightbar lightnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHL.STOPSIGNAL, hlnext)
                .of(SignalHL.EXITSIGNAL, exitnext).of(SignalHL.DISTANTSIGNAL, distantnext)
                .of(SignalHL.LIGHTBAR, lightnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }
}
