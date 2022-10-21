package com.troblecodings.signals.test;

import static com.troblecodings.signals.blocks.signals.SignalHL.DISTANTSIGNAL;
import static com.troblecodings.signals.blocks.signals.SignalHL.EXITSIGNAL;
import static com.troblecodings.signals.blocks.signals.SignalHL.LIGHTBAR;
import static com.troblecodings.signals.blocks.signals.SignalHL.STOPSIGNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.troblecodings.signals.EnumSignals.HL;
import com.troblecodings.signals.EnumSignals.HLDistant;
import com.troblecodings.signals.EnumSignals.HLExit;
import com.troblecodings.signals.EnumSignals.HLLightbar;
import com.troblecodings.signals.EnumSignals.HP;
import com.troblecodings.signals.EnumSignals.HPBlock;
import com.troblecodings.signals.EnumSignals.HPHome;
import com.troblecodings.signals.EnumSignals.KS;
import com.troblecodings.signals.EnumSignals.KSDistant;
import com.troblecodings.signals.EnumSignals.KSMain;
import com.troblecodings.signals.EnumSignals.VR;
import com.troblecodings.signals.EnumSignals.ZS32;
import com.troblecodings.signals.blocks.signals.SignalHV;
import com.troblecodings.signals.blocks.signals.SignalKS;
import com.troblecodings.signals.signalbox.config.HLSignalConfig;
import com.troblecodings.signals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import com.troblecodings.signals.test.DummySignal.DummyBuilder;

public class GIRConfigtestHL {

    private final HLSignalConfig config = HLSignalConfig.INSTANCE;

