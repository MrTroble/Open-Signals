package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalHV.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPBLOCK;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPHOME;
import static eu.gir.girsignals.blocks.signals.SignalHV.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3V;
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
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.signalbox.config.HVSignalConfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigtestHV {

    final HVSignalConfig config = HVSignalConfig.INSTANCE;

    @Test
    public void testHVConfig() {

        // HV -> HV
        configtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
        configtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);

        // HV -> HL
        configtestHV_HL(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HL.HP0,
                HLExit.HP0, HLDistant.HL10, HLLightbar.OFF, 0);

        // HV -> KS
        configtestHV_KS(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, KS.HP0,
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
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, hpnext).of(HPHOME, hphomenext)
                .of(HPBLOCK, hpblocknext).of(DISTANTSIGNAL, vrnext).of(ZS3, zs3next)
                .of(ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

    private void configtestHV_HL(final HP hpcurrent, final HPHome hphomecurrent,
            final HPBlock hpblockcurrent, final VR vrcurrent, final ZS32 zs3current,
            final ZS32 zs3vcurrent, final HL hlnext, final HLExit exitnext,
            final HLDistant distantnext, final HLLightbar lightnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(HPBLOCK, HPBlock.HP0).of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHL.STOPSIGNAL, hlnext)
                .of(SignalHL.EXITSIGNAL, exitnext).of(SignalHL.DISTANTSIGNAL, distantnext)
                .of(SignalHL.LIGHTBAR, lightnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

    private void configtestHV_KS(final HP hpcurrent, final HPHome hphomecurrent,
            final HPBlock hpblockcurrent, final VR vrcurrent, final ZS32 zs3current,
            final ZS32 zs3vcurrent, final KS ksnext, final KSMain ksmainnext,
            final KSDistant distantnext, final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(HPBLOCK, HPBlock.HP0).of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalKS.STOPSIGNAL, ksnext)
                .of(SignalKS.MAINSIGNAL, ksmainnext).of(SignalKS.DISTANTSIGNAL, distantnext)
                .of(SignalKS.ZS3, zs3next).of(SignalKS.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

}
