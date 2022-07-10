package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalHV.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPBLOCK;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPHOME;
import static eu.gir.girsignals.blocks.signals.SignalHV.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3V;
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
import eu.gir.girsignals.signalbox.config.HVSignalConfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public class GIRConfigtestHV {

    private final HVSignalConfig config = HVSignalConfig.INSTANCE;

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
    public void testHVConfig() {
        final ConfigInfo info = new ConfigInfo(new DummySignal(), null, 0);
        config.change(info);
        assertEquals(new DummySignal(), info.current);

        assertChange(
                DummyBuilder.start(SignalHV.STOPSIGNAL, HP.HP2).of(SignalHV.ZS3V, ZS32.Z15).build(),
                DummyBuilder.start(SignalHV.STOPSIGNAL, HP.HP1).build(), DummyBuilder
                        .start(SignalHV.STOPSIGNAL, HP.HP1).of(SignalHV.ZS3V, ZS32.Z15).build());

        // Works at HV
        assertChange(DummyBuilder.start(SignalHV.ZS3V, ZS32.Z2).build(),
                DummyBuilder.start(SignalHV.ZS3, ZS32.Z5).build(),
                DummyBuilder.start(SignalHV.ZS3V, ZS32.Z5).build());

        // Issue 1
//        assertChange(DummyBuilder.start(SignalHV.DISTANTSIGNAL, VR.VR1).build(),
//                DummyBuilder.start(SignalHV.STOPSIGNAL, HP.HP2).build(),
//                DummyBuilder.start(SignalHV.DISTANTSIGNAL, VR.VR2).build());
//
//        assertChange(DummyBuilder.start(SignalHV.DISTANTSIGNAL, VR.VR1).build(),
//                DummyBuilder.start(SignalHV.STOPSIGNAL, HP.HP0).build(),
//                DummyBuilder.start(SignalHV.DISTANTSIGNAL, VR.VR0).build());

        // HV -> HV
        configtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
        configtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.Z6, HP.HP2, HPHome.HP2,
                HPBlock.HP1, VR.VR0, ZS32.Z6, ZS32.OFF, 0);
        configtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, HP.HP1,
                HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, 0);
        for (final ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 33 && zs32.ordinal() != 30) {
                configtestHV(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR2, zs32, zs32, HP.HP2,
                        HPHome.HP2, HPBlock.HP1, VR.VR0, zs32, ZS32.OFF, zs32.ordinal() - 26);
            } else if (zs32.ordinal() == 30) {
                configtestHV(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, HP.HP1,
                        HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, 4);
            } else if (zs32.ordinal() > 32 && zs32.ordinal() < 43) {
                configtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, zs32, HP.HP1,
                        HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, zs32, zs32.ordinal() - 26);
            }
        }

        // HV -> HL
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HL.HP0,
                HLExit.HP0, HLDistant.HL10, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, HL.HL10,
                HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, HL.HL1,
                HLExit.HL1, HLDistant.HL1, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.OFF, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.Z6, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.Z10, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.OFF, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.Z6, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.Z10, HL.HL5_6,
                HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.OFF, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.Z6, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.Z10, HL.HL8_9,
                HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.OFF, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.Z6, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, 0);
        configtestHVHL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.Z10, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, 0);

        // HV -> KS
        configtestHVKS(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, KS.HP0,
                KSMain.HP0, KSDistant.KS2, ZS32.OFF, ZS32.OFF, 0);
        for (final ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 33 && zs32.ordinal() != 30) {
                configtestHVKS(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR2, zs32, zs32, KS.KS1,
                        KSMain.KS1, KSDistant.KS1, zs32, ZS32.OFF, zs32.ordinal() - 26);
            } else if (zs32.ordinal() == 30) {
                configtestHVKS(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR2, ZS32.OFF, ZS32.OFF, KS.KS1,
                        KSMain.KS1, KSDistant.KS1, ZS32.Z4, ZS32.OFF, 4);
            } else if (zs32.ordinal() > 32 && zs32.ordinal() < 43) {
                configtestHVKS(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, zs32, KS.KS1,
                        KSMain.KS1, KSDistant.KS1, zs32, ZS32.OFF, zs32.ordinal() - 26);
            }
        }
        configtestHVKS(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, KS.HP0,
                KSMain.HP0, KSDistant.KS2, ZS32.OFF, ZS32.OFF, 0);

    }

    private void configtestHV(final HP hpcurrent, final HPHome hphomecurrent,
            final HPBlock hpblockcurrent, final VR vrcurrent, final ZS32 zs3current,
            final ZS32 zs3vcurrent, final HP hpnext, final HPHome hphomenext,
            final HPBlock hpblocknext, final VR vrnext, final ZS32 zs3next, final ZS32 zs3vnext,
            final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(HPBLOCK, HPBlock.HP0).of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, hpnext).of(HPHOME, hphomenext)
                .of(HPBLOCK, hpblocknext).of(DISTANTSIGNAL, vrnext).of(ZS3, zs3next)
                .of(ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);

    }

    private void configtestHVHL(final HP hpcurrent, final HPHome hphomecurrent,
            final HPBlock hpblockcurrent, final VR vrcurrent, final ZS32 zs3current,
            final ZS32 zs3vcurrent, final HL hlnext, final HLExit exitnext,
            final HLDistant distantnext, final HLLightbar lightnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(HPBLOCK, HPBlock.HP0).of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHL.STOPSIGNAL, hlnext)
                .of(SignalHL.EXITSIGNAL, exitnext).of(SignalHL.DISTANTSIGNAL, distantnext)
                .of(SignalHL.LIGHTBAR, lightnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }

    private void configtestHVKS(final HP hpcurrent, final HPHome hphomecurrent,
            final HPBlock hpblockcurrent, final VR vrcurrent, final ZS32 zs3current,
            final ZS32 zs3vcurrent, final KS ksnext, final KSMain ksmainnext,
            final KSDistant distantnext, final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(HPBLOCK, HPBlock.HP0).of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalKS.STOPSIGNAL, ksnext)
                .of(SignalKS.MAINSIGNAL, ksmainnext).of(SignalKS.DISTANTSIGNAL, distantnext)
                .of(SignalKS.ZS3, zs3next).of(SignalKS.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }

}