    @Test
    public void testHLConfig() {
        final ConfigInfo info = new ConfigInfo(new DummySignal(), null, 0);
        config.change(info);
        assertEquals(new DummySignal(), info.current);

        // HL -> HL
        configtestHL(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 0);
        configtestHL(HL.HL1, HLExit.HL1, HLDistant.HL1, HLLightbar.OFF, HL.HL10, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestHL(HL.HL4, HLExit.HL1, HLDistant.HL4, HLLightbar.OFF, HL.HL2_3, HLExit.HL2_3,
                HLDistant.HL1, HLLightbar.GREEN, 0);

        configtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 4);
        configtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.YELLOW, HL.HP0,
                HLExit.HP0, HLDistant.HL10, HLLightbar.OFF, 6);
        configtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.GREEN, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 10);

        configtestHL(HL.HL7, HLExit.HL1, HLDistant.HL7, HLLightbar.OFF, HL.HL11_12, HLExit.HL2_3,
                HLDistant.HL7, HLLightbar.OFF, 0);
        configtestHL(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 4);
        configtestHL(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 6);
        configtestHL(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.GREEN, HL.HL11_12,
                HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, 10);

        configtestHL(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, HL.HL2_3, HLExit.HL2_3,
                HLDistant.HL1, HLLightbar.OFF, 4);
        configtestHL(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, 6);
        configtestHL(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, HL.HL2_3,
                HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, 10);

        // HL -> KS
        configtestHLKS(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, KS.HP0, KSMain.HP0,
                KSDistant.KS2, ZS32.OFF, ZS32.OFF, 0);
        configtestHLKS(HL.HL1, HLExit.HL1, HLDistant.HL1, HLLightbar.OFF, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.OFF, ZS32.OFF, 0);

        configtestHLKS(HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, KS.KS2, KSMain.KS1,
                KSDistant.KS2, ZS32.OFF, ZS32.OFF, 4);
        configtestHLKS(HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, KS.KS2, KSMain.KS1,
                KSDistant.KS1, ZS32.OFF, ZS32.OFF, 6);
        configtestHLKS(HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, KS.KS2_LIGHT,
                KSMain.KS1, KSDistant.KS1, ZS32.OFF, ZS32.OFF, 10);

        configtestHLKS(HL.HL4, HLExit.HL1, HLDistant.HL4, HLLightbar.OFF, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z10, ZS32.OFF, 0);
        configtestHLKS(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.OFF, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z10, ZS32.OFF, 4);
        configtestHLKS(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.YELLOW, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z10, ZS32.OFF, 6);
        configtestHLKS(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z10, ZS32.OFF, 10);

        configtestHLKS(HL.HL7, HLExit.HL1, HLDistant.HL7, HLLightbar.OFF, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z4, ZS32.OFF, 0);
        configtestHLKS(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z4, ZS32.OFF, 4);
        configtestHLKS(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z4, ZS32.OFF, 6);
        configtestHLKS(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.GREEN, KS.KS1, KSMain.KS1,
                KSDistant.KS1, ZS32.Z4, ZS32.OFF, 10);

        configtestHLKS(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.OFF, KS.HP0, KSMain.HP0,
                KSDistant.KS2, ZS32.OFF, ZS32.OFF, 4);
        configtestHLKS(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.YELLOW, KS.HP0,
                KSMain.HP0, KSDistant.KS2, ZS32.OFF, ZS32.OFF, 6);
        configtestHLKS(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.GREEN, KS.HP0,
                KSMain.HP0, KSDistant.KS2, ZS32.OFF, ZS32.OFF, 10);

        // HL -> HV
        configtestHLHV(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, HP.HP0, HPHome.HP0,
                HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
        configtestHLHV(HL.HL1, HLExit.HL1, HLDistant.HL1, HLLightbar.OFF, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, 0);

        configtestHLHV(HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.OFF, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, 4);
        configtestHLHV(HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.YELLOW, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, 6);
        configtestHLHV(HL.HL2_3, HLExit.HL2_3, HLDistant.HL1, HLLightbar.GREEN, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR1, ZS32.OFF, ZS32.OFF, 10);

        configtestHLHV(HL.HL4, HLExit.HL1, HLDistant.HL4, HLLightbar.OFF, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR1, ZS32.Z10, ZS32.OFF, 0);
        configtestHLHV(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.OFF, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR0, ZS32.Z10, ZS32.OFF, 4);
        configtestHLHV(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.YELLOW, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR0, ZS32.Z10, ZS32.OFF, 6);
        configtestHLHV(HL.HL5_6, HLExit.HL2_3, HLDistant.HL4, HLLightbar.GREEN, HP.HP1, HPHome.HP1,
                HPBlock.HP1, VR.VR0, ZS32.Z10, ZS32.OFF, 10);

        configtestHLHV(HL.HL7, HLExit.HL1, HLDistant.HL7, HLLightbar.OFF, HP.HP2, HPHome.HP2,
                HPBlock.HP1, VR.VR1, ZS32.Z4, ZS32.OFF, 0);
        configtestHLHV(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.OFF, HP.HP2, HPHome.HP2,
                HPBlock.HP1, VR.VR1, ZS32.Z4, ZS32.OFF, 4);
        configtestHLHV(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.YELLOW, HP.HP2, HPHome.HP2,
                HPBlock.HP1, VR.VR1, ZS32.Z4, ZS32.OFF, 6);
        configtestHLHV(HL.HL8_9, HLExit.HL2_3, HLDistant.HL7, HLLightbar.GREEN, HP.HP2, HPHome.HP2,
                HPBlock.HP1, VR.VR1, ZS32.Z4, ZS32.OFF, 10);

        configtestHLHV(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.OFF, HP.HP0, HPHome.HP0,
                HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 4);
        configtestHLHV(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.YELLOW, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 6);
        configtestHLHV(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.GREEN, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 10);
    }

    private void configtestHL(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final HL hlnext,
            final HLExit exitnext, final HLDistant distantnext, final HLLightbar lightnext,
            final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, hlnext)
                .of(EXITSIGNAL, exitnext).of(DISTANTSIGNAL, distantnext).of(LIGHTBAR, lightnext)
                .build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }

    private void configtestHLKS(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final KS ksnext,
            final KSMain ksmainnext, final KSDistant distantnext, final ZS32 zs3next,
            final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalKS.STOPSIGNAL, ksnext)
                .of(SignalKS.MAINSIGNAL, ksmainnext).of(SignalKS.DISTANTSIGNAL, distantnext)
                .of(SignalKS.ZS3, zs3next).of(SignalKS.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }

    private void configtestHLHV(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final HP hpnext,
            final HPHome hphomenext, final HPBlock hpblocknext, final VR vrnext, final ZS32 zs3next,
            final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        config.reset(signalBase);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHV.STOPSIGNAL, hpnext)
                .of(SignalHV.HPHOME, hphomenext).of(SignalHV.HPBLOCK, hpblocknext)
                .of(SignalHV.DISTANTSIGNAL, vrnext).of(SignalHV.ZS3, zs3next)
                .of(SignalHV.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(signalBase, signalnext, speed));
        assertEquals(signalDummy, signalBase);
    }
}
