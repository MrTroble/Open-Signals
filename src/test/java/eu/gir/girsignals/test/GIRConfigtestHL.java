package eu.gir.girsignals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import static eu.gir.girsignals.blocks.signals.SignalHL.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHL.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHL.EXITSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHL.LIGHTBAR;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.signalbox.config.HLSignalConfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigtestHL {

    final HLSignalConfig config = HLSignalConfig.INSTANCE;

    @Test
    public void testHLConfig() {

        ConfigtestHL(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 0);
        ConfigtestHL(HL.HL1, HLExit.HL1, HLDistant.HL1, HLLightbar.OFF, HL.HL10, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        ConfigtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 4);
        ConfigtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.YELLOW, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 6);
        ConfigtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.GREEN, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 10);

    }

    private void ConfigtestHL(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final HL hlnext,
            final HLExit exitnext, final HLDistant distantnext, final HLLightbar lightnext,
            final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, hlnext)
                .of(EXITSIGNAL, exitnext).of(DISTANTSIGNAL, distantnext).of(LIGHTBAR, lightnext)
                .build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }
}
